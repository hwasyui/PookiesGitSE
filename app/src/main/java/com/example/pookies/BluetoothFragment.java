package com.example.pookies;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothFragment extends Fragment {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 2;

    private BluetoothAdapter bluetoothAdapter;
    private ListView devicesListView;
    private ArrayAdapter<String> devicesArrayAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothSocket bluetoothSocket;

    private DatabaseReference mDatabase; // Firebase database reference
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard UUID for SPP (Serial Port Profile)

    // Parameters
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public BluetoothFragment() {
        // Required empty public constructor
    }

    public static BluetoothFragment newInstance(String param1, String param2) {
        BluetoothFragment fragment = new BluetoothFragment();
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

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        checkBluetoothPermissions();
    }

    private void checkBluetoothPermissions() {
        String[] permissions = {
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        boolean permissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsGranted = false;
                break;
            }
        }

        if (!permissionsGranted) {
            ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        devicesListView = view.findViewById(R.id.devices_list_view);
        Button searchButton = view.findViewById(R.id.search_btn);

        devicesArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        devicesListView.setAdapter(devicesArrayAdapter);

        searchButton.setOnClickListener(v -> listPairedDevices());

        devicesListView.setOnItemClickListener((parent, view1, position, id) -> {
            String deviceName = devicesArrayAdapter.getItem(position);
            if (deviceName != null) {
                connectToDevice(deviceName);
            }
        });

        return view;
    }

    private void listPairedDevices() {
        try {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            }
            pairedDevices = bluetoothAdapter.getBondedDevices();
            devicesArrayAdapter.clear();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    // Ensure permission for device.getName() is explicitly checked
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSIONS);
                        return;
                    }
                    devicesArrayAdapter.add(device.getName());
                }
            } else {
                Toast.makeText(getContext(), "No paired devices found", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            Log.e("BluetoothFragment", "Bluetooth permission denied", e);
            Toast.makeText(getContext(), "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void connectToDevice(String deviceName) {
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(deviceName)) {
                try {
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSIONS);
                        return;
                    }

                    // Create an RFCOMM socket using the UUID for SPP
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    bluetoothSocket.connect(); // Connect to the device's Bluetooth socket

                    Toast.makeText(getContext(), "Connected to " + deviceName, Toast.LENGTH_SHORT).show();
                    transferChatHistory(device); // Transfer chat history on connection
                } catch (IOException e) {
                    Log.e("BluetoothFragment", "Error connecting to device", e);
                    Toast.makeText(getContext(), "Connection failed", Toast.LENGTH_SHORT).show();
                } catch (SecurityException se) {
                    Log.e("BluetoothFragment", "Permission denied", se);
                    Toast.makeText(getContext(), "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void transferChatHistory(BluetoothDevice device) {
        try {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            }

            // Retrieve chat history from Firebase
            mDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("messages")
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String chatHistory = task.getResult().getValue().toString();

                            try (OutputStream outputStream = bluetoothSocket.getOutputStream()) {
                                outputStream.write(chatHistory.getBytes());
                                Toast.makeText(getContext(), "Chat history sent to " + device.getName(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Log.e("BluetoothFragment", "Error sending chat history", e);
                                Toast.makeText(getContext(), "Failed to send chat history", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to retrieve chat history", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (SecurityException se) {
            Log.e("BluetoothFragment", "Permission denied", se);
            Toast.makeText(getContext(), "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, proceed with Bluetooth operations
                listPairedDevices();
            } else {
                Toast.makeText(getContext(), "Bluetooth permissions are required for this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }
}