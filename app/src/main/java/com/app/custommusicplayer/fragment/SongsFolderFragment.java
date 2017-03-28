package com.app.custommusicplayer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.custommusicplayer.R;
import com.app.custommusicplayer.database.DBMethods;
import com.app.custommusicplayer.database.SongFolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;
import io.realm.Sort;

/**
 * Created by mind on 7/2/17.
 */

public class SongsFolderFragment extends BaseFragment {
    @BindView(R.id.recyclerView)
    RealmRecyclerView recyclerView;

    Realm realm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragement_folder_songs, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // here we have initiated realm.
        realm = Realm.getDefaultInstance();
        RealmResults<SongFolder> songFolders = realm
                .where(SongFolder.class)
                .findAllSorted("folder_name", Sort.ASCENDING);

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(
                mActivity,
                songFolders,
                true,
                false,
                null);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, 2);
        recyclerView.getRecycleView().setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    public class RecyclerViewAdapter extends RealmBasedRecyclerViewAdapter<SongFolder,
            RecyclerViewAdapter.ViewHolder> {

        RecyclerViewAdapter(
                Context context,
                RealmResults<SongFolder> realmResults,
                boolean automaticUpdate,
                boolean animateIdType,
                String animateExtraColumnName) {
            super(context, realmResults, automaticUpdate, animateIdType, animateExtraColumnName);
        }

        @Override
        public RecyclerViewAdapter.ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int viewType) {
            return new RecyclerViewAdapter.ViewHolder(inflater.inflate(R.layout.row_media_folder, viewGroup, false));
        }

        @Override
        public void onBindRealmViewHolder(RecyclerViewAdapter.ViewHolder viewHolder, final int position) {

            final SongFolder songFolder = realmResults.get(position);
            if (songFolder != null) {
                viewHolder.tv_foldername.setText(songFolder.getFolder_name());
                long count = DBMethods.getSongCounts(songFolder.getFolder_path());
                String count_text = "";
                if (count == 1) {
                    count_text = "1 song";
                } else {
                    count_text = count + " songs";
                }
                viewHolder.tv_songs_count.setText(count_text);
                viewHolder.ll_main.setBackgroundColor(songFolder.getBackColor());
                viewHolder.tv_songs_count.setTextColor(songFolder.getFontColor());
                viewHolder.tv_foldername.setTextColor(songFolder.getFontColor());

                viewHolder.ll_main.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActivity.addFragment(SongsListFragment.getInstance(realmResults.get(position).getFolder_name(),
                                realmResults.get(position).getFolder_path()), true, true);
                    }
                });
            }
        }

        class ViewHolder extends RealmViewHolder {
            @BindView(R.id.ll_main)
            LinearLayout ll_main;

            @BindView(R.id.tv_foldername)
            TextView tv_foldername;

            @BindView(R.id.tv_songs_count)
            TextView tv_songs_count;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
