package com.source.aero.aerogroundstation;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.commons.codec.binary.Hex;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.source.aero.aerogroundstation.Bluetooth.BluetoothConstantsInterface;
import com.source.aero.aerogroundstation.Bluetooth.BluetoothDevices;
import com.source.aero.aerogroundstation.Bluetooth.BluetoothService;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;

    private static final String TAG = "MyActivity";

    //UI Elements
    Button startButton;
    boolean bluetoothDisplayed = false;

    //Bluetooth Elements
    //Request Codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

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

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothService bluetoothService = null;
    private StringBuffer dataBuffer;
    private int discoveryTime = 300;
    private String connectedDevice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoiYWVyb2Rlc2lnbiIsImEiOiJjam9sczI0bjMwM3E4M2twMXk0NG93YXg1In0.jYhWqqiBnn4O4KrLImf-Gg");
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        startButton = (Button) findViewById(R.id.testButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                if (!bluetoothDisplayed) {
                    ((LinearLayout) findViewById(R.id.bluetoothView)).setVisibility(View.VISIBLE);
                    bluetoothDisplayed = true;
                } else {
                    ((LinearLayout) findViewById(R.id.bluetoothView)).setVisibility(View.INVISIBLE);
                    bluetoothDisplayed = false;
                }
            }
        });

        //Bluetooth Setup
        //Get local bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        logView = (ListView) findViewById(R.id.bluetooth_messageView);
        editTextView = (EditText) findViewById(R.id.bluetooth_sendMsgEditTextView);
        sendButton = (Button) findViewById(R.id.bluetooth_sendMsgButton);
    }

    //Make bluetooth menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bluetoothmain, menu);
        return true;
    }

    //Bluetooth options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secureConnectOption: {
                Intent intent = new Intent(this, BluetoothDevices.class);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecureConnectOption: {
                Intent intent = new Intent(this, BluetoothDevices.class);
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


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        //Bluetooth
        //Request for bluetooth to be enabled
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else if (bluetoothService == null) {
            setup();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        //Bluetooth
        if (bluetoothService != null) {
            if (bluetoothService.getState() == BluetoothService.STATE_NONE) {
                bluetoothService.start();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

        //Bluetooth
        if (bluetoothService != null) {
            bluetoothService.stop();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    //Bluetooth Functions
    private void setup() {
        logArrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_bluetoothlog);
        logView.setAdapter(logArrayAdapter);

        editTextView.setOnEditorActionListener(writeListener);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                if (null != view) {
                    TextView textView = (TextView) findViewById(R.id.bluetooth_sendMsgEditTextView);
                    String data = textView.getText().toString();
                    send(data);
                }
            }
        });

        //Initialize bluetooth connections
        bluetoothService = new BluetoothService(this, handler);
        dataBuffer = new StringBuffer("");
    }

    private void makeDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, discoveryTime);
            startActivity(intent);
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    //Send data
    private void send(String data) {
        Log.d(TAG, "Sending data...");
        
        // Lat/Lon multiplied by 1000000 to remove decimals
        // Test message with target GPS coordinates pointing to ACEB building for testing gnd station -> onboard
        ////                                  start type  lat         lon         calibrate rssi drop gliders motors
        byte[] testMessage = hexStringToByteArray("0a0000029035D0FB27D200010201020000000100020003000400050006000700080009000a000b000c000d000e000f0000ff");


        //Check device is connected
        if (bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.bluetooth_notConnectedToast, Toast.LENGTH_SHORT).show();
            return;
        }

        if (data.length() > 0) {
            byte[] send = data.getBytes();

            //bluetoothService.write(send);
            bluetoothService.write(testMessage);

            dataBuffer.setLength(0);
            editTextView.setText(dataBuffer);
        }
    }
    
    // Thiss function takes in a string that represents an incoming bluetooth msg from the HC-05
    private void readIncomingBluetoothData(byte[] data) {
        //if(data.length() > 0) { // Compare size to expected message size aka sizeof(struct)
            Log.d(TAG, "Incoming bluetooth data string preparing to be parsed");
            //byte[] received = data.getBytes();
            
            // Print bytes
//            StringBuilder sb = new StringBuilder();
//            for (byte b : received){
//                sb.append(String.format("%02X ", b));
//            }
//            Log.d(TAG, "Incoming bluetooth data string bytes: " + sb.toString());
            
            // Wrap data in ByteBuffer so we can parse the data easily
            ByteBuffer msgBuffer = ByteBuffer.wrap(data);  // BIG ENDIAN BY DEFAULT
            
            // Get first and last byte of the message
            byte startByte = data[0];
            byte endByte = data[data.length - 1];
            
            // Print out the two msg type bytes
            Log.d(TAG, "Incoming bluetooth data string msg type bytes: " + Byte.valueOf(data[0]) + " "  + Byte.valueOf(data[1]));
                
            // Check if start and stop bytes match message definition
            if((Byte.compare(startByte, (byte) 10) == 0) && (Byte.compare(endByte, (byte) 15) == 0)) {
                Log.d(TAG, "Incoming bluetooth data string has correct start and stop bytes");
                
                // Get two bytes starting at index one and convert them to a short
                short msgTypeValue = msgBuffer.getShort(1);
                Log.d(TAG, "Incoming bluetooth data string has message type: " + String.valueOf(msgTypeValue));
                
                // TODO: Parse the rest of the data packet
            }
        //}
        //else {
         //   Log.d(TAG, "Incoming bluetooth data is of size 0");
        //}
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
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

                    if(readData.length() > 0){
                        readIncomingBluetoothData(readBuffer);
                    }

                    
                    logArrayAdapter.add(connectedDevice + ": " + readData);
                    break;
                case BluetoothConstantsInterface.MESSAGE_DEVICE_NAME:
                    connectedDevice = msg.getData().getString(BluetoothConstantsInterface.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectedDevice, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothConstantsInterface.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothConstantsInterface.TOAST),
                            Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, R.string.bluetooth_btNotEnabledToast, Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(BluetoothDevices.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        bluetoothService.connect(device, secure);
    }
}