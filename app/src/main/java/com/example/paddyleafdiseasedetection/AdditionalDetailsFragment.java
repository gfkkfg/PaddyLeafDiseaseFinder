package com.example.paddyleafdiseasedetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AdditionalDetailsFragment extends Fragment {

    private Map<String, DiseaseDetails> diseaseMap;
    private Button sharePDF;
    private String diseaseType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_additional_details, container, false);

        // Initialize the disease details map
        initializeDiseaseMap();

        // Bind UI elements
        TextView detailsText = view.findViewById(R.id.details_text);
        TextView symptomsText = view.findViewById(R.id.symptoms_text);
        TextView causesText = view.findViewById(R.id.causes_text);
        TextView managementText = view.findViewById(R.id.management_text);
        TextView preventionText = view.findViewById(R.id.prevention_text);
        sharePDF = view.findViewById(R.id.sharePDF);

        TextView diseaseName = view.findViewById(R.id.disease_name);
        TextView confidenceScore = view.findViewById(R.id.confidence_score);
        ImageView imagePreview = view.findViewById(R.id.image_preview);

        // Retrieve arguments passed to this fragment
        Bundle args = getArguments();
        if (args != null) {
            diseaseType = args.getString("disease_type");
            float confidence = args.getFloat("confidence", 0);
            String imageUrl = args.getString("image_url");

            diseaseName.setText(diseaseType);
            confidenceScore.setText("Confidence: " + String.format("%.2f%%", confidence * 100));

            if (imageUrl != null) {
                imagePreview.setImageURI(Uri.parse(imageUrl));
            } else {
                imagePreview.setImageResource(R.drawable.ic_placeholder); // Add a default placeholder image
            }

            // Retrieve and display disease details
            DiseaseDetails diseaseDetails = diseaseMap.get(diseaseType);
            if (diseaseDetails != null) {
                detailsText.setText(diseaseDetails.details);
                symptomsText.setText(diseaseDetails.symptoms);
                causesText.setText(diseaseDetails.causes);
                managementText.setText(diseaseDetails.management);
                preventionText.setText(diseaseDetails.prevention);

                sharePDF.setOnClickListener(v -> SharePDF());
            } else {
                detailsText.setText(R.string.details_not_available);
                symptomsText.setText("");
                causesText.setText("");
                managementText.setText("");
                preventionText.setText("");
            }
        } else {
            Log.e("AdditionalDetails", "No arguments provided to the fragment");
        }

        return view;
    }

    private void SharePDF() {
        // Ensure the correct UI elements are accessed
        TextView detailsText = requireView().findViewById(R.id.details_text);
        TextView symptomsText = requireView().findViewById(R.id.symptoms_text);
        TextView causesText = requireView().findViewById(R.id.causes_text);
        TextView managementText = requireView().findViewById(R.id.management_text);
        TextView preventionText = requireView().findViewById(R.id.prevention_text);
        TextView confidenceScoreView = requireView().findViewById(R.id.confidence_score); // Fetch confidence_score TextView

        // Retrieve values from the UI
        String details = detailsText.getText().toString();
        String symptoms = symptomsText.getText().toString();
        String causes = causesText.getText().toString();
        String management = managementText.getText().toString();
        String prevention = preventionText.getText().toString();
        String confidenceScoreText = confidenceScoreView.getText().toString()
                .replace("Confidence: ", "").replace("%", ""); // Extract confidence value
        float confidenceScore = Float.parseFloat(confidenceScoreText) / 100;

        // Retrieve image bitmap from ImageView
        ImageView imagePreview = requireView().findViewById(R.id.image_preview);
        Bitmap leafImage = ((BitmapDrawable) imagePreview.getDrawable()).getBitmap();

        // Generate the PDF
        String reportFileName = "PaddyLeafDiseaseReport_" + System.currentTimeMillis() + ".pdf";
        PDFGenerator.generateReport(
                requireContext(),
                diseaseType,
                confidenceScore,
                leafImage,
                reportFileName,
                details,
                symptoms,
                causes,
                management,
                prevention
        );

        // Share the PDF file
        File pdfFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "DiseaseReports/" + reportFileName);
        if (!pdfFile.exists()) {
            Log.e("PDF_PATH", "PDF file does not exist");
            return;
        }

        Uri pdfUri = FileProvider.getUriForFile(requireContext(), "com.example.paddyleafdiseasedetection.fileprovider", pdfFile);

        // Create an Intent for sharing the PDF
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Share PDF"));
    }



    // Initialize disease details
    private void initializeDiseaseMap() {
        diseaseMap = new HashMap<>();

        diseaseMap.put("Bacterial Leaf Blight", new DiseaseDetails(
                getString(R.string.blb_details),
                getString(R.string.blb_symptoms),
                getString(R.string.blb_causes),
                getString(R.string.blb_management),
                getString(R.string.blb_prevention)
        ));

        diseaseMap.put("Bacterial Leaf Streak", new DiseaseDetails(
                getString(R.string.bls_details),
                getString(R.string.bls_symptoms),
                getString(R.string.bls_causes),
                getString(R.string.bls_management),
                getString(R.string.bls_prevention)
        ));

        diseaseMap.put("Bacterial Panicle Blight", new DiseaseDetails(
                getString(R.string.bpb_details),
                getString(R.string.bpb_symptoms),
                getString(R.string.bpb_causes),
                getString(R.string.bpb_management),
                getString(R.string.bpb_prevention)
        ));

        diseaseMap.put("Blast", new DiseaseDetails(
                getString(R.string.blast_details),
                getString(R.string.blast_symptoms),
                getString(R.string.blast_causes),
                getString(R.string.blast_management),
                getString(R.string.blast_prevention)
        ));

        diseaseMap.put("Brown Spot", new DiseaseDetails(
                getString(R.string.brown_spot_details),
                getString(R.string.brown_spot_symptoms),
                getString(R.string.brown_spot_causes),
                getString(R.string.brown_spot_management),
                getString(R.string.brown_spot_prevention)
        ));

        diseaseMap.put("Dead Heart", new DiseaseDetails(
                getString(R.string.dead_heart_details),
                getString(R.string.dead_heart_symptoms),
                getString(R.string.dead_heart_causes),
                getString(R.string.dead_heart_management),
                getString(R.string.dead_heart_prevention)
        ));

        diseaseMap.put("Downy Mildew", new DiseaseDetails(
                getString(R.string.downy_mildew_details),
                getString(R.string.downy_mildew_symptoms),
                getString(R.string.downy_mildew_causes),
                getString(R.string.downy_mildew_management),
                getString(R.string.downy_mildew_prevention)
        ));

        diseaseMap.put("Hispa", new DiseaseDetails(
                getString(R.string.hispa_details),
                getString(R.string.hispa_symptoms),
                getString(R.string.hispa_causes),
                getString(R.string.hispa_management),
                getString(R.string.hispa_prevention)
        ));

        diseaseMap.put("Normal", new DiseaseDetails(
                getString(R.string.normal_leaf_disease_details),
                getString(R.string.normal_leaf_disease_symptoms),
                getString(R.string.normal_leaf_disease_causes),
                getString(R.string.normal_leaf_disease_management),
                getString(R.string.normal_leaf_disease_prevention)
        ));

        diseaseMap.put("Tungro", new DiseaseDetails(
                getString(R.string.tungro_details),
                getString(R.string.tungro_symptoms),
                getString(R.string.tungro_causes),
                getString(R.string.tungro_management),
                getString(R.string.tungro_prevention)
        ));
    }

    // Inner class for storing disease details
    private static class DiseaseDetails {
        String details;
        String symptoms;
        String causes;
        String management;
        String prevention;

        DiseaseDetails(String details, String symptoms, String causes, String management, String prevention) {
            this.details = details;
            this.symptoms = symptoms;
            this.causes = causes;
            this.management = management;
            this.prevention = prevention;
        }
    }
}