package com.example.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by Екатерина Захарова on 29.02.2016.
 */

public class SplashScreen extends Activity {
    final int SPLASH_DISPLAY_LENGTH = 3000;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_screen_layout);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}

