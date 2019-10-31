package com.shirali.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shirali.App;
import com.shirali.R;
import com.shirali.activity.LoginActivity;
import com.shirali.activity.SplashActivity;
import com.shirali.activity.YourSubscriptionActivity;
import com.shirali.controls.Controls;
import com.shirali.model.campaign.Campaign;
import com.shirali.model.playlist.Shirali;
import com.shirali.model.setting.AppSetting;
import com.shirali.model.setting.Setting;
import com.shirali.model.songs.GenresList;
import com.shirali.model.songs.Song;
import com.shirali.model.user.User;
import com.shirali.model.user.UserModel;
import com.shirali.service.SongPlayService;
import com.shirali.widget.CustomBottomTabView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.ServiceConfigurationError;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Sagar on 5/8/17.
 */

public class Utility {

    public static final String preferencesLanguage = "appLanguage";
    public static final String IS_LOGIN = "isLogin";
    private static final String PREF = "login";
    private static final String PREF_TIMER = "timer";
    public static MediaPlayer mediaplayer;
    public static boolean isAdAlreadyFound = false;
    private static String PREFERENCES = "Shirali";
    private static boolean isDialogShow;

    public static User getUserInfo(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("save", 0);
        return new Gson().fromJson(preferences.getString("userData", ""), User.class);
    }

