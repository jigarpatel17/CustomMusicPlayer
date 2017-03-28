package com.app.custommusicplayer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.custommusicplayer.R;
import com.app.custommusicplayer.database.DBMethods;
import com.app.custommusicplayer.database.Song;
import com.app.custommusicplayer.util.CommonKeys;
import com.app.custommusicplayer.util.CommonMethods;

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

public class SongsQueueFragment extends BaseFragment implements View.OnClickListener {
    @BindView(R.id.recyclerView)
    RealmRecyclerView recyclerView;

    @BindView(R.id.inc_back_arrow)
    ImageButton inc_back_arrow;

    @BindView(R.id.inc_tv_title)
    TextView inc_tv_title;

    String title;
    String folder_path;
    Realm realm;

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

        // getting songs list from db for particular folder.
        RealmResults<Song> songs = null;
        if (folder_path != null && !folder_path.isEmpty()) {
            songs = realm
                    .where(Song.class)
                    .equalTo("folder_path", folder_path)
                    .findAllSorted("displayName", Sort.ASCENDING);
        }


        RecyclerViewAdapter adapter = new RecyclerViewAdapter(
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
                String time = CommonMethods.getSongTime(song.getSongDuration());
                if (time != null && !time.isEmpty()) {
                    viewHolder.tv_songDuration.setText(time);
                }
            }

            viewHolder.imgbtn_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //creating a popup menu
                    final PopupMenu popup = new PopupMenu(mActivity, viewHolder.imgbtn_more);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.queue_menu);
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
            });
        }

        class ViewHolder extends RealmViewHolder implements View.OnCreateContextMenuListener {
            @BindView(R.id.ll_main)
            LinearLayout ll_main;

            @BindView(R.id.tv_songName)
            TextView tv_songName;

            @BindView(R.id.tv_songDuration)
            TextView tv_songDuration;

            @BindView(R.id.frame_play_pause)
            FrameLayout frame_play_pause;

            @BindView(R.id.imgbtn_play)
            ImageButton imgbtn_play;

            @BindView(R.id.imgbtn_pause)
            ImageButton imgbtn_pause;

            @BindView(R.id.imgbtn_more)
            ImageButton imgbtn_more;

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
