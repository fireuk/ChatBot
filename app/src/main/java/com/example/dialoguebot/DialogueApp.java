package com.example.dialoguebot;

import android.app.Application;
import android.util.Log;

public class DialogueApp extends Application {

    private static final String TAG = "braind_DialogueApp";

    private static DialogueApp sApplicationContext = null;


    public static DialogueApp getInstance() {
        return sApplicationContext;
    }


    @Override
    public void onCreate() {
        Log.i(TAG, "DialogueApp onCreate");
        super.onCreate();
        sApplicationContext = this;
    }
}
