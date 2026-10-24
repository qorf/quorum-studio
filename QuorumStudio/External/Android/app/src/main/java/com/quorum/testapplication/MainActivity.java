package com.quorum.testapplication;

import android.app.Activity;
import android.os.Bundle;

import com.quorum.TestApplication.R;

import quorum.Libraries.Game.AndroidApplication;
import plugins.quorum.Libraries.Game.GameStateManager;
import quorum.Main;

// Quorum Studio replaces this file with one for the program's Main class
// (AndroidSupport:GetHiddenMain).
public class MainActivity extends Activity {

    private static boolean initialized = false;
    private static Main main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        plugins.quorum.Libraries.Game.AndroidApplication.SetActivity(this);

        if (savedInstanceState == null || main == null)
        {
            main = new Main();
            main.Main();
        }
        else
        {
            main.StartAndroidGame();
        }
        initialized = true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

        if (initialized)
            ((AndroidApplication)GameStateManager.application).plugin_.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onPause()
    {
        if (initialized)
            ((AndroidApplication)GameStateManager.application).plugin_.onPause();

        super.onPause();
    }

    @Override
    protected void onResume()
    {
        if (initialized)
            ((AndroidApplication)GameStateManager.application).plugin_.onResume();

        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        if (initialized)
            ((AndroidApplication)GameStateManager.application).plugin_.onDestroy();

        super.onDestroy();
    }
}
