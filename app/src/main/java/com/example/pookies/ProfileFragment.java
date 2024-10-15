package com.example.pookies;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.Manifest.permission.CAMERA;

public class ProfileFragment extends Fragment {

    private EditText usernameEditText, emailEditText, passwordEditText;
    private ShapeableImageView profileImageView;
    private FloatingActionButton floatingActionButton;

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 102;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize the EditText fields
        usernameEditText = view.findViewById(R.id.username);
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);  // For showing password as asterisks

        // Initialize the ShapeableImageView and FloatingActionButton
        profileImageView = view.findViewById(R.id.imageView2);
        floatingActionButton = view.findViewById(R.id.floatingActionButton);

        // Fetch the current user from Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // If the user is logged in, set their email, username, and dummy password
        if (currentUser != null) {
            // Set the email
            emailEditText.setText(currentUser.getEmail());

            // Set the display name (username)
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                usernameEditText.setText(displayName);
            } else {
                // If display name is not set, show a default placeholder
                usernameEditText.setHint("No username set");
            }

            // Set a dummy password as asterisks (cannot retrieve the actual password)
            passwordEditText.setText("****"); // Show 8 asterisks as a placeholder for password
        }

        // Set click listener for the FloatingActionButton to pick image
        floatingActionButton.setOnClickListener(v -> {
            CharSequence[] options = {"Take Photo", "Choose from Gallery", "Remove Profile Picture", "Cancel"};
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
            builder.setTitle("Update Profile Picture");
            builder.setItems(options, (dialog, which) -> {
                if (options[which].equals("Take Photo")) {
                    if (ContextCompat.checkSelfPermission(getActivity(), CAMERA) != PERMISSION_GRANTED) {
                        requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA_PERMISSION);
                    } else {
                        openCamera();
                    }
                } else if (options[which].equals("Choose from Gallery")) {
                    openGallery();
                } else if (options[which].equals("Remove Profile Picture")) {
                    removeProfilePicture();  // Call method to reset the profile picture
                } else {
                    dialog.dismiss();
                }
            });
            builder.show();
        });

        return view;
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
        // Reset the image view to a default placeholder image
        profileImageView.setImageResource(R.drawable.baseline_person_24);  // Set this to your default profile image drawable
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA && data != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                profileImageView.setImageBitmap(imageBitmap);
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                Uri selectedImage = data.getData();
                profileImageView.setImageURI(selectedImage);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getActivity(), "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show();
            }
        }
    }
}