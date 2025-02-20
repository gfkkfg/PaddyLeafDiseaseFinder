package com.example.paddyleafdiseasedetection;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HelpFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        TextView appInfoTextView = view.findViewById(R.id.app_info_text);
        appInfoTextView.setText(getString(R.string.app_info));

        TextView appGuidanceTextView = view.findViewById(R.id.app_guidance_text);
        appGuidanceTextView.setText(getString(R.string.app_guidance));

        return view;
    }
}