package com.shirali.model;

/**
 * Created by user on 10/7/17.
 */

public class MySongsModel {

    public String songName;
    public String songTag;
    public String singerName;
    public String albumName;

    public MySongsModel(String songName, String songTag, String singerName, String albumName) {
        this.songName = songName;
        this.songTag = songTag;
        this.singerName = singerName;
        this.albumName = albumName;
    }
}
