package com.shirali.model;

import java.io.Serializable;

/**
 * Created by user on 10/7/17.
 */

public class MyMusicModel implements Serializable {

    public String artistName;
    public String artistAlbumNo;
    public String artistAlbumSongs;

    public MyMusicModel(String artistName, String artistAlbumNo, String artistAlbumSongs) {
        this.artistName = artistName;
        this.artistAlbumNo = artistAlbumNo;
        this.artistAlbumSongs = artistAlbumSongs;
    }
}
