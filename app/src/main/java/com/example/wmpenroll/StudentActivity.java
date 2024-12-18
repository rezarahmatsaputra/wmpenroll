package com.example.wmpenroll;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.HashMap;

public class StudentActivity extends AppCompatActivity {

    private ListView lvAvailableSubjects, lvEnrolledSubjects;
    private TextView tvTotalCredits;
    private Button btnEnroll;

    private ArrayList<String> availableSubjects = new ArrayList<>();
    private ArrayList<String> enrolledSubjects = new ArrayList<>();
    private HashMap<String, Integer> subjectCredits = new HashMap<>();
    private int totalCredits = 0;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Subjects");

        // Initialize UI components
        lvAvailableSubjects = findViewById(R.id.lvAvailableSubjects);
        lvEnrolledSubjects = findViewById(R.id.lvEnrolledSubjects);
        tvTotalCredits = findViewById(R.id.tvTotalCredits);
        btnEnroll = findViewById(R.id.btnEnroll);

        // Adapters for ListViews
        ArrayAdapter<String> availableAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, availableSubjects);
        lvAvailableSubjects.setAdapter(availableAdapter);
        lvAvailableSubjects.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        ArrayAdapter<String> enrolledAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, enrolledSubjects);
        lvEnrolledSubjects.setAdapter(enrolledAdapter);

        // Fetch subjects from Firebase Realtime Database
        fetchAvailableSubjects(availableAdapter);

        // Enroll button logic
        btnEnroll.setOnClickListener(v -> {
            int selectedPosition = lvAvailableSubjects.getCheckedItemPosition();
            if (selectedPosition != ListView.INVALID_POSITION) {
                String selectedSubject = availableSubjects.get(selectedPosition);
                int credit = subjectCredits.get(selectedSubject);

                if (totalCredits + credit <= 24) {
                    // Add subject to enrolled list
                    enrolledSubjects.add(selectedSubject);
                    enrolledAdapter.notifyDataSetChanged();

                    // Update total credits
                    totalCredits += credit;
                    tvTotalCredits.setText("Total Credits: " + totalCredits);

                    // Remove subject from available list
                    availableSubjects.remove(selectedPosition);
                    availableAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Maximum 24 credits allowed!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Select a subject to enroll", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAvailableSubjects(ArrayAdapter<String> adapter) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                availableSubjects.clear();
                subjectCredits.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String subjectName = snapshot.child("name").getValue(String.class);
                    String creditStr = snapshot.child("credit").getValue(String.class);

                    if (subjectName != null && creditStr != null) {
                        int credit = Integer.parseInt(creditStr);
                        availableSubjects.add(subjectName);
                        subjectCredits.put(subjectName, credit);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", databaseError.getMessage());
            }
        });
    }
}
