package com.adbtim.exceptionhandlerdemo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Created by adbtime on 16/2/9.
 * email: adbtime@outlook.com
 */

public class ExceptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int i = 2 / 0;// will cause exception
            }
        });

//        try {
//            // the exception would come out
//        }catch (Exception e){
//            //throw e;
//            //catch the exception , you can send the e.message to you
//            // to help you fix the bug
//        }finally {
//            // if the exception happened, come here .
//        }

    }

}
