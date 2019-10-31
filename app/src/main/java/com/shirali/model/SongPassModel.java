package com.shirali.model;

import com.shirali.model.songs.Song;

import java.util.ArrayList;

/**
 * Created by Sagar on 13/9/17.
 */

public class SongPassModel {
    public static ArrayList<Song> arraylist;

    public static void setArraylist(ArrayList<Song> arraylist) {
        SongPassModel.arraylist = arraylist;
    }

    public static ArrayList<Song> getArraylist() {
        return arraylist;
    }
}
