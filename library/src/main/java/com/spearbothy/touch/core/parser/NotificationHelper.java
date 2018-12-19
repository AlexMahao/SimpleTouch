package com.spearbothy.touch.core.parser;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.spearbothy.touch.core.R;

/**
 * @author mahao 2018/12/18 下午3:52
 */

public class NotificationHelper {

    private static final String CHANNEL_SIMPLE_TOUCH = "SimpleTouch";

    private static final String NOTIFICATION_TITLE = "SimpleTouch";
    private static final String NOTIFICATION_MESSAGE = "查看事件分发流程图入口";

    public static void sendNotificationForParser(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle();
        textStyle.setBigContentTitle(NOTIFICATION_TITLE)
                .bigText(NOTIFICATION_MESSAGE);

        Intent intent = new Intent();
        intent.setClassName(context, FileListActivity.class.getName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_SIMPLE_TOUCH)
                .setOngoing(true)
                .setTicker(NOTIFICATION_TITLE)
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(NOTIFICATION_MESSAGE)
                .setStyle(textStyle)
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

        setSmallIcon(builder);
        setVisibility(builder, NotificationCompat.VISIBILITY_PUBLIC);

        Notification notification = builder.build();
        nm.notify(1, notification);
    }


    @TargetApi(Build.VERSION_CODES.O)
    public static void init(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = makeChannel(CHANNEL_SIMPLE_TOUCH, "SimpleTouch", "触摸流程监听入口");
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }

    public static void setChannelId(NotificationCompat.Builder builder, String id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            switch (id) {
                case CHANNEL_SIMPLE_TOUCH:
                    builder.setChannelId(id);
                    break;
                default:
                    throw new IllegalArgumentException("no such channel: " + id);
            }
        }
    }

    public static void setSmallIcon(NotificationCompat.Builder builder) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            builder.setSmallIcon(R.drawable.notification_small_icon_new);
//        } else {
        builder.setSmallIcon(R.drawable.notification_small_icon);
//        }
    }

    public static void setVisibility(NotificationCompat.Builder builder, @NotificationCompat.NotificationVisibility int visibility) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(visibility);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static NotificationChannel makeChannel(String id, String name, String description) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        channel.setDescription(description);
        channel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        channel.setLightColor(Color.RED);
        channel.setSound(uri, Notification.AUDIO_ATTRIBUTES_DEFAULT);
        channel.enableVibration(false);
        return channel;
    }
}
