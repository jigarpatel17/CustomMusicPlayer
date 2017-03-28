package com.app.custommusicplayer.database;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mind on 8/2/17.
 */

public class SongsQueue extends RealmObject {
    @PrimaryKey
    private String path;
    private Song song;

    public Song getSong(String path) {
        if (song == null) {
            Realm realm = Realm.getDefaultInstance();
            song = realm.where(Song.class).equalTo("path", path).findFirst();
        }
        return song;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
