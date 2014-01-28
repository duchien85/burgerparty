package com.agateau.burgerparty;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class MainActivity extends AndroidApplication {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL20 = true;
        cfg.useCompass = false;
        cfg.useAccelerometer = false;
        cfg.hideStatusBar = true;
        initialize(new BurgerPartyGame(new AndroidAdController()), cfg);
    }
}