package com.example.paddyleafdiseasedetection;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;

public class SignupFragment extends Fragment implements View.OnClickListener {

    private String name, dob, email, mobile_no, password;
    private EditText etName, etDob, etEmail, etMobile, etPassword;
    private Button btnSignup;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        etName = view.findViewById(R.id.nameField);
        etDob = view.findViewById(R.id.dobField);
        etEmail = view.findViewById(R.id.emailField);
        etMobile = view.findViewById(R.id.mobileField);
        etPassword = view.findViewById(R.id.passwordField);
        btnSignup = view.findViewById(R.id.signupButton);
        TextView loginLink = view.findViewById(R.id.loginLink);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Handle Date of Birth field
        etDob.setOnClickListener(v -> showDatePickerDialog());

        // Login Link Click Listener
        loginLink.setOnClickListener(v -> loadFragment(new LoginFragment()));

        btnSignup.setOnClickListener(this);

        return view;
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                (view, year, month, dayOfMonth) -> etDob.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private boolean validateInputs() {
        name = etName.getText().toString().trim();
        dob = etDob.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        mobile_no = etMobile.getText().toString().trim();
        password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.error_name_required));
            return false;
        } else if (TextUtils.isEmpty(dob)) {
            etDob.setError(getString(R.string.error_dob_required));
            return false;
        } else if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_invalid_email));
            return false;
        } else if (TextUtils.isEmpty(mobile_no)) {
            etMobile.setError(getString(R.string.error_mobile_required));
            return false;
        } else if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_password_required));
            return false;
        }
        return true;
    }

    private void signup() {
        if (validateInputs()) {
            // Create Firebase Authentication User
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // User creation successful, add additional data to Firestore
                            addUserDataToFirestore();
                        } else {
                            Toast.makeText(getContext(), getString(R.string.error_signup_failed) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void addUserDataToFirestore() {
        // Create user data to be stored in Firestore
        HashMap<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("dob", dob);
        user.put("email", email);
        user.put("mobile_no", mobile_no);

        // Store additional user data in Firestore
        db.collection("users")
                .document(mAuth.getCurrentUser().getUid()) // Use the user's UID as the document ID
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), getString(R.string.success_signup), Toast.LENGTH_SHORT).show();
                    // After successful signup and data storage, navigate to another fragment
                    loadFragment(new HomeFragment());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), getString(R.string.error_saving_data) + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onClick(View v) {
        if (v == btnSignup) {
            signup();
        }
    }

    // Load a fragment into the fragment container
    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = requireFragmentManager().beginTransaction();
            transaction.replace(R.id.frameView, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}
