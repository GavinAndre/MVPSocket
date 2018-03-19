package com.gavinandre.mvpsocketclient.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;


/**
 * Created by gavinandre on 18-1-8.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected void startActivity(Class<?> clz) {
        Intent intent = new Intent(this, clz);
        startActivity(intent);
    }

}
