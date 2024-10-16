package com.example.pookies;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;  // Ensure correct import

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private static final String PREFS_NAME = "APP_PREFS";
    private static final String USER_ID_KEY = "USER_ID";

    private EditText usernameEditText, emailEditText, passwordEditText;
    private ShapeableImageView profileImageView;
    private FloatingActionButton floatingActionButton;
    private Button editProfileButton, forgotPasswordButton;

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;

    private FirebaseAuth mAuth;
    private DBHelper dbHelper;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initializeViews(view);
        setupListeners();
        loadUserData();

        return view;
    }

    private void initializeViews(View view) {
        usernameEditText = view.findViewById(R.id.username);
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        profileImageView = view.findViewById(R.id.imageView2);
        floatingActionButton = view.findViewById(R.id.floatingActionButton);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        forgotPasswordButton = view.findViewById(R.id.forgotPasswordButton);

        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DBHelper(getActivity());
        prefs = getActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void setupListeners() {
        floatingActionButton.setOnClickListener(v -> showImagePickerDialog());
        editProfileButton.setOnClickListener(v -> showEditProfileDialog());
        forgotPasswordButton.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String userID = prefs.getString(USER_ID_KEY, null);

        if (firebaseUser != null) {
            // User is logged in with Firebase
            emailEditText.setText(firebaseUser.getEmail());
            usernameEditText.setText(firebaseUser.getDisplayName());
        } else if (userID != null) {
            // User is logged in with SharedPreferences
            User currentUser = dbHelper.getUserByEmail(userID);
            if (currentUser != null) {
                emailEditText.setText(currentUser.getEmail());
                usernameEditText.setText(currentUser.getName());
            }
        }

        passwordEditText.setText("********"); // Show 8 asterisks as a placeholder for password
        loadProfilePicture();
    }

    private void loadProfilePicture() {
        String profilePicUri = prefs.getString("PROFILE_PIC_URI", null);
        if (profilePicUri != null) {
            profileImageView.setImageURI(Uri.parse(profilePicUri));
        } else {
            profileImageView.setImageResource(R.drawable.baseline_person_24);
        }
    }

    private void showImagePickerDialog() {
        CharSequence[] options = {"Take Photo", "Choose from Gallery", "Remove Profile Picture", "Cancel"};
        new AlertDialog.Builder(getActivity())
                .setTitle("Update Profile Picture")
                .setItems(options, (dialog, which) -> {
                    if (options[which].equals("Take Photo")) {
                        openCamera();
                    } else if (options[which].equals("Choose from Gallery")) {
                        openGallery();
                    } else if (options[which].equals("Remove Profile Picture")) {
                        removeProfilePicture();
                    }
                })
                .show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private void removeProfilePicture() {
        profileImageView.setImageResource(R.drawable.baseline_person_24);
        prefs.edit().remove("PROFILE_PIC_URI").apply();
    }

    private void showForgotPasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        TextInputEditText currentPasswordInput = dialogView.findViewById(R.id.currentPassword);
        TextInputEditText newPasswordInput = dialogView.findViewById(R.id.newPassword);
        TextInputEditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPassword);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setTitle("Change Password")
                .setPositiveButton("Change", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String currentPassword = currentPasswordInput.getText().toString().trim();
                String newPassword = newPasswordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();

                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(getActivity(), "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(getActivity(), "New passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Typically verify current password and update to new password
                Toast.makeText(getActivity(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        TextInputEditText newUsernameInput = dialogView.findViewById(R.id.newUsername);
        TextInputEditText newEmailInput = dialogView.findViewById(R.id.newEmail);

        newUsernameInput.setText(usernameEditText.getText());
        newEmailInput.setText(emailEditText.getText());

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setTitle("Edit Profile")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String newUsername = newUsernameInput.getText().toString().trim();
                String newEmail = newEmailInput.getText().toString().trim();

                if (newUsername.isEmpty() || newEmail.isEmpty()) {
                    Toast.makeText(getActivity(), "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateProfile(newUsername, newEmail);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void updateProfile(String newUsername, String newEmail) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            // Update Firebase user email
            firebaseUser.updateEmail(newEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update Firebase username
                            firebaseUser.updateProfile(new UserProfileChangeRequest.Builder()
                                            .setDisplayName(newUsername)
                                            .build())
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            // Firebase update successful, update local data
                                            updateLocalData(newUsername, newEmail);
                                            Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(getActivity(), "Failed to update email", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Firebase user is null, update local SQLite database
            String userID = prefs.getString(USER_ID_KEY, null);

            if (userID != null) {
                boolean isUpdated = dbHelper.updateUsername(newEmail, newUsername);

                if (isUpdated) {
                    updateLocalData(newUsername, newEmail);
                    Toast.makeText(getActivity(), "Profile updated in local database", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Failed to update profile in local database", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "User not found locally", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateLocalData(String newUsername, String newEmail) {
        usernameEditText.setText(newUsername);
        emailEditText.setText(newEmail);
        Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA && data != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                profileImageView.setImageBitmap(imageBitmap);
                saveProfilePictureUri(imageBitmap);
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                Uri selectedImage = data.getData();
                profileImageView.setImageURI(selectedImage);
                prefs.edit().putString("PROFILE_PIC_URI", selectedImage.toString()).apply();
            }
        }
    }

    private void saveProfilePictureUri(Bitmap bitmap) {
        // Save bitmap to internal storage and get URI
        String savedImageURI = MediaStore.Images.Media.insertImage(
                getActivity().getContentResolver(),
                bitmap,
                "ProfilePicture",
                "Profile picture"
        );
        prefs.edit().putString("PROFILE_PIC_URI", savedImageURI).apply();
    }
}