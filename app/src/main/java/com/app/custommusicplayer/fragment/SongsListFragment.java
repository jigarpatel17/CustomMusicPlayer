package com.app.custommusicplayer.fragment;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.custommusicplayer.R;
import com.app.custommusicplayer.database.DBMethods;
import com.app.custommusicplayer.database.Song;
import com.app.custommusicplayer.util.CommonKeys;
import com.app.custommusicplayer.util.CommonMethods;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;
import io.realm.Sort;

/**
 * Created by mind on 8/2/17.
 */

public class SongsListFragment extends BaseFragment implements View.OnClickListener {
    private static final long MIN_DELAY_MS = 200;
    @BindView(R.id.recyclerView)
    RealmRecyclerView recyclerView;
    @BindView(R.id.inc_back_arrow)
    ImageButton inc_back_arrow;
    @BindView(R.id.inc_tv_title)
    TextView inc_tv_title;
    String title;
    String folder_path;
    Realm realm;
    MediaPlayer mediaPlayer = new MediaPlayer();
    Timer timer;
    Runnable runnable;
    private long mLastClickTime;
    private long current_playing_position = -1;
    RecyclerViewAdapter adapter;

    public static SongsListFragment getInstance(String title, String folder_path) {
        SongsListFragment songsListFragment = new SongsListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CommonKeys.TITLE, title);
        bundle.putString(CommonKeys.FOLDER_PATH, folder_path);
        songsListFragment.setArguments(bundle);
        return songsListFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragement_songs_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        title = CommonKeys.TITLE;
        getBundleData();
    }

    void getBundleData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(CommonKeys.TITLE)) {
                title = bundle.getString(CommonKeys.TITLE);
                inc_tv_title.setText(title);
            }

            if (bundle.containsKey(CommonKeys.FOLDER_PATH)) {
                folder_path = bundle.getString(CommonKeys.FOLDER_PATH);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // here we have initiated realm.
        realm = Realm.getDefaultInstance();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // getting songs list from db for particular folder.
        RealmResults<Song> songs = null;
        if (folder_path != null && !folder_path.isEmpty()) {
            songs = realm
                    .where(Song.class)
                    .equalTo("folder_path", folder_path)
                    .findAllSorted("displayName", Sort.ASCENDING);
        }


        adapter = new RecyclerViewAdapter(
                mActivity,
                songs,
                true,
                false,
                null);

        recyclerView.setAdapter(adapter);

        handleClickListener();
    }

    void handleClickListener() {
        inc_back_arrow.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.inc_back_arrow:
                mActivity.onBackPressed();
                break;
        }
    }

    void playSong(final Song song, final TextView textView, final ProgressBar progressbar) {
        if (song.getPath() != null && !song.getPath().isEmpty()) {
            try {

                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                }

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }

                mediaPlayer.setDataSource(mActivity, Uri.parse(song.getPath()));
                mediaPlayer.prepare();
                mediaPlayer.start();


                timer = new Timer();

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            int milliseconds = mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition();
                            int seconds = (int) (milliseconds / 1000) % 60;
                            int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
                            int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
                            String time = "";

                            if (hours > 0) {
                                time += String.format("%02d", hours)+ ":";
                            }
                            time += String.format("%02d", minutes) + ":"
                                    + String.format("%02d", seconds);

                            textView.setVisibility(View.VISIBLE);
                            textView.setText(time);

                            int progress = (int) (mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration());
                            if (progress<=100){
                                progressbar.setProgress(progress);
                            }
                        } else {
                            timer.cancel();
                            timer.purge();
                            stopSong(song);
                            progressbar.setVisibility(View.GONE);
                            progressbar.setProgress(0);
                            textView.setVisibility(View.GONE);
                            textView.setText("");
//                            Toast.makeText(mActivity, "Song Completed",Toast.LENGTH_SHORT).show();
                        }
                    }
                };

                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        mActivity.runOnUiThread(runnable);
                    }
                }, 0, 1000);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void stopSong(Song song) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }

        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        if (song != null) {
            realm.beginTransaction();
            song.setPlaying(false);
            realm.copyToRealmOrUpdate(song);
            realm.commitTransaction();
        } else {
            DBMethods.updatePlayingState(folder_path, "");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopSong(null);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public class RecyclerViewAdapter extends RealmBasedRecyclerViewAdapter<Song,
            RecyclerViewAdapter.ViewHolder> {

        RecyclerViewAdapter(
                Context context,
                RealmResults<Song> realmResults,
                boolean automaticUpdate,
                boolean animateIdType,
                String animateExtraColumnName) {
            super(context, realmResults, automaticUpdate, animateIdType, animateExtraColumnName);
        }

        @Override
        public RecyclerViewAdapter.ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int viewType) {
            return new RecyclerViewAdapter.ViewHolder(inflater.inflate(R.layout.row_song_list, viewGroup, false));
        }

        @Override
        public void onBindRealmViewHolder(final RecyclerViewAdapter.ViewHolder viewHolder, final int position) {

            final Song song = realmResults.get(position);
            if (song != null) {
                viewHolder.tv_songName.setText(song.getTitle());
                Log.d(TAG,"path: " + song.getPath());
                String time = CommonMethods.getSongTime(song.getSongDuration());
                if (time != null && !time.isEmpty()) {
                    viewHolder.tv_songDuration.setText(time);
                }

                viewHolder.progressbar.getProgressDrawable().setColorFilter(
                        Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);

                if (current_playing_position == position) {
                    viewHolder.imgbtn_play.setVisibility(View.GONE);
                    viewHolder.imgbtn_pause.setVisibility(View.VISIBLE);
                    viewHolder.tv_songDurationRemaining.setVisibility(View.VISIBLE);
                    viewHolder.progressbar.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.imgbtn_play.setVisibility(View.VISIBLE);
                    viewHolder.imgbtn_pause.setVisibility(View.GONE);
                    viewHolder.tv_songDurationRemaining.setText("");
                    viewHolder.tv_songDurationRemaining.setVisibility(View.GONE);
                    viewHolder.tv_songDurationRemaining.removeCallbacks(runnable);
                    viewHolder.progressbar.removeCallbacks(runnable);
                    viewHolder.progressbar.setVisibility(View.GONE);
                }

                viewHolder.imgbtn_more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long lastClickTime = mLastClickTime;
                        long now = System.currentTimeMillis();
                        mLastClickTime = now;
                        if (now - lastClickTime < MIN_DELAY_MS) {
                            // Too fast: ignore
                        } else {
                            //creating a popup menu
                            final PopupMenu popup = new PopupMenu(mActivity, viewHolder.imgbtn_more);
                            //inflating menu from xml resource
                            popup.inflate(R.menu.options_menu);
                            //adding click listener
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.add_to_queue:
                                            DBMethods.addSongInQueue(realmResults.get(position).getPath());
                                            break;
                                        case R.id.play:
                                            DBMethods.clearSongQueue();
                                            DBMethods.addSongInQueue(realmResults.get(position).getPath());
                                            break;
                                    }
                                    return false;
                                }
                            });
                            //displaying the popup
                            popup.show();
                        }
                    }
                });

                viewHolder.frame_play_pause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long lastClickTime = mLastClickTime;
                        long now = System.currentTimeMillis();
                        mLastClickTime = now;
                        if (now - lastClickTime < MIN_DELAY_MS) {
                            // Too fast: ignore
                        } else {
                            int old_position = CommonMethods.safeLongToInt(current_playing_position);
                            if (!song.isPlaying()) {
                                if (song.getPath() != null && !song.getPath().isEmpty()) {
                                    playSong(song, viewHolder.tv_songDurationRemaining, viewHolder.progressbar);
                                    current_playing_position = position;
                                    DBMethods.updatePlayingState(song.getFolder_path(), song.getPath());
                                } else {
                                    current_playing_position = -1;
                                    Toast.makeText(mActivity, "Song is empty", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                current_playing_position = -1;
                                viewHolder.tv_songDurationRemaining.removeCallbacks(runnable);
                                viewHolder.progressbar.removeCallbacks(runnable);
                                stopSong(song);
                            }
                            adapter.notifyItemChanged(old_position);
                            adapter.notifyItemChanged(CommonMethods.safeLongToInt(current_playing_position));
                        }
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        class ViewHolder extends RealmViewHolder implements View.OnCreateContextMenuListener {
            @BindView(R.id.ll_main)
            LinearLayout ll_main;

            @BindView(R.id.tv_songName)
            TextView tv_songName;

            @BindView(R.id.tv_songDuration)
            TextView tv_songDuration;

            @BindView(R.id.tv_songDurationRemaining)
            TextView tv_songDurationRemaining;

            @BindView(R.id.frame_play_pause)
            FrameLayout frame_play_pause;

            @BindView(R.id.imgbtn_play)
            ImageButton imgbtn_play;

            @BindView(R.id.imgbtn_pause)
            ImageButton imgbtn_pause;

            @BindView(R.id.imgbtn_more)
            ImageButton imgbtn_more;

            @BindView(R.id.progressbar)
            ProgressBar progressbar;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                //menuInfo is null
                menu.add(R.string.add_to_Queue);
                menu.add(R.string.play);
            }
        }
    }
}
