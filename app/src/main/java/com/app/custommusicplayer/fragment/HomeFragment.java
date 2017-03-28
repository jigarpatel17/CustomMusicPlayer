package com.app.custommusicplayer.fragment;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.custommusicplayer.R;
import com.app.custommusicplayer.database.DBMethods;
import com.app.custommusicplayer.database.Song;
import com.app.custommusicplayer.database.SongFolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by mind on 7/2/17.
 */

public class HomeFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.btnStartFetchingSongs)
    Button btnStartFetchingSongs;

    @BindView(R.id.progressbar)
    ProgressBar progressbar;

    Realm realm;
    boolean isDataFetchingRunning = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        btnStartFetchingSongs.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // here we have initiated realm.
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartFetchingSongs:
                loadMusic();
                break;
        }
    }

    private void loadMusic() {
        if (isDataFetchingRunning){
            Toast.makeText(mActivity,"Please wait, we are fetching data.", Toast.LENGTH_SHORT ).show();
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                isDataFetchingRunning = true;
                scanDeviceForMp3Files();
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressbar.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                isDataFetchingRunning = false;
                progressbar.setVisibility(View.GONE);
                // adding home fragment.
                mActivity.addFragment(new SongsFolderFragment(), false, true);

            }
        }.execute();
    }

    private List<Song> scanDeviceForMp3Files() {
        List<Song> songList = new ArrayList<>();
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";

        Cursor cursor = null;
        try {
            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            cursor = mActivity.getContentResolver().query(uri, projection, selection, null, sortOrder);
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String title = cursor.getString(0);
                    String artist = cursor.getString(1);
                    String path = cursor.getString(2);
                    String displayName = cursor.getString(3);
                    String songDuration = cursor.getString(4);
                    cursor.moveToNext();
                    if (path != null && path.endsWith(".mp3")) {

                        String[] splitPath;
                        if (path != null && !path.equalsIgnoreCase(" ") &&
                                path.contains("/")) {
                            splitPath = path.split("/");
                            if (splitPath.length > 1){
                                int hierarchy_size = splitPath.length;
                                if (hierarchy_size>2){

                                    // here we are making whole folder_path;
                                    String folderPath = "";
                                    for (int i = 0; i < hierarchy_size-2; i++) {
                                        folderPath+=splitPath[i];
                                        if (i != hierarchy_size -2){
                                            folderPath+="/";
                                        }
                                    }

                                    String folderName = folderPath;
                                    if (folderPath.contains("/")){
                                        String[] f_name = folderName.split("/");
                                        folderName = f_name[f_name.length-1];
                                    }

                                    String songName = splitPath[hierarchy_size-1];
                                    Realm realm = Realm.getDefaultInstance();
                                    SongFolder songFolder = DBMethods.getNextSongFolder(folderPath);
                                    realm.beginTransaction();
                                    songFolder.setFolder_name(folderName);
                                    Random rnd = new Random();
                                    int red_random = rnd.nextInt(256);
                                    int green_random = rnd.nextInt(256);
                                    int blue_random = rnd.nextInt(256);

                                    int color = Color.argb(255, red_random, green_random, blue_random);
                                    songFolder.setBackColor(color);

                                    if ((red_random * 0.299) + (green_random * 0.587) + (blue_random * 0.114) > 186) {
                                        songFolder.setFontColor(mActivity.getResources().getColor(android.R.color.black));
                                    } else {
                                        songFolder.setFontColor(mActivity.getResources().getColor(android.R.color.white));
                                    }
//                                    songFolder.setFolder_path(folderPath);
                                    Song song = new Song();
                                    song.setTitle(title);
                                    song.setArtist(artist);
                                    song.setDisplayName(displayName);
                                    song.setPath(path);
                                    song.setSongDuration(songDuration);
                                    song.setFolder_path(folderPath);

                                    RealmList<Song> songsList = songFolder.getSongList();
                                    if (songsList == null){
                                        songsList = new RealmList<>();
                                    }
                                    songsList.add(song);
                                    songFolder.setSongList(songsList);
                                    realm.copyToRealmOrUpdate(songFolder);
                                    realm.commitTransaction();
                                }
                            }
                        }
                    }
                }
            }

            // print to see list of mp3 files
            for (Song song : songList) {
                Log.i(TAG, song.getPath());
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return songList;
    }
}
