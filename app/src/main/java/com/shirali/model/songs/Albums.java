package com.shirali.model.songs;

/**
 * Created by Sagar on 24/7/17.
 */
public class Albums {

    public String artwork,createDate,title,updateDate,keyword;
    public Boolean isActive,isExclusive,isNewRelease;

    public Albums(String artwork, String createDate, String title, String updateDate, String keyword, Boolean isActive, Boolean isExclusive, Boolean isNewRelease){
        this.artwork = artwork;
        this.createDate = createDate;
        this.title = title;
        this.updateDate = updateDate;
        this.keyword = keyword;
        this.isActive = isActive;
        this.isExclusive = isExclusive;
        this.isNewRelease = isNewRelease;
    }
}
