package com.datamine.www.aprocessor;

import android.content.Context;

import com.datamine.www.library.DataKey;
import com.datamine.www.library.DataMine;

@DataMine(fileName = "preference_test",mode = Context.MODE_PRIVATE)
public class PreferenceData {

    @DataKey(key = "key_for_test")
    private int test;
}
