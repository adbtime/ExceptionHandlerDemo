package com.adbtim.exceptionhandlerdemo.RestartDemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.adbtim.exceptionhandlerdemo.R;


/**
 * Created by adbtime on 16/2/9.
 * email: adbtime@outlook.com
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restart_activity_main);

        Button crashMainThreadButton = (Button) findViewById(R.id.button_crash);

        crashMainThreadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                throw new RuntimeException("Exception Happen.");
            }
        });

    }
}
