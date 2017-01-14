package com.olegel.professor.testcamera;

import android.app.Application;

public class App extends Application {
    @Override
    public void onCreate() {
        SavePicture.setContext(getApplicationContext());
        super.onCreate();
    }
}
