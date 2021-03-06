package com.spearbothy.touch.core.print;

import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.spearbothy.touch.core.Constants;
import com.spearbothy.touch.core.Message;
import com.spearbothy.touch.core.Touch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * @author mahao 2018/11/15 下午3:19
 */

public class FilePrint implements Print {

    public static final String DIR = "SimpleTouch";
    private static final String LOG_TOKEN_FORMART = "yyyy-MM-dd_HH:mm:ss";

    @Override
    public void printMessage(Message message) {}

    @Override
    public void printMultipleMessage(List<Message> messages) {
        new WriterFileTask().execute(JsonFactory.toJson(messages));
    }

    private class WriterFileTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String json = params[0];
            if (TextUtils.isEmpty(json)) {
                return "json错误，未查找到日志文件";
            }
            if (!isExternalStorageAvailable()) {
                return "存储不可用";
            }
            if (TextUtils.isEmpty(Touch.sHostPackage)) {
                return "请先调用Touch.init(context)完成初始化";
            }

            String absFileDir = Environment.getExternalStorageDirectory() + File.separator + DIR + File.separator + Touch.sHostPackage + File.separator;
            SimpleDateFormat sdf = new SimpleDateFormat(LOG_TOKEN_FORMART, Locale.getDefault());

            String absFileName = absFileDir + sdf.format(System.currentTimeMillis()) + "_log.txt";

            File parentFile = new File(absFileDir);

            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            File file = new File(absFileName);
            if (file.exists()) {
                file.delete();
            }

            PrintStream ps = null;
            try {
                file.createNewFile();
                ps = new PrintStream(new FileOutputStream(file));
                ps.println(json);// 往文件里写入字符串
                ps.flush();
            } catch (FileNotFoundException e) {
                return e.getMessage();
            } catch (IOException e) {
                return e.getMessage();
            } finally {
                if (ps != null) {
                    ps.close();
                }
            }
            return "touch日志已保存至文件:" + absFileName;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(Constants.LOG_TAG, result);
        }
    }

    public static boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
