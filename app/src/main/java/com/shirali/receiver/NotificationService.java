/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shirali.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.shirali.R;
import com.shirali.activity.MainActivity;
import com.shirali.activity.SplashActivity;
import com.shirali.model.user.UserModel;
import com.shirali.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            String message = "";
            try {
                JSONObject jsonObject = new JSONObject(remoteMessage.getData());
                if (jsonObject.has("custom")){
                    JSONObject jObject = new JSONObject(remoteMessage.getData().get("custom"));
                    if (jObject.has("a")) {
                        sendNotification(remoteMessage);
                    } else {
                        String msg = "";
                        try {
                            JSONObject jsonObjec = new JSONObject(remoteMessage.getData());
                            msg = jsonObjec.getString("alert");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sendNotificationSimple(msg);
                    }
                }else {
                    message = jsonObject.getString("aps");
                    if (message == null || message.equalsIgnoreCase("")) {
                        String msg = "";
                        try {
                            JSONObject jsonObjec = new JSONObject(remoteMessage.getData());
                            msg = jsonObjec.getString("mp_message");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sendNotificationSimple(msg);
                    } else {
                        sendMixpanelNotification(remoteMessage);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    //Send notification with data payload
    private void sendMixpanelNotification(RemoteMessage message) {
        SplashActivity.isFromDeep = true;
        JSONObject jObject = null; // json
        try {
            jObject = new JSONObject(message.getData().get("aps"));
            String projectname = "";
            try {
                projectname = jObject.getString("alert");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String type = message.getData().get("type");
            String id = message.getData().get("id");
            Intent open = new Intent(this, SplashActivity.class);
            open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            open.putExtra(type, id);
            open.putExtra("type", type);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, open,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.imglogo)
                    .setContentTitle("ShiraLi")
                    .setContentText(projectname)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Utility.getBooleaPreferences(getApplicationContext(),"NotificationPref")) {
                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
            } else {
                notificationManager.cancel(0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Send notification with data payload
    private void sendNotification(RemoteMessage message) {
        SplashActivity.isFromDeep = true;
        JSONObject jObject = null; // json
        try {
            jObject = new JSONObject(message.getData().get("custom"));
            JSONObject projectname = null;
            try {
                projectname = jObject.getJSONObject("a");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String type = projectname.getString("type");
            String id = projectname.getString("id");
            Intent open = new Intent(this, SplashActivity.class);
            open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            UserModel.getInstance().notificationType = type;
            UserModel.getInstance().notificationId = id;
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, open,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.imglogo)
                    .setContentTitle("ShiraLi")
                    .setContentText("HelloTest")
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Utility.getBooleaPreferences(getApplicationContext(), "NotificationPref")) {
                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
            } else {
                notificationManager.cancel(0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Send Simple notification
    private void sendNotificationSimple(String messageBody) {
        final int icon = R.drawable.imglogo;
        BitmapDrawable smallDrawable = (BitmapDrawable) getResources().getDrawable(icon);
        Bitmap smallicon = smallDrawable.getBitmap();
        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setColor(getResources().getColor(R.color.black))
                .setSmallIcon(R.drawable.imglogo).setWhen(0)
                .setContentTitle("ShiraLi")
                .setLargeIcon(smallicon)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utility.getBooleaPreferences(getApplicationContext(), "NotificationPref")) {
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        } else {
            notificationManager.cancel(0);
        }
    }
}
