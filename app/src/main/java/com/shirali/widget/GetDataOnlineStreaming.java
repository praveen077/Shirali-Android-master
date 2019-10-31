package com.shirali.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.shirali.model.radio.Radio;
import com.shirali.model.radio.RadioTempData;
import com.shirali.model.user.UserModel;
import com.shirali.util.Constants;

import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetDataOnlineStreaming extends AsyncTask<URL, Void, Void> {
    @SuppressLint("StaticFieldLeak")
    private Context context;

    public GetDataOnlineStreaming(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(URL... urls) {
        try {
            URL url = urls[0];
            ParshingHeaderData streaming = new ParshingHeaderData();
            ParshingHeaderData.TrackData trackData = streaming.getTrackDetails(url);
            UserModel.getInstance().artistName = trackData.artist;
            UserModel.getInstance().title = trackData.title;
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("update_metadata"));
            if (!trackData.artist.equalsIgnoreCase("") && !trackData.title.equalsIgnoreCase("")) {
                getArtwork(trackData.artist + " - " + trackData.title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void getArtwork(String name) {
        Call<Radio> call = Constants.v2service.getRadioArtwork(name, "song");
        call.enqueue(new Callback<Radio>() {
            @Override
            public void onResponse(Call<Radio> call, Response<Radio> response) {
                UserModel.getInstance().tempData = new RadioTempData();
                UserModel.getInstance().artwork = "";
                if (response.isSuccessful()) {
                    Radio radio = response.body();
                    if (radio.results.size() > 0) {
                        String artworkString = radio.results.get(0).artworkUrl100;
                        artworkString = artworkString.replace("100x100", "600x600");
                        UserModel.getInstance().artwork = artworkString;
                    }
                }
            }

            @Override
            public void onFailure(Call<Radio> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}