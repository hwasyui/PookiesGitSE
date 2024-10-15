package com.example.pookies;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothFragment extends Fragment {

    private static final String TAG = "BluetoothFragment";
    private static final UUID MY_UUID = UUID.randomUUID(); // Unique UUID for this app
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private ListView devicesListView;
    private Button sendButton, receiveButton;

    // Firebase variables
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference chatSessionsRef;

    public BluetoothFragment() {
        // Required empty public constructor
    }

    public static BluetoothFragment newInstance(String param1, String param2) {
        BluetoothFragment fragment = new BluetoothFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString("param1");
            String mParam2 = getArguments().getString("param2");
        }

        // Initialize Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth not supported on this device.", Toast.LENGTH_SHORT).show();
        }

        // Enable Bluetooth if it's not already enabled
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        // Initialize Firebase database reference
        firebaseDatabase = FirebaseDatabase.getInstance();
        chatSessionsRef = firebaseDatabase.getReference("chat_sessions");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        // Initialize views
        devicesListView = view.findViewById(R.id.devices_list_view);
        sendButton = view.findViewById(R.id.send_button);
        receiveButton = view.findViewById(R.id.receive_button);

        // Show paired devices list
        showPairedDevices();

        // Set click listeners for sending and receiving chat data
        sendButton.setOnClickListener(v -> sendChatData());
        receiveButton.setOnClickListener(v -> receiveChatData());

        return view;
    }

    private void showPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.d(TAG, "Paired Device: " + device.getName() + " - " + device.getAddress());
                // You can display this data in the ListView (devicesListView) for user selection.
            }
        } else {
            Toast.makeText(getContext(), "No paired devices found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendChatData() {
        if (bluetoothSocket == null) {
            Toast.makeText(getContext(), "No Bluetooth connection established.", Toast.LENGTH_SHORT).show();
            return;
        }

        String chatData = getChatDataFromFirebase(); // Retrieve chat session data from Firebase
        try {
            OutputStream outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(chatData.getBytes());
            Toast.makeText(getContext(), "Chat data sent via Bluetooth.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error sending chat data.", Toast.LENGTH_SHORT).show();
        }
    }

    private void receiveChatData() {
        try {
            InputStream inputStream = bluetoothSocket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            String receivedChatData = new String(buffer, 0, bytesRead);

            // Save the received chat session to Firebase
            saveChatSessionToFirebase(receivedChatData);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error receiving chat data.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getChatDataFromFirebase() {
        // Here we fetch the chat data from Firebase for sending
        final String[] chatData = {""};
        chatSessionsRef.child("user_chat_session") // Replace with actual user/chat session ID
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            chatData[0] = snapshot.getValue(String.class); // Assuming chat data is saved as String
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error fetching chat session data", error.toException());
                    }
                });
        return chatData[0]; // Return the fetched chat data (e.g., serialized JSON)
    }

    private void saveChatSessionToFirebase(String chatData) {
        // Save the received chat session data to Firebase
        String newSessionId = chatSessionsRef.push().getKey(); // Generate a new session ID
        chatSessionsRef.child(newSessionId).setValue(chatData)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Chat session saved to Firebase.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Log.e(TAG, "Error saving chat session to Firebase", e));
    }
}
