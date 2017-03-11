package com.example.jay_pc.dcrypt;

/**
 * Created by Jay-pc on 3/10/2017.
 */
public class DBLogger {

    private DCryptActivity dCryptActivity = null;

    public DBLogger(DCryptActivity dCryptActivity) {
        this.dCryptActivity = dCryptActivity;
    }

    public void e(String tag, String message) {
        android.util.Log.e(tag, message);
        dCryptActivity.appendToMainTextView(message);
    }

    public void i(String tag, String message) {
        android.util.Log.i(tag, message);
        dCryptActivity.appendToMainTextView(message);
    }
}