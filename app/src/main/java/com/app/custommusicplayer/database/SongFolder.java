package com.app.custommusicplayer.database;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mind on 7/2/17.
 */

public class SongFolder extends RealmObject {
    @PrimaryKey
    private String folder_path;
    private String folder_name;
    private RealmList<Song> songList;
    private int fontColor;
    private int backColor;

    public int getFontColor() {
        return fontColor;
    }

    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
    }

    public int getBackColor() {
        return backColor;
    }

    public void setBackColor(int backColor) {
        this.backColor = backColor;
    }

    public RealmList<Song> getSongList() {
        return songList;
    }

    public void setSongList(List<Song> songList) {
        this.songList = (RealmList<Song>) songList;
    }

    public String getFolder_name() {
        return folder_name;
    }

    public void setFolder_name(String folder_name) {
        this.folder_name = folder_name;
    }

    public String getFolder_path() {
        return folder_path;
    }

    public void setFolder_path(String folder_path) {
        this.folder_path = folder_path;
    }
}
