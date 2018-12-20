package com.spearbothy.touch.core.parser;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.spearbothy.touch.core.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author mahao
 * @date 2018/12/20 上午11:03
 */

public class ChartActivity extends AppCompatActivity {

    private static final String INTENT_PARAMS_FILE = "file";

    private TextView mTextView;

    private String mFilePath;

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
        mTextView = (TextView) findViewById(R.id.text);

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

    private void refreshChart(String json) {
        mTextView.setText(json);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INTENT_PARAMS_FILE, mFilePath);
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
            refreshChart(result);
        }
    }
}
