package fr.aasofts.afds;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;


public class SettingsActivity extends AppCompatActivity {


    Context context;
    SharedPreferences sharedPref;
    TextView txt_Server,txt_User, txt_Password, txt_UserId, txt_Address, txt_EmergencyPhone, txt_FriendPhone;

    public void  onBackPressed(){
        super.onBackPressed();

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Server", txt_Server.getText().toString());
        editor.putString("User", txt_User.getText().toString());
        editor.putString("Password", txt_Password.getText().toString());
        editor.putString("UserId", txt_UserId.getText().toString());
        editor.putString("Address", txt_Address.getText().toString());
        editor.putString("EmergencyPhone", txt_EmergencyPhone.getText().toString());
        editor.putString("FriendPhone", txt_FriendPhone.getText().toString());
        editor.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPref = this.getSharedPreferences("Default",
                Context.MODE_PRIVATE);

        txt_Server = (TextView) findViewById(R.id.txt_Server);
        txt_Server.setText(sharedPref.getString("Server", "wss://ns01-wss.brainium.com"));

        txt_User = (TextView) findViewById(R.id.txt_User);
        txt_User.setText(sharedPref.getString("User", "oauth2-user"));

        txt_Password = (TextView) findViewById(R.id.txt_Password);
        txt_Password.setText(sharedPref.getString("Password", ""));

        txt_UserId = (TextView) findViewById(R.id.txt_UserId);
        txt_UserId.setText(sharedPref.getString("UserId", ""));

        txt_Address = (TextView) findViewById(R.id.txt_Address);
        txt_Address.setText(sharedPref.getString("Address", ""));

        txt_EmergencyPhone = (TextView) findViewById(R.id.txt_EmergencyPhone);
        txt_EmergencyPhone.setText(sharedPref.getString("EmergencyPhone", ""));

        txt_FriendPhone = (TextView) findViewById(R.id.txt_FriendPhone);
        txt_FriendPhone.setText(sharedPref.getString("FriendPhone", ""));

    }
}
