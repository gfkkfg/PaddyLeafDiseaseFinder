package com.example.paddyleafdiseasedetection;

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

import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        loginButton = view.findViewById(R.id.login_button);
        TextView signupLink = view.findViewById(R.id.signup_link);

        mAuth = FirebaseAuth.getInstance();

        // Login Button Click Listener
        loginButton.setOnClickListener(v -> loginUser());

        // Signup Link Click Listener
        signupLink.setOnClickListener(v -> loadFragment(new SignupFragment()));

        return view;
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_email_required));
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_password_required));
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
                        // After successful login, load the home fragment
                        loadFragment(new HomeFragment());
                    } else {
                        Toast.makeText(getContext(), getString(R.string.login_failed) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
