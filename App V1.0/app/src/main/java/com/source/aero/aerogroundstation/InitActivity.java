package com.source.aero.aerogroundstation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

public class InitActivity extends AppCompatActivity {
    //UI elements
    Button startButton;
    RadioGroup configGroup;
    TextView configPreview;

    String configuration = "";
    String configurationKey = "CONFIGURATION";

    //Required methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize UI elements
        setContentView(R.layout.activity_initactivity);
        startButton = (Button) findViewById(R.id.initConfigStartButton);
        configGroup = (RadioGroup) findViewById(R.id.initConfigRadioGroup);
        configPreview = (TextView) findViewById(R.id.initConfigPreviewTextView);

        //Register on click events
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMainActivity();
            }
        });
        //Send id of radio button to config method
        configGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                changeConfig(id);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    //Change config preview based on selected radio button
    private void changeConfig(int radioButton) {
        TextView text = (TextView) findViewById(R.id.initConfigPreviewTextView);
        switch (radioButton) {
            case R.id.initConfigButton1:
                text.setText(getResources().getString(R.string.config1PreviewText));
                configuration = "COMPETITION";
                break;
            default:
                text.setText(getResources().getString(R.string.config2PreviewText));
                configuration = "DEBUG";
        }
    }

    //Start main activity (sends chosen configuration through intent)
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(configurationKey,configuration);
        startActivity(intent);
    }
}
