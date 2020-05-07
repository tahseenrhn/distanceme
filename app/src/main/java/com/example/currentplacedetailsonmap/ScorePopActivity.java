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

public class ScorePopActivity extends Activity {

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

    private double currScore, newScore;
    private int nearbyDevices;

    private TextView curr_score, last_score, last_score_title, curr_score_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.score_popup_window);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Adjust the popup window dimensions
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width*.5),(int) (height*.175));

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

        curr_score = findViewById(R.id.curr_score);
        last_score = findViewById(R.id.last_score);
        last_score_title = findViewById(R.id.last_score_title);
        curr_score_title = findViewById(R.id.curr_score_title);

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

                // No new bluetooth devices within a measured threshold nearby
                if (nearbyDevices <= 2) {

                    newScore = 100;

                }

                // Nearby bluetooth devices found pass measured threshold
                // Calculate new score
                else {

                    // Check scores
                    if (currScore <= 0 || newScore <= 0) {

                        currScore = 0;
                        newScore = 0;

                    }

                    else if (currScore > 0) {

                        newScore = 100 - ((nearbyDevices / currScore) * 100);

                    }

                }

                // Update Last Score on view if score has changed
                if (currScore != newScore) {

                    // Format & update last score -> rounded to 2 decimal places
                    last_score.setText(String.format("%.2f", currScore));

                    // Set color for excellent score
                    if (currScore >= 80) last_score.setTextColor(Color.rgb(0, 255, 0));

                    // Set color for moderate score
                    else if (currScore >= 60 && currScore < 80) last_score.setTextColor(Color.rgb(255, 165, 0));

                    // Set color for bad score
                    else last_score.setTextColor(Color.rgb(255, 0, 0));

                }

                // Update Current Score
                currScore = newScore;

                // Format & update score on view -> rounded to 2 decimal places
                curr_score.setText(String.format("%.2f", currScore));

                // Set color for excellent score
                if (currScore >= 80) curr_score.setTextColor(Color.rgb(0, 255, 0));

                // Set color for moderate score
                else if (currScore >= 60 && currScore < 80) curr_score.setTextColor(Color.rgb(255, 165, 0));

                // Set color for bad score
                else curr_score.setTextColor(Color.rgb(255, 0, 0));

            }

        }
    };

}
