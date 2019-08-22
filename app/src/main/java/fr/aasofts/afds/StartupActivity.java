package fr.aasofts.afds;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.util.Log;
import android.content.SharedPreferences;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;


import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


import helpers.MqttHelper;



public class StartupActivity extends AppCompatActivity {

    MqttHelper mqttHelper;

    private TextView mTextMessage;
    private TextView mTextConnection;
    private TextView mTextLocation;
    TextToSpeech tts;
    String sosMessage = "The alert system has detected that you have fallen. You have  60 seconds to cancel the alert. Otherwise, emergency services and the person that has been designated will be  informed. If you cannot get up, do not risk further damage or injury by attempting to force yourself to stand. If you decide to try to get up, roll to one side, and then slowly pull yourself up on all fours, until you are on your hands and knees. If there is no sturdy object nearby, crawl to a sturdy object. Push on the object with your hands, supporting your body weight with your hands and slowly rise to a sitting position on the steps or sturdy piece of furniture. Remain seated until confident that you can stand.";
    LocationManager locationManager;
    double longitudeBest, latitudeBest;
    private static final int AUTO_DISMISS_MILLIS = 60000;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;

                case R.id.navigation_settings:
                    GoSettings();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        mTextMessage = findViewById(R.id.message);
        mTextConnection = findViewById(R.id.msg_Connection);
        mTextLocation = (TextView) findViewById(R.id.msg_location);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        startMqtt();
        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                }
            }
        });
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        toggleBestUpdates();
    }

    private void GoSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    private void startMqtt() {
        mqttHelper = new MqttHelper(this, getApplicationContext());
        mqttHelper.mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                mTextConnection.setText("Connected to : " + s);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                mTextConnection.setText("Connection lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                try {

                    String innerMessage = mqttMessage.toString().substring(1, mqttMessage.toString().length() - 1);
                    Log.w("Debug", innerMessage.toString());
                    String[] couples = innerMessage.split(",");

                    Hashtable<String, String> hash = new Hashtable<String, String>();
                    try {
                        for (int i = 0; i < couples.length; i++) {
                            String s = couples[i];
                            int index = s.indexOf(":");
                            hash.putIfAbsent(s.substring(1, index - 1), s.substring(index + 2, s.length() - 1));
                        }
                    }
                    catch(Exception ex){Log.w("Debug", ex.getMessage());}

                    String motionTypeName = hash.get("motionTypeName");
                    Log.w("Debug", motionTypeName);
                    if (motionTypeName.equals("ElderyFall"))
                        confirmSOS();

                } catch (Exception ex) {
                    Log.w("Debug", ex.getMessage());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void toggleBestUpdates() {

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            String provider = locationManager.getBestProvider(criteria, true);
            if(provider != null && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
                locationManager.requestLocationUpdates(provider, 2 * 60 * 1000, 10, locationListenerBest);
            }
    }

    private final LocationListener locationListenerBest = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeBest = location.getLongitude();
            latitudeBest = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextLocation.setText("Long:"+longitudeBest+"; Lat:"+latitudeBest);
                }
            });
        }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        private void confirmSOS()
        {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("False Alert?")
                    .setMessage("Do you want to cancel this alert?")
                    .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setNegativeButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sos();
                        }
                    })
                    .create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(final DialogInterface dialog) {
                    final Button defaultButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                    final CharSequence negativeButtonText = defaultButton.getText();
                    new CountDownTimer(AUTO_DISMISS_MILLIS, 100) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            defaultButton.setText(String.format(
                                    Locale.getDefault(), "%s (%d)",
                                    negativeButtonText,
                                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1 //add one so it never displays zero
                            ));
                        }
                        @Override
                        public void onFinish() {
                            sos();
                            if (((AlertDialog) dialog).isShowing()) {
                                dialog.dismiss();
                            }
                        }
                    }.start();
                }
            });
            dialog.show();

        }
        private void sos() {


        SharedPreferences sharedPreferences = this.getSharedPreferences("Default",
                Context.MODE_PRIVATE);

        String address = sharedPreferences.getString("Address", "");
        String emergencyPhone = sharedPreferences.getString("EmergencyPhone", "");
        String friendPhone = sharedPreferences.getString("FriendPhone", "");
        tts.speak(sosMessage, TextToSpeech.QUEUE_FLUSH, null);

        try {

            if (friendPhone != "") {

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(friendPhone, null, sosMessage, null, null);
                smsManager.sendTextMessage(friendPhone, null, address, null, null);
                smsManager.sendTextMessage(friendPhone, null, "GPS Position: " + "Long:"+longitudeBest+"; Lat:"+latitudeBest, null, null);
            }
        } catch (Exception ex) {

        }


    }
}