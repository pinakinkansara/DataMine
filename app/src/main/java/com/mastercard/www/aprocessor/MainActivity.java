package com.mastercard.www.aprocessor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mastercard.www.library.DataMine;

@DataMine
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
