package com.app.desktop;

import com.app.InterfaceListener;
import com.app.Main;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

// DesktopLauncher
public class DesktopLauncher implements InterfaceListener {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "App"; // window title
        config.width = 800; // default screen width
        config.height = 450; // default screen height

        // fullscreen mode
        /*config.fullscreen = true;
        config.width = Gdx.graphics.getDisplayMode().width;
        config.height = Gdx.graphics.getDisplayMode().height;*/

        // run
        new LwjglApplication(new Main(new DesktopLauncher()), config);
    }

    @Override
    public void saveScore(int score) {
        // called when game score has been changed
    }

    @Override
    public void signIn() {
        // called when pressed "Sign In" to Google Play Game Services
    }

    @Override
    public void signOut() {
        // called when pressed "Sign Out" from Google Play Game Services
    }

    @Override
    public void rate() {
        // called if need to rate the App
    }

    @Override
    public void showLeaders() {
        // called when pressed "Leaders"
    }

    @Override
    public void admobInterstitial() {
        // called to show AdMob Interstitial
    }
}