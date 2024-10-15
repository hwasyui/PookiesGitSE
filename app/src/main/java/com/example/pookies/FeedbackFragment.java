package com.example.pookies;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FeedbackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedbackFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private RadioGroup radioGroup;
    private EditText editTextFeedback;
    private TextView errorTextView;
    private Button submitButton;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FeedbackFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FeedbackFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeedbackFragment newInstance(String param1, String param2) {
        FeedbackFragment fragment = new FeedbackFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        radioGroup = view.findViewById(R.id.feedbackType); // RadioGroup for feedback type
        editTextFeedback = view.findViewById(R.id.feedbackText); // EditText for feedback description
        errorTextView = view.findViewById(R.id.errorTextView); // TextView to display error messages
        submitButton = view.findViewById(R.id.submitButton); // Button for sending feedback

        // Handle form submission on button click
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFeedback();
            }
        });

        return view;
    }

    // Function to submit feedback to Firebase
    private void submitFeedback() {
        // Get the selected radio button
        int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();

        // Check if a radio button is selected
        if (selectedRadioButtonId == -1) {
            errorTextView.setVisibility(View.VISIBLE);
            errorTextView.setText("Please select a feedback type.");
            return;
        }

        // Get feedback description and validate word count (minimum 5 words)
        String feedbackDescription = editTextFeedback.getText().toString().trim();
        if (feedbackDescription.split("\\s+").length < 5) {
            errorTextView.setVisibility(View.VISIBLE);
            errorTextView.setText("Please put at least 5 words.");
            return;
        }

        // Hide error message if validation passes
        errorTextView.setVisibility(View.GONE);

        // Get selected feedback type
        RadioButton selectedRadioButton = radioGroup.findViewById(selectedRadioButtonId);
        String feedbackType = selectedRadioButton.getText().toString();

        // Get current user information from Firebase Authentication
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String username = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous";
            String email = currentUser.getEmail() != null ? currentUser.getEmail() : "No Email";

            // Create a feedback object
            Feedback feedback = new Feedback(
                    userId,
                    username,
                    email,
                    feedbackType,
                    feedbackDescription,
                    getCurrentTime() // Timestamp
            );

            // Save feedback to Firebase Realtime Database
            mDatabase.child("feedback").child(userId).push().setValue(feedback)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                            // Optionally clear the form after submission
                            radioGroup.clearCheck();
                            editTextFeedback.setText("");
                        } else {
                            Toast.makeText(getContext(), "Failed to submit feedback. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Helper function to get current time in a readable format
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}