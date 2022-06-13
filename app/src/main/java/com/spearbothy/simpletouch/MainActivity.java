package com.spearbothy.simpletouch;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spearbothy.touch.core.Touch;

public class MainActivity extends AppCompatActivity {
    private LinearLayout mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Touch.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRootView = (LinearLayout) findViewById(R.id.root);
    }
}
