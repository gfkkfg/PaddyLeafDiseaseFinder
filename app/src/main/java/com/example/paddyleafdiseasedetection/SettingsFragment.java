package com.example.paddyleafdiseasedetection;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment (ensure it is the settings layout)
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Button btnEnglish = view.findViewById(R.id.btn_english);
        Button btnTamil = view.findViewById(R.id.btn_tamil);
        Button btnHindi = view.findViewById(R.id.btn_hindi);

        // Use string resources for button labels
        btnEnglish.setText(getString(R.string.language_english));
        btnTamil.setText(getString(R.string.language_tamil));
        btnHindi.setText(getString(R.string.language_hindi));

        btnEnglish.setOnClickListener(v -> setAppLocale("en"));
        btnTamil.setOnClickListener(v -> setAppLocale("ta"));
        btnHindi.setOnClickListener(v -> setAppLocale("hi"));

        return view;
    }

    // Method to notify activity about the locale change
    public void setAppLocale(String localeCode) {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("Settings", getActivity().MODE_PRIVATE).edit();
        editor.putString("My_Lang", localeCode);
        editor.apply();

        // Notify the activity to reload the locale
        if (getActivity() != null) {
            ((MainActivity) getActivity()).onLocaleChanged(localeCode);
        }
    }
}
