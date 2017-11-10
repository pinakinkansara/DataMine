package com.datamine.www.aprocessor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;




public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceRepository repository = new PreferenceRepository(this);
        repository.puttest(10);

        int val = repository.gettest(-1);
        Log.d(TAG, "Value obtained from preference : "+val);
    }
}
