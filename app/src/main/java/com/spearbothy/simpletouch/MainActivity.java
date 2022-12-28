package com.spearbothy.simpletouch;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sparbothy.library_so.NativeCallbackHelper;
import com.spearbothy.touch.core.Touch;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Touch.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRootView = (LinearLayout) findViewById(R.id.root);
        findViewById(R.id.button).setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        try {
            throw new RuntimeException("测试自定义异常");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
