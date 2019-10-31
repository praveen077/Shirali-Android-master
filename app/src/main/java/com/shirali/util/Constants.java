package com.shirali.util;

import android.os.Handler;

import com.shirali.model.songs.Song;
import com.shirali.model.stations.Stations;
import com.shirali.network.Api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Sagar on 3/7/17.
 */

public class Constants {

    // true if activity resumed
    public static boolean isActivityResume = false;
    public static boolean isListHitFirstTime = false; /* --- KIPL -> AKM: Stop shuffle very first time in the list ---*/
    // true if song complete
    public static boolean isSongComplete = false;
    public static boolean isFirstLoad = false;
    public static String GOOGLE_SENDER_ID = "527217682522";

    //cloud token to genreate song url
    public static String song_cloud_token = "ON2JnMgtEcNGdpmCCxND";

    // Stripe key for live and staging
    public static String stripe_test_key = "pk_test_hXZpjA4OA6gb1bhdv7Tm2Aud";
    public static String stripe_Publish_Live_Key = "pk_live_6nUgVaIDxEKYJDB7letGHpOD";

    //DEV
    /*public static String baseServerLiveUrl = "http://18.222.48.249:3001/v2/";
    public static String socketUrl = "http://18.222.48.249:3001/";
    public static String songUrl = "http://18.222.48.249:3001/v2/song/getSignedUrl";
    public static String PROJECT_TOKEN = "4d7e81428e690b09b243cdecc952a9a4";*/
    //Staging
    /*public static String baseServerLiveUrl = "http://stagmapi.shiraliapp.com/v2/";
    public static String socketUrl = "http://stagmapi.shiraliapp.com";
    public static String songUrl = "http://stagmapi.shiraliapp.com/v2/song/getSignedUrl";
    public static String PROJECT_TOKEN = "4d7e81428e690b09b243cdecc952a9a4";*/

    //"http://d15cf3tnir8i53.cloudfront.net/v2/"; // AKM
    // CDN Production Urls
    public static String baseServerLiveUrl = "https://d1lfer5iumgfim.cloudfront.net/v2/";
    public static String socketUrl = "https://d15cf3tnir8i53.cloudfront.net";
    public static String songUrl = "https://d15cf3tnir8i53.cloudfront.net/v2/song/getSignedUrl";
    public static String PROJECT_TOKEN = "d08f70c4f8fdcc2ef89711f150fe32ed";

    //Production v1
    /*public static String baseServerLiveUrl = "http://prodmapi.shiraliapp.com/v1/";
    public static String socketUrl = "http://prodmapi.shiraliapp.com";
    public static String songUrl = "http://prodmapi.shiraliapp.com/v1/song/getSignedUrl";
    public static String PROJECT_TOKEN = "d08f70c4f8fdcc2ef89711f150fe32ed";*/

    private static String radioArtworkUrl = "https://itunes.apple.com/";

    private static final OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
            .connectTimeout(120000, TimeUnit.MILLISECONDS)
            .addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request.Builder ongoing = chain.request().newBuilder();
                    if (isUserLogged) {
                        ongoing.addHeader("Accept", "application/json");
                        ongoing.addHeader("userid", user_id);
                        ongoing.addHeader("appsecret", "eSrrxqt8MVAdJB6Xq9wzJZXdFq89MZo6");
                        ongoing.addHeader("deviceid", device_id);
                        ongoing.addHeader("devicetype", "android");
                    } else {
                        ongoing.addHeader("Accept", "application/json");
                        ongoing.addHeader("appsecret", "eSrrxqt8MVAdJB6Xq9wzJZXdFq89MZo6");
                        ongoing.addHeader("devicetype", "android");
                    }
                    return chain.proceed(ongoing.build());
                }
            })
            .readTimeout(120000, TimeUnit.MILLISECONDS)
            .writeTimeout(120000, TimeUnit.MILLISECONDS)
            .connectionPool(new ConnectionPool(50, 50000, TimeUnit.MILLISECONDS))
            .build();
    public static final Api service = new Retrofit.Builder()
            .baseUrl(baseServerLiveUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(Api.class);

    private static final OkHttpClient okClient = new OkHttpClient().newBuilder()
            .connectTimeout(120000, TimeUnit.MILLISECONDS)
            .addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request.Builder ongoing = chain.request().newBuilder();
                    return chain.proceed(ongoing.build());
                }
            })
            .readTimeout(120000, TimeUnit.MILLISECONDS)
            .writeTimeout(120000, TimeUnit.MILLISECONDS)
            .connectionPool(new ConnectionPool(50, 50000, TimeUnit.MILLISECONDS))
            .build();
    public static final Api v2service = new Retrofit.Builder()
            .baseUrl(radioArtworkUrl)
            .client(okClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(Api.class);

    public static void setLoggedUser(boolean check, String id, String device) {
        isUserLogged = check;
        user_id = id;
        device_id = device;
    }

    //Player Constant
    //List of Songs
    public static ArrayList<Song> SONGS_LIST = new ArrayList<Song>();
    //song number which is playing right now from SONGS_LIST
    public static int SONG_NUMBER = 0;
    //handler for song changed(next, previous) defined in service(SongService)
    public static Handler SONG_CHANGE_HANDLER = new Handler();
    //handler for song play/pause defined in service(SongService)
    public static Handler PLAY_PAUSE_HANDLER = new Handler();
    //handler for showing song progress defined in Activities(MainActivity, AudioPlayerActivity)
    public static Handler PROGRESSBAR_HANDLER = new Handler();
    // return true if player play song
    public static boolean isPlay = false;
    // if for reset seek bar
    public static boolean resetSeekBar = false;
    // check is repeat mode activate or not
    public static boolean repeat = false;
    // check is shuffle mode activate or not
    public static boolean shuffel = false;
    // seek to seek bar at specific position
    public static int seekTo;
    public static boolean isPrevious = false;
    //Maintain song id for highlight currently played song
    public static String song = "";
    // Check song is play or not for remote player
    public static boolean isSongPlaying = false;
    // true if next song play after ad
    public static boolean isChangeSong = false;
    // it is true if ad is show for remote player
    public static boolean adShow = false;
    // it is used
    public static boolean isPageSelectedFromNextOfPrevious = true;
    // Select song number for play after ad
    public static int songSelectionNumberAfterAd = 0;
    public static boolean openSubsWindowFromAds = false;
    //List of Station
    public static ArrayList<Stations> StationList = new ArrayList<Stations>();
    //Boolean that check song or album play
    public static boolean isSongPlay = false;
    public static boolean isHomeScreenPlayerVisible = false; /* --- KIPL -> AKM: Manage bottom Player visible/gone ---*/
    private static boolean isUserLogged = false;
    private static String user_id;
    private static String device_id;

}
