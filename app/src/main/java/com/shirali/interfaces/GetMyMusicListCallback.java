package com.shirali.interfaces;

import java.util.ArrayList;

/**
 * Created by Sagar on 1/5/18.
 */

public interface GetMyMusicListCallback {
    void addedToMusic(boolean isAdded, ArrayList<String> myMusic);
}
