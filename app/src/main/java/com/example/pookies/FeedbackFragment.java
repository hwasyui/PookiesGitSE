package com.example.pookies;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FeedbackFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private RadioGroup radioGroup;
    private EditText editTextFeedback;
    private TextView errorTextView;
    private Button submitButton;
    private DBHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DBHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        radioGroup = view.findViewById(R.id.feedbackType);
        editTextFeedback = view.findViewById(R.id.feedbackText);
        errorTextView = view.findViewById(R.id.errorTextView);
        submitButton = view.findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFeedback();
            }
        });

        return view;
    }

    private void submitFeedback() {
        int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();

        if (selectedRadioButtonId == -1) {
            errorTextView.setVisibility(View.VISIBLE);
            errorTextView.setText("Please select a feedback type.");
            return;
        }

        String feedbackDescription = editTextFeedback.getText().toString().trim();
        if (feedbackDescription.split("\\s+").length < 5) {
            errorTextView.setVisibility(View.VISIBLE);
            errorTextView.setText("Please put at least 5 words.");
            return;
        }

        errorTextView.setVisibility(View.GONE);

        RadioButton selectedRadioButton = radioGroup.findViewById(selectedRadioButtonId);
        String feedbackType = selectedRadioButton.getText().toString();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String username = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous";
            String email = currentUser.getEmail() != null ? currentUser.getEmail() : "No Email";
            String currentTime = getCurrentTime();

            Feedback feedback = new Feedback(
                    userId,
                    username,
                    email,
                    feedbackType,
                    feedbackDescription,
                    currentTime
            );

            // Save to Firebase
            mDatabase.child("feedback").child(userId).push().setValue(feedback)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Save to SQLite
                            saveFeedbackToSQLite(feedback);
                            Toast.makeText(getContext(), "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                            clearForm();
                        } else {
                            Toast.makeText(getContext(), "Failed to submit feedback to Firebase. Saving locally.", Toast.LENGTH_SHORT).show();
                            saveFeedbackToSQLite(feedback);
                        }
                    });
        }
    }

    private void saveFeedbackToSQLite(Feedback feedback) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userId", feedback.getUserId());
        values.put("username", feedback.getUsername());
        values.put("email", feedback.getEmail());
        values.put("feedbackType", feedback.getFeedbackType());
        values.put("description", feedback.getDescription());
        values.put("feedbackTime", feedback.getFeedbackTime());

        long newRowId = db.insert("feedback", null, values);
        if (newRowId == -1) {
            Toast.makeText(getContext(), "Error saving feedback locally", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Feedback saved locally", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void clearForm() {
        radioGroup.clearCheck();
        editTextFeedback.setText("");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}