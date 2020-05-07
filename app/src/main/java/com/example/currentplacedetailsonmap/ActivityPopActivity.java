package com.example.currentplacedetailsonmap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ActivityPopActivity extends Activity {

    private TextView location_name, location_head, location_advice;

    private LocationManager locationManager;
    private LocationListener locationListener;

    // Popular grocery stores in the US
    ArrayList<String> groceries = new ArrayList<String>(
            Arrays.asList("Aldi", "Big Y", "Central Market", "Lidl", "Publix", "Stop & Shop",
                    "Target", "Trader Joe's", "Walmart", "Wegmans", "Whole Foods"));

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_popup_window);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Adjust the popup window dimensions
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .5), (int) (height * .22));

        bindView();

        // Setup location listener & manager
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        locationListener = new GPSLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                0, locationListener);

    }

    private void bindView() {

        location_head = findViewById(R.id.location_head);
        location_name = findViewById(R.id.location_name);
        location_advice = findViewById(R.id.location_advice);

    }

    private class GPSLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {

                String address = "";
                Geocoder gc = new Geocoder(getBaseContext(), Locale.getDefault());

                try {

                    List<Address> addresses = gc.getFromLocation(location.getLatitude(),
                            location.getLongitude(), 5);

                    if (addresses.size() > 0) {

                        // Found a location so update text on view
                        location_head.setText("Looks like you're currently at");

                        // Returns a line of the address numbered by the given
                        // index (starting at 0), or null if no such line is present.
                        address = addresses.get(0).getAddressLine(0);
                        location_name.setText(address);

                        // Returns the feature name of the address.
                        // for example, "Golden Gate Bridge", or null if it is unknown
                        String feature_name = addresses.get(0).getFeatureName();

                        // Check if location is a grocery store
                        if (groceries.contains(feature_name)) {

                            // Update advice on view
                            location_advice.setText("Looks like you're getting groceries. "+
                                    "Always wear a mask in public and maintain 6 feet of " +
                                    "distance from others!");
                            location_advice.setTextColor(Color.rgb(255, 165,0));

                        }

                        // Else potentially in public
                        else {

                            // Update advice on view
                            location_advice.setText("If you're in a public area, don't forget " +
                                            "to wear a mask and avoid large crowds!");
                            location_advice.setTextColor(Color.rgb(0,255, 0));

                        }

                    }

                }

                catch (IOException e) {

                    e.printStackTrace();

                }

            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onProviderDisabled(String provider) { }

    }

}
