package com.example.paddyleafdiseasedetection;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.paddyleafdiseasedetection.ml.PaddyLeafDiseaseModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int IMAGE_SIZE = 224;

    private ImageView imageView;
    private ImageButton imageCaptureButton, imageUploadButton;
    private Button fabDetectButton;
    private TextView result, addDetails;
    private ProgressBar progressBar;
    private Uri photoURI;
    private String detectedDisease;
    private float detectedConfidence;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI components
        imageView = view.findViewById(R.id.image_preview);
        result = view.findViewById(R.id.result);
        addDetails = view.findViewById(R.id.additionaldetails);
        progressBar = view.findViewById(R.id.progress_bar);
        imageCaptureButton = view.findViewById(R.id.button_capture);
        imageUploadButton = view.findViewById(R.id.button_upload);
        fabDetectButton = view.findViewById(R.id.fab_detect);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Button listeners
        imageCaptureButton.setOnClickListener(v -> checkCameraPermission());
        imageUploadButton.setOnClickListener(v -> openGallery());
        fabDetectButton.setOnClickListener(v -> handleDetectDiseaseButtonClick());

        addDetails.setOnClickListener(v -> {
            if (detectedDisease != null) {
                openAdditionalDetailsFragment();
            } else {
                Toast.makeText(getContext(), getString(R.string.capture_or_upload), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_IMAGE_PICK);
        }
    }

    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
    }

    private void handleDetectDiseaseButtonClick() {
        if (imageView.getDrawable() != null) {
            detectDisease();
        } else {
            Toast.makeText(getContext(), getString(R.string.capture_or_upload), Toast.LENGTH_SHORT).show();
        }
    }

    private void detectDisease() {
        progressBar.setVisibility(View.VISIBLE);
        fabDetectButton.setEnabled(false);

        try {
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            bitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, false);

            ByteBuffer byteBuffer = convertBitmapToByteBuffer(bitmap);

            TensorBuffer inputBuffer = TensorBuffer.createFixedSize(new int[]{1, IMAGE_SIZE, IMAGE_SIZE, 3}, DataType.FLOAT32);
            inputBuffer.loadBuffer(byteBuffer);

            PaddyLeafDiseaseModel model = PaddyLeafDiseaseModel.newInstance(getContext());
            PaddyLeafDiseaseModel.Outputs outputs = model.process(inputBuffer);
            TensorBuffer outputBuffer = outputs.getOutputFeature0AsTensorBuffer();
            float[] output = outputBuffer.getFloatArray();
            model.close();

            int maxIndex = getMaxConfidenceIndex(output);
            String[] diseaseNames = {"Bacterial Leaf Blight", "Bacterial Leaf Streak", "Bacterial Panicle Blight",
                    "Blast", "Brown Spot", "Dead Heart", "Downy Mildew", "Hispa", "Normal", "Tungro"};
            detectedDisease = diseaseNames[maxIndex];
            detectedConfidence = output[maxIndex];

            // Save the detection history
            saveDetectionHistory();

            // Update UI with detection results
            result.setText(getString(R.string.disease_detected, detectedDisease) + "\n" +
                    getString(R.string.confidence, detectedConfidence * 100));
        } catch (IOException e) {
            Toast.makeText(getContext(), getString(R.string.error_detecting_disease, e.getMessage()), Toast.LENGTH_SHORT).show();
        } finally {
            progressBar.setVisibility(View.GONE);
            fabDetectButton.setEnabled(true);
        }
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int pixelValue : intValues) {
            byteBuffer.putFloat(((pixelValue >> 16) & 0xFF) / 255.0f);
            byteBuffer.putFloat(((pixelValue >> 8) & 0xFF) / 255.0f);
            byteBuffer.putFloat((pixelValue & 0xFF) / 255.0f);
        }
        return byteBuffer;
    }

    private int getMaxConfidenceIndex(float[] output) {
        int maxIndex = 0;
        for (int i = 1; i < output.length; i++) {
            if (output[i] > output[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private void saveDetectionHistory() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> historyData = new HashMap<>();
        historyData.put("disease", detectedDisease);
        historyData.put("timestamp", new Date());
        historyData.put("confidence", detectedConfidence);
        historyData.put("imageUrl", photoURI != null ? photoURI.toString() : null);

        firestore.collection("users").document(userId).collection("detectionHistory")
                .add(historyData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), getString(R.string.detection_saved), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.save_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getContext(), getString(R.string.error_creating_image_file, ex.getMessage()), Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(requireContext(),
                        "com.example.paddyleafdiseasedetection.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.no_camera_found), Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageView.setImageURI(photoURI);
        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            photoURI = data.getData();
            imageView.setImageURI(photoURI);
        }
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void openAdditionalDetailsFragment() {
        AdditionalDetailsFragment fragment = new AdditionalDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("disease_type", detectedDisease);
        bundle.putFloat("confidence", detectedConfidence);
        bundle.putString("image_url", photoURI != null ? photoURI.toString() : null);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = requireFragmentManager().beginTransaction();
        transaction.replace(R.id.frameView, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
