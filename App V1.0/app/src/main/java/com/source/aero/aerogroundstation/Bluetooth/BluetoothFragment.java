package com.source.aero.aerogroundstation.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.source.aero.aerogroundstation.R;

import java.util.List;

public class BluetoothFragment extends Fragment {
    private static final String TAG = "BluetoothFragment";

    //Request Codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    //UI Elements
    private ListView logView;
    private EditText editTextView;
    private Button sendButton;
    private ArrayAdapter<String> logArrayAdapter;
    private TextView.OnEditorActionListener writeListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            if (i == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                String data = textView.getText().toString();
                send(data);
            }
            return true;
        }
    };

    //Bluetooth elements
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothService bluetoothService;
    private StringBuffer dataBuffer;
    private int discoveryTime = 300;
    private String connectedDevice = null;

    public BluetoothFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Report that fragment has menu options to add to menu
        setHasOptionsMenu(true);

        //Get local bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bluetooth, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        //Request for bluetooth to be enabled
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else if (bluetoothAdapter == null) {
            setup();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothService != null) {
            bluetoothService.stop();
        }
    }

    //Will start up bluetooth service when enable activity returns
    @Override
    public void onResume() {
        super.onResume();
        if (bluetoothService != null) {
            if (bluetoothService.getState() == BluetoothService.STATE_NONE) {
                bluetoothService.start();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_bluetoothmain, menu);
    }

    //Initialize UI Elements
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        logView = (ListView) view.findViewById(R.id.bluetooth_messageView);
        editTextView = (EditText) view.findViewById(R.id.bluetooth_sendMsgEditTextView);
        sendButton = (Button) view.findViewById(R.id.bluetooth_sendMsgButton);
    }

    //Sets onclick behavior for menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secureConnectOption: {
                Intent intent = new Intent(getActivity(), BluetoothDevices.class);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecureConnectOption: {
                Intent intent = new Intent(getActivity(), BluetoothDevices.class);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.makeDiscoverableOption: {
                makeDiscoverable();
                return true;
            }
        }
        return false;
    }

    private void setup() {
        logArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.activity_bluetoothlog);
        logView.setAdapter(logArrayAdapter);

        editTextView.setOnEditorActionListener(writeListener);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                View view = getView();
                if (null != view) {
                    TextView textView = (TextView) view.findViewById(R.id.bluetooth_sendMsgEditTextView);
                    String data = textView.getText().toString();
                    send(data);
                }
            }
        });

        //Initialize bluetooth connections
        bluetoothService = new BluetoothService(getActivity(), handler);
        dataBuffer = new StringBuffer("");
    }

    private void makeDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, discoveryTime);
            startActivity(intent);
        }
    }

    //Send data
    private void send(String data) {
        //Check device is connected
        if (bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.bluetooth_notConnectedToast, Toast.LENGTH_SHORT).show();
            return;
        }

        if (data.length() > 0) {
            byte[] send = data.getBytes();
            bluetoothService.write(send);

            dataBuffer.setLength(0);
            editTextView.setText(dataBuffer);
        }
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch(msg.what) {
                case BluetoothConstantsInterface.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            //setStatus(getString(R.string.bluetooth_titleConnectedTo, connectedDevice));
                            logArrayAdapter.clear();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            //setStatus(R.string.bluetooth_titleConnectedTo);
                            break;
                        case BluetoothService.STATE_LISTENING:
                        case BluetoothService.STATE_NONE:
                            //setStatus(R.string.bluetooth_titleNotConnectedTo);
                            break;
                    }
                    break;
                case BluetoothConstantsInterface.MESSAGE_WRITE:
                    byte[] writeBuffer = (byte[]) msg.obj;
                    String writeData = new String(writeBuffer);
                    logArrayAdapter.add("Me: " + writeData);
                    break;
                case BluetoothConstantsInterface.MESSAGE_READ:
                    byte[] readBuffer = (byte[]) msg.obj;
                    String readData = new String(readBuffer, 0, msg.arg1);
                    logArrayAdapter.add(connectedDevice + ": " + readData);
                    break;
                case BluetoothConstantsInterface.MESSAGE_DEVICE_NAME:
                    connectedDevice = msg.getData().getString(BluetoothConstantsInterface.DEVICE_NAME);
                    if (activity != null) {
                        Toast.makeText(activity, "Connected to " + connectedDevice, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BluetoothConstantsInterface.MESSAGE_TOAST:
                    if (activity != null) {
                        Toast.makeText(activity, msg.getData().getString(BluetoothConstantsInterface.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setup();
                }
                else {
                    Toast.makeText(getActivity(), R.string.bluetooth_btNotEnabledToast, Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(BluetoothDevices.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        bluetoothService.connect(device, secure);
    }
}
