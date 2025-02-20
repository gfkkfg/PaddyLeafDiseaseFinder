package com.example.paddyleafdiseasedetection;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Date;

public class HistoryFragment extends Fragment {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.history_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HistoryAdapter();
        recyclerView.setAdapter(adapter);

        loadHistoryData();
        return view;
    }

    private void loadHistoryData() {
        String userId = auth.getCurrentUser().getUid();
        firestore.collection("users").document(userId).collection("detectionHistory")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String disease = document.getString("disease");
                            Date timestamp = document.getDate("timestamp");
                            float confidence = document.contains("confidence") ? document.getDouble("confidence").floatValue() : 0f;
                            String imageUrl = document.getString("imageUrl");
                            String location = document.getString("location");

                            HistoryItem item = new HistoryItem(disease, timestamp, confidence, imageUrl, location);
                            adapter.addHistoryItem(item);
                        }
                    } else {
                        // Use string resource for failure message
                        Toast.makeText(getContext(), getString(R.string.failed_to_load_history), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
