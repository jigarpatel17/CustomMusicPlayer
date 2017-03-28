package com.app.custommusicplayer.database;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by mind on 7/2/17.
 */

public class DBMethods {
    public static SongFolder getNextSongFolder(String folder_path) {
        Realm realm = Realm.getDefaultInstance();
        if (realm != null && !realm.isClosed()) {
            RealmResults<SongFolder> songFolders = realm.where(SongFolder.class)
                    .equalTo("folder_path", folder_path).findAllSorted("folder_name", Sort.DESCENDING);
            if (songFolders != null && !songFolders.isEmpty()) {
                boolean isFolderFound = false;
                SongFolder sendingData = null;
                for (SongFolder songFolder : songFolders) {
                    if (songFolder.getFolder_path().equals(folder_path)) {
                        isFolderFound = true;
                        sendingData = songFolder;
                    }
                }
                if (!isFolderFound) {
                    Realm realm1 = Realm.getDefaultInstance();
                    realm1.beginTransaction();
                    sendingData = new SongFolder();
                    sendingData.setFolder_path(folder_path);
                    realm1.copyToRealmOrUpdate(sendingData);
                    realm1.commitTransaction();
                }
                return sendingData;
            } else {
                Realm realm2 = Realm.getDefaultInstance();
                realm2.beginTransaction();
                SongFolder sendingData = new SongFolder();
                sendingData.setFolder_path(folder_path);
                realm2.copyToRealmOrUpdate(sendingData);
                realm2.commitTransaction();
                return sendingData;
            }
        } else {
            return null;
        }
    }

    public static long getSongCounts(String folder_path) {
        Realm realm = Realm.getDefaultInstance();
        if (realm != null && !realm.isClosed()) {
            RealmResults<Song> songs = realm.where(Song.class).equalTo("folder_path", folder_path).findAll();
            if (songs != null && !songs.isEmpty()) {
                return songs.size();
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public static void addSongInQueue(String song_path) {
        Realm realm = Realm.getDefaultInstance();
        if (realm != null && !realm.isClosed()) {
            realm.beginTransaction();
            SongsQueue songsQueue = new SongsQueue();
            songsQueue.setPath(song_path);
            realm.copyToRealmOrUpdate(songsQueue);
            realm.commitTransaction();
        }
    }

    public static void clearSongQueue() {
        Realm realm = Realm.getDefaultInstance();
        if (realm != null && !realm.isClosed()) {
            realm.beginTransaction();
            realm.delete(SongsQueue.class);
            realm.commitTransaction();
        }
    }

    public static void updatePlayingState(String folder_path, String song_path) {
        Realm realm = Realm.getDefaultInstance();
        if (realm != null && !realm.isClosed()
                && folder_path != null && !folder_path.isEmpty()
                && song_path != null) {

            RealmResults<Song> songs = realm.where(Song.class)
                    .equalTo("folder_path", folder_path)
                    .findAll();

            for (Song song : songs) {
                if (song != null) {
                    realm.beginTransaction();
                    if (song.getPath().equalsIgnoreCase(song_path)) {
                        song.setPlaying(true);
                    } else {
                        song.setPlaying(false);
                    }
                    realm.copyToRealmOrUpdate(song);
                    realm.commitTransaction();
                }
            }

        }
    }
}
