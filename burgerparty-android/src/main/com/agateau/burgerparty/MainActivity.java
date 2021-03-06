package com.agateau.burgerparty;

import android.os.Bundle;

import com.agateau.burgerparty.utils.GdxPrinter;
import com.agateau.burgerparty.utils.NLog;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class MainActivity extends AndroidApplication {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BurgerPartyGame game = new BurgerPartyGame();
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL20 = true;
        cfg.useCompass = false;
        cfg.useAccelerometer = false;
        cfg.hideStatusBar = true;
        initialize(game, cfg);
        // Must be done *after* initialize because it requires Gdx.app to be
        // valid
        NLog.addPrinter(new GdxPrinter("BP"));
        NLog.i("");
        game.setRatingControllerImplementation(new AndroidRatingControllerImplementation(this));
    }
}
