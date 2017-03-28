package com.app.custommusicplayer.appclasses;

import android.app.Application;
import android.content.Context;

import com.app.custommusicplayer.R;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by mind on 7/2/17.
 */

public class CustomMusicPlayer extends Application {
    public static final String DATABASE_NAME = "CustomMusicPlayer.realm";
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        // The RealmConfiguration is created using the builder pattern.
        // The Realm file will be located in Context.getFilesDir() with name "myrealm.realm"
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(DATABASE_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        // Use the config
        Realm.setDefaultConfiguration(config);

        CalligraphyConfig.initDefault(
                new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/AvenirLTStd-Book.otf")
                        .setFontAttrId(R.attr.fontPath)
                        .build());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
    }
}
