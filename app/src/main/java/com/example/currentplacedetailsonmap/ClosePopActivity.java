package com.example.currentplacedetailsonmap;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Set;

public class ClosePopActivity extends Activity {

    // Intent request code
    private static final int REQUEST_ENABLE_BT = 3;

    /**
     * Member fields
     */
    private BluetoothAdapter mBtAdapter;

    // Paired devices
    private Set<BluetoothDevice> pairedDevices;

    /**
     * Newly discovered devices
     */
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    private int nearbyDevices;

    private TextView numClose, who_close, advice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.close_popup_window);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Adjust the popup window dimensions
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width*.5),(int) (height*.22));

        bindView();

        // Setup bluetooth
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBtAdapter.isEnabled()) {

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }

        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.score_popup_window);

        // Get paired devices
        pairedDevices = mBtAdapter.getBondedDevices();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(receiver, filter);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {

            mBtAdapter.cancelDiscovery();

        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();

    }

    private void bindView() {

        who_close = findViewById(R.id.who_close);
        numClose = findViewById(R.id.numClose);
        advice = findViewById(R.id.advice);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {

            mBtAdapter.cancelDiscovery();

        }

        // Don't forget to unregister the ACTION_FOUND receiver.
        this.unregisterReceiver(receiver);

    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {

                    if (device.getName() != null) {

                        mNewDevicesArrayAdapter.add(device.getName());

                    }

                }

                // Get number of nearby bluetooth devices
                nearbyDevices = mNewDevicesArrayAdapter.getCount();

                // Set lower range for expected number of people
                int lowerRange = (2 * nearbyDevices) / 3;

                // Update text to indicate only 1 person is near the user
                if (nearbyDevices == 1) {

                    who_close.setText("It looks like there's only");
                    numClose.setText(String.format("%d", nearbyDevices) + "\n" + "person near you");

                }

                // Update text to indicate more than 1 person is near the user
                else if (nearbyDevices > 1){

                    who_close.setText("It looks like there's around");
                    numClose.setText(String.format("%d - %d", lowerRange, nearbyDevices) + "\n" + "people near you");

                }

                // Update advice to indicate user is maintaining social distancing
                if (nearbyDevices < 5) {

                    advice.setText("Looks like you're maintaining Social Distancing!" + "\n" + "Keep it up!");
                    advice.setTextColor(Color.rgb(0, 255, 0));

                }

                // Update advice to warn the user is near a small group of people
                else if (nearbyDevices >= 5 && nearbyDevices < 10) {

                    advice.setText("Looks like there's a small\n group of people near you.\n We suggest staying wary of\n your surroundings!");
                    advice.setTextColor(Color.rgb(255, 165, 0));

                }

                // Update advice to warn the user is near a large group of people
                else if (nearbyDevices >= 10) {

                    advice.setText("Looks like you're near a large\n group of people.\n Health Care Professionals currently recommend \nlimiting groups to less than 10!");
                    advice.setTextColor(Color.rgb(255, 0, 0));

                }

            }
        }
    };

}
