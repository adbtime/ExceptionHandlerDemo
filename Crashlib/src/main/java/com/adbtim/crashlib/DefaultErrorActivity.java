package com.adbtim.crashlib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by adbtime on 16/2/9.
 * email: adbtime@outlook.com
 */
public class DefaultErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_error);

        Button restartButton = (Button) findViewById(R.id.activity_default_error_restart_button);

        final Class<? extends Activity> restartActivityClass = CrashHandler.getRestartActivityClassFromIntent(getIntent());

        if (restartActivityClass != null) {
            restartButton.setText(R.string.activity_default_error_restart_app);
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DefaultErrorActivity.this, restartActivityClass);
                    CrashHandler.restartApplicationWithIntent(DefaultErrorActivity.this, intent);
                }
            });
        } else {
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CrashHandler.closeApplication(DefaultErrorActivity.this);
                }
            });
        }

        Button moreInfoButton = (Button) findViewById(R.id.activity_default_error_more_info_button);

        if (CrashHandler.isShowErrorDetailsFromIntent(getIntent())) {

            moreInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //you can get error message
                  String errorValue = CrashHandler.getAllErrorDetailsFromIntent(DefaultErrorActivity.this, getIntent());
                    Log.e("DefaultErrorActivity","errorVlaue =" +errorValue);

                }
            });
        } else {
            moreInfoButton.setVisibility(View.GONE);
        }

    }
}
