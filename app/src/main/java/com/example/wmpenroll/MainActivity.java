package com.example.wmpenroll;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText etSubjectName, etCredit;
    private Button btnCreate;
    private ListView lvSubjects;
    private DatabaseReference databaseReference;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> subjectList;
    private HashMap<String, String> subjectKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Subjects");

        // Initialize UI components
        etSubjectName = findViewById(R.id.etSubjectName);
        etCredit = findViewById(R.id.etCredit);
        btnCreate = findViewById(R.id.btnCreate);
        lvSubjects = findViewById(R.id.lvSubjects);

        // Initialize ListView and adapter
        subjectList = new ArrayList<>();
        subjectKeys = new HashMap<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, subjectList);
        lvSubjects.setAdapter(adapter);

        // Button listeners
        btnCreate.setOnClickListener(v -> createSubject());
    }

    private void createSubject() {
        String subjectName = etSubjectName.getText().toString().trim();
        String credit = etCredit.getText().toString().trim();

        if (subjectName.isEmpty() || credit.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pastikan credit adalah angka valid
        try {
            int creditValue = Integer.parseInt(credit);
            if (creditValue <= 0) {
                Toast.makeText(this, "Credit must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Credit must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Siapkan data untuk dikirim
        String subjectId = databaseReference.push().getKey();
        Map<String, Object> subject = new HashMap<>();
        subject.put("name", subjectName);
        subject.put("credit", credit);

        // Kirim data ke Firebase
        if (subjectId != null) {
            databaseReference.child(subjectId).setValue(subject).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Subject created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to create subject", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void fetchSubjects() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                subjectList.clear();
                subjectKeys.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    String name = snapshot.child("name").getValue(String.class);
                    String credit = snapshot.child("credit").getValue(String.class);

                    if (key != null && name != null) {
                        subjectList.add(name + " - " + credit + " Credits");
                        subjectKeys.put(key, name);
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