    public static void setUserInfo(Context context, User user) {
        SharedPreferences preferences = context.getSharedPreferences("save", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userData", new Gson().toJson(user)).apply();
    }

    public static GenresList getGenres(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("Genres", 0);
        return new Gson().fromJson(preferences.getString("genres", ""), GenresList.class);
    }

    public static void setGenres(Context context, GenresList genres) {
        SharedPreferences preferences = context.getSharedPreferences("Genres", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("genres", new Gson().toJson(genres)).apply();
    }

    public static Setting getUserSetting(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("setting", 0);
        return new Gson().fromJson(preferences.getString("user_setting", ""), Setting.class);
    }

    public static void setUserSetting(Context context, AppSetting user) {
        SharedPreferences preferences = context.getSharedPreferences("setting", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_setting", new Gson().toJson(user.setting)).commit();
    }

    public static String getStringPreferences(Context context, String key) {

        SharedPreferences setting = (SharedPreferences) context
                .getSharedPreferences(PREFERENCES, 0);
        return setting.getString(key, "");

    }

    public static void setStringPreferences(Context context, String key,
                                            String value) {
        SharedPreferences setting = (SharedPreferences) context
                .getSharedPreferences(PREFERENCES, 0);

        SharedPreferences.Editor editor = setting.edit();

        editor.putString(key, value);
        editor.commit();

    }

    public static void setBooleanPreferences(Context context, String key,
                                             Boolean value) {
        SharedPreferences setting = (SharedPreferences) context
                .getSharedPreferences(PREF, 0);
        SharedPreferences.Editor editor = setting.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static Boolean getBooleaPreferences(Context context, String key) {
        SharedPreferences setting = (SharedPreferences) context
                .getSharedPreferences(PREF, 0);
        return setting.getBoolean(key, false);
    }


    public static void setLongPreferences(Context context, String key,
                                          String value) {
        SharedPreferences setting = (SharedPreferences) context
                .getSharedPreferences(PREF_TIMER, 0);
        SharedPreferences.Editor editor = setting.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static int getIntPreferences(Context context, String key) {
        SharedPreferences setting = (SharedPreferences) context
                .getSharedPreferences(PREF_TIMER, 0);
        return setting.getInt(key, 0);
    }

    public static void setIntPreferences(Context context, String key,
                                         int value) {
        SharedPreferences setting = (SharedPreferences) context
                .getSharedPreferences(PREF_TIMER, 0);
        SharedPreferences.Editor editor = setting.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static String getLongPreferences(Context context, String key) {
        SharedPreferences setting = (SharedPreferences) context
                .getSharedPreferences(PREF_TIMER, 0);
        return setting.getString(key, "");
    }

    public static void clearAllSharedPreferences(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREF, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static Calendar setDateIntoCalenderAndGet(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static Date convertStringToDate(String dateString, String dateFormat) {
        try {
            return new SimpleDateFormat(dateFormat, Locale.US).parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DrawableRequestBuilder<String> loadMailPlayImage(@NonNull String posterPath, Context context) {
        return Glide
                .with(context)
                .load(posterPath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)   // cache both original & resized image
                .crossFade();
    }

    public static String getFormatedDate(String strDate, String sourceFormate,
                                         String destinyFormate) {
        SimpleDateFormat df;
        df = new SimpleDateFormat(sourceFormate);
        Date date = null;
        try {
            date = df.parse(strDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        df = new SimpleDateFormat(destinyFormate);
        return df.format(date);
    }

    public static boolean dateIsExist(String strDate, String strDate2, String format,String current_date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date date = new Date();
        String current = dateFormat.format(date);
        if (dateFormat.parse(current_date).after(dateFormat.parse(strDate2)) && dateFormat.parse(current_date).before(dateFormat.parse(strDate))) {
            return true;
        } else {
            return false;
        }
    }

    private static String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
    }

    public static String getCurrentTimeEvent() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date());
    }

    public static String formatSeconds(Context context, int timeInSeconds) {
        int secondsLeft = timeInSeconds % 3600 % 60;
        int minutes = (int) Math.floor(timeInSeconds % 3600 / 60);
        int hours = (int) Math.floor(timeInSeconds / 3600);

        String HH = String.valueOf(hours < 10 ? "0" + hours : hours);
        String MM = String.valueOf(minutes < 10 ? "0" + minutes : minutes);
        //string SS = secondsLeft < 10 ? "0" + secondsLeft : secondsLeft;
        if (HH.equalsIgnoreCase("00")) {
            return MM + " " + context.getResources().getString(R.string.minute);
        } else {
            return HH + " " + context.getString(R.string.hour) + " " + MM + " " + context.getResources().getString(R.string.minute);
        }
    }

    // Set App Language
    public static void setAppLanguage(Context mContext) {
        String languageToLoad = Utility.getStringPreferences(mContext, preferencesLanguage);
        if (languageToLoad == null) {
            languageToLoad = "en";
            Utility.setStringPreferences(mContext, preferencesLanguage, "en");
        }
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        mContext.getResources().updateConfiguration(config,
                mContext.getResources().getDisplayMetrics());
    }

    //Share song, album, artist and playelist
    public static void shareIt(Context context, String type, String title, String artist, String url) {
        MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(context, Constants.PROJECT_TOKEN);
        String data;
        if (type.equalsIgnoreCase("song")) {
            mixpanelAPI.track("Share Song");
            mixpanelAPI.flush();
            data = context.getString(R.string.checkout_mylistning) + " " + title + " " + context.getString(R.string.by) + " " + artist + " " + context.getString(R.string.on_shira_li) + " " + url;
        } else if (type.equalsIgnoreCase("album")) {
            data = context.getString(R.string.i_think_you_checkout_album) + " " + title + " " + context.getString(R.string.by) + " " + artist + " " + context.getString(R.string.on_shira_li) + " " + url;
        } else if (type.equalsIgnoreCase("playlist")) {
            data = context.getString(R.string.i_think_you_checkout) + " " + title + " " + context.getString(R.string.on_shira_li) + " " + url;
        } else {
            data = context.getString(R.string.i_think_you_checkout_artist) + " " + title + " " + context.getString(R.string.on_shira_li) + " " + url;
        }
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, data);
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }


    public static long timeDiffrence(String time1) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = simpleDateFormat.parse(getCurrentTime());
            date2 = simpleDateFormat.parse(time1);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        long difference = date1.getTime() - date2.getTime();
        int days = (int) (difference / (1000 * 60 * 60 * 24));
        int hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
        if (days == 0) {
            if (hours > 1) {
                return 60 * 60 * 1000;
            } else {
                return difference;
            }
        } else {
            return difference;
        }
    }

    //Session out dialog
    public static void openSessionOutDialog(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(context.getResources().getString(R.string.please_login_again));
        alertDialog.setMessage(context.getResources().getString(R.string.this_user_logged_in_another_device));
        alertDialog.setPositiveButton(context.getResources().getString(R.string.log_in),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserModel.getInstance().checkSessionOutDialogVisibility = true;
                        if (!((Activity) context).isFinishing()) {
                            dialog.dismiss();
                        }
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Runtime.getRuntime().gc();
                                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("cancel_notification"));
                                Utility.setBooleanPreferences(context, Utility.IS_LOGIN, false);
                                Utility.clearAllSharedPreferences(context);
                                Utility.setBooleanPreferences(context, "appInstall", true);
                                context.startActivity(new Intent(context, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Runtime.getRuntime().gc();
                                        try {
                                            Controls.stopControl(context);
                                            if (isServiceRunning(SongPlayService.class.getName(), context)) {
                                                context.stopService(new Intent(context, SongPlayService.class));
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 200); //200
                            }
                        }, 100); //1000
                    }
                });
        alertDialog.setCancelable(false);
        if (!((Activity) context).isFinishing()) {
            if (UserModel.getInstance().checkSessionOutDialogVisibility) {
                UserModel.getInstance().checkSessionOutDialogVisibility = false;
                alertDialog.show();
            }
        }
    }

    //Perform click on ad
    public static void clickAds(Context context, String ad_id) {
        UserModel.getInstance().clickOnAd(ad_id);
    }

    public static void showNoDataFound(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("");
        alertDialog.setMessage(R.string.data_not_found);
        alertDialog.setPositiveButton(R.string.again,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!((Activity) context).isFinishing()) {
                            dialog.dismiss();
                        }
                    }
                });
        if (!((Activity) context).isFinishing()) {
            alertDialog.show();
        }
    }


    //Check internet connection availability // KIPL -> AKM CHECK
    public static boolean isConnectingToInternet(final Context mContext) {
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
            }
        }
        if (!isDialogShow) {
            isDialogShow = true;
            showInternetAlert(mContext, mContext.getString(R.string.no_internet));
        }
        return false;
    }

    // Multiple APi call at same Screen i.e. the reason skip managed through isDialogShow // KIPL -> AKM
    public static boolean isConnectingToInternetOld(final Context mContext) {
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        isDialogShow = true;
                        return true;
                    }
            }
        }
        // If internet gone > app vary start without internet > dialog not show
        if (isDialogShow) {
            isDialogShow = false;
            showInternetAlert(mContext, mContext.getString(R.string.no_internet));
        }
        return false;
    }
    public static boolean isConnectingToInternetStatus(final Context mContext) {
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
            }
        }
        // If internet gone > app vary start without internet > dialog not show

        return false;
    }

    //Check internet connection availability without alert // KIPL -> AKM CHECK
    public static boolean isConnectingToInternetWithoutAlert(final Context mContext) {
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
            }
        }
        return false;
    }

    public static void showPopup(final Context context, String text) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.add_song_layout);
        TextView title = (TextView) dialog.findViewById(R.id.lytAdd);
        title.setText(text);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (!((Activity) context).isFinishing()) {
            dialog.show();
        }
        try {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Runtime.getRuntime().gc();
                    if (!((Activity) context).isFinishing()) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                }
            }, 2500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAlert(final Context context, String text) {
        final Dialog openDialog = new Dialog(context);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.alert_layout);
        TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
        TextView action = (TextView) openDialog.findViewById(R.id.lblOkay);
        title.setText(text);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!((Activity) context).isFinishing()) {
                    openDialog.dismiss();
                }
            }
        });
        if (!((Activity) context).isFinishing()) {
            openDialog.show();
        }
    }

    public static void showAlertWithCondition(final Context context, String text) {
        final Dialog openDialog = new Dialog(context);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.alert_layout);
        TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
        TextView action = (TextView) openDialog.findViewById(R.id.lblOkay);
        title.setText(text);
        action.setText(context.getResources().getString(R.string.go_back));
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ((Activity) context).finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!((Activity) context).isFinishing()) {
                    openDialog.dismiss();
                }
            }
        });
        if (!((Activity) context).isFinishing()) {
            openDialog.show();
        }
        openDialog.setCancelable(false);
    }

    //Show internet alert
    private static void showInternetAlert(final Context context, String text) {
        final Dialog openDialog = new Dialog(context);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.alert_layout);
        TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
        TextView action = (TextView) openDialog.findViewById(R.id.lblOkay);
        title.setText(text);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openBottomSheet(context);
                if (!((Activity) context).isFinishing()) {
                    openDialog.dismiss();
                    //AKM
                    isDialogShow = false;
                }
            }
        });
        if (!((Activity) context).isFinishing()) {
            openDialog.show();
        }
        openDialog.setCancelable(false);
    }

    private static void openBottomSheet(final Context context) {
        final Dialog d = new BottomSheetDialog(context);
        d.setContentView(R.layout.internet_connection_layout);
        View view = d.findViewById(R.id.bs);
        ((View) view.getParent()).setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Runtime.getRuntime().gc();
                if (Utility.isConnectingToInternet(context)) {
                    if (!((Activity) context).isFinishing()) {
                        if (d.isShowing()) {
                            d.dismiss();
                        }
                    }
                } else {
                    handler.postDelayed(this, 2000);
                }
            }
        }, 2000);
        d.setCancelable(false);
        if (!((Activity) context).isFinishing()) {
            d.show();
        }
    }

    //Send Local notification
    public static void sendNotification(Context ctx) {
        Intent intent = new Intent(ctx, SplashActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx);
        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.shirali_logo)
                .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.imglogo))
                .setContentTitle(ctx.getString(R.string.contineous_listing))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(ctx.getString(R.string.timer_done_music_smooth)))
                .setContentText(ctx.getString(R.string.timer_done_music_smooth))
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentInfo("Info");

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, b.build());
    }

    public static String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;
    }

    // Alert that show if user free for premium feature
    public static void openAlert(final Context mContext, String data) {
        final Dialog openDialog = new Dialog(mContext);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.subscription_alert);
        TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
        TextView msg = (TextView) openDialog.findViewById(R.id.lblMessage);
        TextView cancle = (TextView) openDialog.findViewById(R.id.lblNeverMind);
        TextView action = (TextView) openDialog.findViewById(R.id.lblSwitch);
        title.setText(mContext.getResources().getString(R.string.have_upgrade_me_today));
        title.setVisibility(View.GONE);
        msg.setText(data);
        action.setText(R.string.upgrade_premium);
        cancle.setText(mContext.getResources().getString(R.string.cancel));
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, YourSubscriptionActivity.class));
                if (!((Activity) mContext).isFinishing()) {
                    openDialog.dismiss();
                }
            }
        });
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();
            }
        });
        openDialog.setCanceledOnTouchOutside(false);
        openDialog.setCancelable(false);
        if (!((Activity) mContext).isFinishing()) {
            openDialog.show();
        }
    }

    // Alert that show if user free for join ShiraLi for repeat song
    public static void openRepeatAlert(final Context context, String data) {
        final Dialog openDialog = new Dialog(context);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.custom_alert);
        TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
        TextView cancle = (TextView) openDialog.findViewById(R.id.lblCancel);
        TextView action = (TextView) openDialog.findViewById(R.id.lblOkay);
        title.setText(data);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, YourSubscriptionActivity.class);
                intent.putExtra("month_subscription", Utility.getUserSetting(context).monthlySubscriptionPrice);
                intent.putExtra("year_subscription", Utility.getUserSetting(context).yearlySubscriptionPrice);
                context.startActivity(intent);
                if (!((Activity) context).isFinishing()) {
                    openDialog.dismiss();
                }
            }
        });
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();
            }
        });
        openDialog.setCanceledOnTouchOutside(false);
        openDialog.setCancelable(false);
        if (!((Activity) context).isFinishing()) {
            openDialog.show();
        }
    }

    //dialog for add to playlist
    public static void openAlertForAddToPlaylist(final Context mContext, String data) {
        final MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(mContext, Constants.PROJECT_TOKEN);
        final Dialog openDialog = new Dialog(mContext);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.custom_alert);
        TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
        TextView cancle = (TextView) openDialog.findViewById(R.id.lblCancel);
        TextView action = (TextView) openDialog.findViewById(R.id.lblOkay);
        title.setText(data);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mixpanelAPI.track("Add to Playlist: Go Premium");
                mixpanelAPI.flush();
                Intent intent = new Intent(mContext, YourSubscriptionActivity.class);
                intent.putExtra("month_subscription", Utility.getUserSetting(mContext).monthlySubscriptionPrice);
                intent.putExtra("year_subscription", Utility.getUserSetting(mContext).yearlySubscriptionPrice);
                mContext.startActivity(intent);
                if (!((Activity) mContext).isFinishing()) {
                    openDialog.dismiss();
                }
            }
        });
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();
            }
        });
        if (!((Activity) mContext).isFinishing()) {
            openDialog.show();
        }
    }

    //check service running or not
    public static boolean isServiceRunning(String serviceName, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean currentVersionSupportBigNotification() {
        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        if (sdkVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            return true;
        }
        return false;
    }

    public static boolean currentVersionSupportLockScreenControls() {
        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        if (sdkVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return true;
        }
        return false;
    }

    //Subscription dialog
    public static void showSubscriptionAlert(final Context context, String msg) {
        final Dialog openDialog = new Dialog(context);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.new_premium_popup_layout);
        TextView message = (TextView) openDialog.findViewById(R.id.lblMessage);
        TextView subscribe = (TextView) openDialog.findViewById(R.id.btnSubscribe);
        TextView not_now = (TextView) openDialog.findViewById(R.id.lblNotNow);
        message.setText(msg);
        subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, YourSubscriptionActivity.class);
                intent.putExtra("month_subscription", Utility.getUserSetting(context).monthlySubscriptionPrice);
                intent.putExtra("year_subscription", Utility.getUserSetting(context).yearlySubscriptionPrice);
                context.startActivity(intent);
                if (!((Activity) context).isFinishing()) {
                    openDialog.dismiss();
                }
            }
        });
        not_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();
            }
        });
        if (!((Activity) context).isFinishing()) {
            openDialog.show();
        }
    }

    //Like unlike alert
    public static void likeUnlikeAlert(final Context context, String text) {
        final Dialog openDialog = new Dialog(context);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.like_unlike_layout);
        TextView title = (TextView) openDialog.findViewById(R.id.lblTitle);
        TextView doNotNotify = (TextView) openDialog.findViewById(R.id.lblDoNotNotify);
        TextView gotIt = (TextView) openDialog.findViewById(R.id.lblGotIt);
        title.setText(Html.fromHtml(text));
        gotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();
            }
        });
        doNotNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!((Activity) context).isFinishing()) {
                    openDialog.dismiss();
                }
                Utility.setBooleanPreferences(context, "showLikeUnlikePopup", true);
            }
        });
        openDialog.setCancelable(false);
        openDialog.setCanceledOnTouchOutside(false);
        if (!((Activity) context).isFinishing()) {
            openDialog.show();
        }
    }

    //Send notification for no more skip from external player
    public static void sendNoMoreSkipNotification(Context ctx) {
        Intent intent = new Intent(ctx, YourSubscriptionActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx);
        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.shirali_logo)
                .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.imglogo))
                .setContentTitle(ctx.getString(R.string.subscribe_to_the_premium_package))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(ctx.getString(R.string.you_can_only_skip_three_songs_in_a_row_You_will_be_able_to_skip_more_songs)))
                .setContentText(ctx.getString(R.string.you_can_only_skip_three_songs_in_a_row_You_will_be_able_to_skip_more_songs))
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_LOW)
                .setContentInfo("Info");

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, b.build());
    }

    //Dialog for show artist is in progress with different function
    public static void openPremiumAlert(final Context context) {
        final Dialog openDialog = new Dialog(context);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.artist_popup_layout);
        TextView start = (TextView) openDialog.findViewById(R.id.lblNotifyAgain);
        TextView cancle = (TextView) openDialog.findViewById(R.id.lblGotIt);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!((Activity) context).isFinishing()) {
                    openDialog.dismiss();
                }
                ((Activity) context).finish();
            }
        });
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!((Activity) context).isFinishing()) {
                    openDialog.dismiss();
                }
                ((Activity) context).finish();
            }
        });
        if (!((Activity) context).isFinishing()) {
            openDialog.show();
        }
        openDialog.setCanceledOnTouchOutside(false);
        openDialog.setCancelable(false);
    }

    //Dialog for show artist is in progress
    public static void showArtistInProgress(final Context context) {
        final Dialog openDialog = new Dialog(context);
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        openDialog.setContentView(R.layout.artist_popup_layout);
        TextView start = (TextView) openDialog.findViewById(R.id.lblNotifyAgain);
        TextView cancle = (TextView) openDialog.findViewById(R.id.lblGotIt);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!((Activity) context).isFinishing()) {
                    openDialog.dismiss();
                }
            }
        });
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!((Activity) context).isFinishing()) {
                    openDialog.dismiss();
                }
            }
        });
        if (!((Activity) context).isFinishing()) {
            openDialog.show();
        }
        openDialog.setCanceledOnTouchOutside(false);
        openDialog.setCancelable(false);
    }

    //Force update dialog
    public static void showUpdateDialog(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.please_update));
        builder.setMessage(context.getResources().getString(R.string.there_is_an_update_available));
        builder.setPositiveButton(context.getResources().getString(R.string.update), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.shirali")));
                    dialog.dismiss();
                } catch (Exception e) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.shirali")));
                    dialog.dismiss();
                    e.printStackTrace();
                }
            }
        });
        builder.setCancelable(false);
        if (!((Activity) context).isFinishing()) {
            builder.show();
        }
    }

    // Append two text
    public static String setTextToTextView(String first, String second) {
        String dotString = "<font color=\"#0511E2\"> • </font>";
        if (first == null || first.equalsIgnoreCase("")) {
            return second;
        } else if (second == null || second.equalsIgnoreCase("")) {
            return first;
        } else {
            return first + dotString + second;
        }
    }

    //Get rounded image from bitmap
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 20;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /*"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"*/
    public static String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
    }

    public static boolean compareDates(String system_date, String curDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date strDate = sdf.parse(system_date);
        Date currentDate = sdf.parse(curDate);
        if (currentDate.before(strDate)) {
            return true;
        } else if (currentDate.equals(strDate)) {
            return true;
        } else {
            return false;
        }
    }

    //Check current date is expire for specific date
    public static boolean isExpire(String date) {
        if (date.isEmpty() || date.trim().equals("")) {
            return false;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy hh:mm:ss a"); // Jan-20-2015 1:30:55 PM
            Date d = null;
            Date d1 = null;
            String today = getToday("MMM-dd-yyyy hh:mm:ss a");
            try {
                d = sdf.parse(date);
                d1 = sdf.parse(today);
                if (d1.compareTo(d) < 0) {// not expired
                    return false;
                } else if (d.compareTo(d1) == 0) {// both date are same
                    if (d.getTime() < d1.getTime()) {// not expired
                        return false;
                    } else if (d.getTime() == d1.getTime()) {//expired
                        return true;
                    } else {//expired
                        return true;
                    }
                } else {//expired
                    return true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    //Get current date
    private static String getToday(String format) {
        Date date = new Date();
        return new SimpleDateFormat(format).format(date);
    }

    //Find differences between of two dates into days
    public static int getDifferenceDays(Date d1, Date d2) {
        int daysdiff = 0;
        long diff = d2.getTime() - d1.getTime();
        long diffDays = diff / (24 * 60 * 60 * 1000) /*+ 1*/;
        daysdiff = (int) diffDays;
        return daysdiff;
    }

    //Change format of date into specific one
    public static Date convertDateFormat(String data) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = simpleDateFormat.parse(data);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Load ad previously for instant play
    public static void getAdForFreeUser(final Context context, String user_id) {
        Call<Campaign> call = Constants.service.getAdsAccordingUser(user_id);
        call.enqueue(new Callback<Campaign>() {
            @Override
            public void onResponse(Call<Campaign> call, Response<Campaign> response) {
                isAdAlreadyFound = false;
                UserModel.getInstance().playerDuration = 0;
                if (response.isSuccessful()) {
                    Campaign campaign = response.body();
                    if (campaign != null && campaign.campaign != null && campaign.campaign.ads != null) {
                        UserModel.getInstance().ad = campaign.campaign;
                        try {
                            try {
                                if (Utility.mediaplayer != null) {
                                    Utility.mediaplayer.release();
                                    Utility.mediaplayer.reset();
                                    Utility.mediaplayer = null;
                                }
                                Utility.mediaplayer = new MediaPlayer();
                                Utility.mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                Utility.mediaplayer.setDataSource(campaign.campaign.ads.adFileUrl);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Utility.mediaplayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    Utility.mediaplayer.start();
                                    Utility.mediaplayer.pause();
                                    isAdAlreadyFound = true;
                                    UserModel.getInstance().playerDuration = mediaplayer.getDuration();
                                }
                            });
                            Utility.mediaplayer.prepareAsync();
                        } catch (IllegalArgumentException e) {
                            isAdAlreadyFound = false;
                            UserModel.getInstance().playerDuration = 0;
                            e.printStackTrace();
                        } catch (SecurityException e) {
                            isAdAlreadyFound = false;
                            UserModel.getInstance().playerDuration = 0;
                            e.printStackTrace();
                        } catch (IllegalStateException e) {
                            isAdAlreadyFound = false;
                            UserModel.getInstance().playerDuration = 0;
                            e.printStackTrace();
                        }
                    } else {
                        UserModel.getInstance().playerDuration = 0;
                        isAdAlreadyFound = false;
                    }
                } else {
                    if (Utility.mediaplayer != null) {
                        Utility.mediaplayer.reset();
                        Utility.mediaplayer.release();
                        Utility.mediaplayer = null;
                    }
                    UserModel.getInstance().playerDuration = 0;
                    isAdAlreadyFound = false;
                }
            }

            @Override
            public void onFailure(Call<Campaign> call, Throwable t) {
                t.printStackTrace();
                if (Utility.mediaplayer != null) {
                    Utility.mediaplayer.reset();
                    Utility.mediaplayer.release();
                    Utility.mediaplayer = null;
                }
                UserModel.getInstance().playerDuration = 0;
                isAdAlreadyFound = false;
            }
        });
    }


    //Send battery alert notification
    public static void sendBatteryNotification(Context ctx) {
        Intent intent = new Intent(ctx, SplashActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx);
        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.shirali_logo)
                .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.imglogo))
                .setContentTitle(ctx.getString(R.string.battery_discharged))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(ctx.getString(R.string.interruption_in_music)))
                .setContentText(ctx.getString(R.string.interruption_in_music_short))
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentInfo("Info");

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, b.build());
    }

    public static void playFullPlayListShuffle(Intent intent, Context mContext, CustomBottomTabView playerView,
                                         ArrayList<Song> getSonglist, Shirali shirali) {
        if (intent.hasExtra("list_song")) {
            Constants.SONGS_LIST = getSonglist;
        } else {
            Constants.SONGS_LIST = shirali.songs;
        }
        UserModel.getInstance().listOfShuffleSong.clear();
        UserModel.getInstance().listOfShuffleSong.addAll(Constants.SONGS_LIST);
        Collections.shuffle(UserModel.getInstance().listOfShuffleSong);
        UserModel.getInstance().listOfActualSong.clear();
        UserModel.getInstance().listOfActualSong.addAll(Constants.SONGS_LIST);
        playerView.setPlayerData(Constants.SONGS_LIST);
        Constants.SONG_NUMBER = 0; // random can be applied if needed
        Constants.isSongPlay = true;
        //Constants.isListHitFirstTime = true;
        Constants.SONG_CHANGE_HANDLER.sendMessage(Constants.SONG_CHANGE_HANDLER.obtainMessage());
        try {
            UserModel.getInstance().addInRecentWithType(mContext, Utility.getUserInfo(mContext).id, shirali.id, "playlist");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}