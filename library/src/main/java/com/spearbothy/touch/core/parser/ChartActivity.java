package com.spearbothy.touch.core.parser;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.spearbothy.touch.core.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author mahao
 */

public class ChartActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ChartActivity.class.getSimpleName();

    private static final String INTENT_PARAMS_FILE = "file";

    private String mFilePath;

    private String mJsonData;

    private boolean isJson = true;
    private TextView mTypeView;
    private JsonViewerFragment mJsonFragment;
    private ChartFragment mChartFragment;

    public static Intent getIntent(Context context, String file) {
        Intent intent = new Intent(context, ChartActivity.class);
        intent.putExtra(INTENT_PARAMS_FILE, file);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mFilePath = savedInstanceState.getString(INTENT_PARAMS_FILE, "");
        } else {
            mFilePath = getIntent().getStringExtra(INTENT_PARAMS_FILE);
        }
        setContentView(R.layout.simple_touch_activity_chart);
        mTypeView = (TextView) findViewById(R.id.type);
        mTypeView.setOnClickListener(this);
        readFile();
    }

    private void readFile() {
        if (TextUtils.isEmpty(mFilePath)) {
            Toast.makeText(this, "未获取到文件地址", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new ReadFileTask().execute(mFilePath);
    }

    private void refreshView(String json) {
        if (TextUtils.isEmpty(json)) {
            Toast.makeText(this, "读取文件错误" + mFilePath, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // json
        mJsonData = json;

        mJsonFragment = JsonViewerFragment.newInstance(mJsonData);
        mChartFragment = ChartFragment.newInstance(mJsonData);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment, mJsonFragment, "json")
                .add(R.id.fragment, mChartFragment, "chart")
                .commit();

        switchFragment();
    }

    private void switchFragment() {
        if (TextUtils.isEmpty(mJsonData)) {
            Toast.makeText(this, "数据为空，无法进行展示" + mFilePath, Toast.LENGTH_SHORT).show();
            return;
        }
        isJson = !isJson;
        if (isJson) {
            mTypeView.setText("JSON视图");
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(mChartFragment)
                    .show(mJsonFragment)
                    .commit();
        } else {
            mTypeView.setText("View视图");
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(mJsonFragment)
                    .show(mChartFragment)
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INTENT_PARAMS_FILE, mFilePath);
    }

    @Override
    public void onClick(View v) {
        switchFragment();
    }

    private class ReadFileTask extends AsyncTask<String, Void, String> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(ChartActivity.this);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected String doInBackground(String... params) {
            FileInputStream in = null;
            BufferedReader reader = null;
            StringBuilder content = new StringBuilder();
            try {
                in = new FileInputStream(params[0]);
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return content.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            refreshView(result);
        }
    }
}
