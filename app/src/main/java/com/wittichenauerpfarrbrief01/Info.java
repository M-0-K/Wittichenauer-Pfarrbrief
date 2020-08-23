package com.wittichenauerpfarrbrief01;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;


public class Info extends AppCompatActivity {

    public SharedPreferences pref;
    public SharedPreferences.Editor editor;
    protected MainActivity ma;
    protected Button btemail;
    protected Button btback;
    protected TextView tvname;
    protected TextView tvversion;
    protected Switch swdark;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getSharedPreferences("Wittichenauer Pfarrbrief", 0);
        editor = pref.edit();
        if(pref.getBoolean("Darkmode", false)){
            setTheme(R.style.DarkTheme);
        }else{
            setTheme(R.style.LightTheme);
        }
        setContentView(R.layout.activity_info);

        ma = new MainActivity();
        btemail = (Button) findViewById(R.id.btemail);
        btback = (Button) findViewById(R.id.btback);
        tvname = (TextView) findViewById(R.id.tvname);
        tvversion = (TextView) findViewById(R.id.tvversion);
        swdark = (Switch) findViewById(R.id.swdark);
        swdark.setChecked(pref.getBoolean("Darkmode", false));

        tvversion.setText("Version: " + BuildConfig.VERSION_NAME);

        swdark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swdark.isChecked()){
                    editor.putBoolean("Darkmode", true);
                    editor.commit();
                    setTheme(R.style.DarkTheme);
                }else {
                    editor.putBoolean("Darkmode", false);
                    editor.commit();
                    setTheme(R.style.LightTheme);
                }
                Intent explicitIntent = new Intent(Info.this, Info.class);
                startActivity(explicitIntent);
            }
        });

        btemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"moritzkockert@gmail.com"});
                try {
                    startActivity(Intent.createChooser(i, "Sende Mail..."));
                } catch (ActivityNotFoundException ex) {
                    //Toast.makeText(ExampleActivity.this, "Es sind keine Mail-Clients installiert, weshalb die Mail nicht versendet werden kann.", Toast.LENGTH_LONG).show();
                }
            }
        });

        btback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent explicitIntent = new Intent(Info.this, MainActivity.class);
                startActivity(explicitIntent);
            }
        });

        tvname.setText("''"+pref.getString("pfdatum", "") + "Pfarrbrief.pdf'' \n  gedownloaded am: " + pref.getString("ddatum", ""));


    }

}