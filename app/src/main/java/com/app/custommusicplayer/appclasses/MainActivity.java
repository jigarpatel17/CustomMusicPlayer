package com.app.custommusicplayer.appclasses;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.custommusicplayer.R;
import com.app.custommusicplayer.fragment.HomeFragment;
import com.app.custommusicplayer.fragment.SongsFolderFragment;
import com.app.custommusicplayer.database.SongFolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_DRAGGING;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;
import static android.support.design.widget.BottomSheetBehavior.STATE_SETTLING;
import static android.support.design.widget.BottomSheetBehavior.from;

public class MainActivity extends MIActivity implements View.OnClickListener {

    private static final int REQUEST_RUNTIME_PERMISSION = 19;
    @BindView(R.id.design_bottom_sheet)
    public RelativeLayout design_bottom_sheet;

    @BindView(R.id.coordinatorLayout)
    public CoordinatorLayout coordinatorLayout;

    @BindView(R.id.txtNoData)
    public TextView txtNoData;

    @BindView(R.id.fragment_container)
    public FrameLayout fragment_container;

    BottomSheetBehavior behavior;
    boolean permissionGranted = false;
    public Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this, this);

        // requesting run time permission for the Marshmallow and above version.
        requestRunTimePermission();
    }

    void requestRunTimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_RUNTIME_PERMISSION);
            } else {
                // continue with your code
                permissionGranted = true;
                initComponents();
            }
        } else {
            // continue with your code
            permissionGranted = true;
            initComponents();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_RUNTIME_PERMISSION:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        permissionGranted = false;
                        txtNoData.setVisibility(View.VISIBLE);
                        fragment_container.setVisibility(View.GONE);
                        Toast.makeText(this, "Please provide Read Permission.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Log.e("Permission", "Granted");
                permissionGranted = true;
                initComponents();
                break;
        }
    }

    void initComponents() {

        txtNoData.setVisibility(View.GONE);
        fragment_container.setVisibility(View.VISIBLE);

        // here we have initiated realm.
        realm = Realm.getDefaultInstance();

        // handle click listeners.
        design_bottom_sheet.setOnClickListener(this);

        // handle bottom sheet behavior
        handleBottomSheetBehavior();

        RealmResults<SongFolder> songFolders = realm.where(SongFolder.class).findAll();
        if (songFolders != null && !songFolders.isEmpty()) {
            // adding home fragment.
            addFragment(new SongsFolderFragment(), false, true);
        } else {
            // adding home fragment.
            addFragment(new HomeFragment(), false, true);
        }
    }

    void handleBottomSheetBehavior() {
        behavior = from(design_bottom_sheet);
        behavior.setBottomSheetCallback(new BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case STATE_DRAGGING:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_DRAGGING");
                        break;
                    case STATE_SETTLING:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_SETTLING");
                        break;
                    case STATE_EXPANDED:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_EXPANDED");
                        break;
                    case STATE_COLLAPSED:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_COLLAPSED");
                        break;
                    case STATE_HIDDEN:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_HIDDEN");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.i("BottomSheetCallback", "slideOffset: " + slideOffset);
            }
        });
    }

    void toggleBottomSheet() {
        if (behavior.getState() == STATE_COLLAPSED) {
            behavior.setState(STATE_EXPANDED);
        } else {
            behavior.setState(STATE_COLLAPSED);
        }
    }

    @Override
    public void onClick(View v) {
        if (!permissionGranted) {
            Toast.makeText(this, "Please provide all permission", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {
            case R.id.design_bottom_sheet:
                toggleBottomSheet();
                break;
        }
    }

    // pass context to Calligraphy
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(context));
    }

    @Override
    public void onBackPressed() {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        // this will hide keypad if it is open
        hideKeyboard();

        android.support.v4.app.Fragment currentFragment = this.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        FragmentManager childFm = currentFragment.getChildFragmentManager();
        if (childFm.getBackStackEntryCount() > 0) {
            childFm.popBackStack();
            return;
        }

        if (fm.getBackStackEntryCount() > 0) {
            hideKeyboard();
            if (currentFragment != null) {
                fragmentManager.popBackStack();
            }
        } else {
            if (currentFragment != null) {
                /*if (currentFragment.getClass().getName().equalsIgnoreCase(ProfileMyfavouritesFragment.class.getName())
                        || currentFragment.getClass().getName().equalsIgnoreCase(FavouriteFragment.class.getName())
                        || currentFragment.getClass().getName().equalsIgnoreCase(PanneyCenterFragemnt.class.getName())
                        || currentFragment.getClass().getName().equalsIgnoreCase(InviteFriendsGroupFragment.class.getName())
                        || currentFragment.getClass().getName().equalsIgnoreCase(FaqsFragment.class.getName())
                        || currentFragment.getClass().getName().equalsIgnoreCase(PromotionFragment.class.getName())
                        || currentFragment.getClass().getName().equalsIgnoreCase(SettingsFragment.class.getName())
                        || currentFragment.getClass().getName().equalsIgnoreCase(NotificationFragment.class.getName())
                        || currentFragment.getClass().getName().equalsIgnoreCase(FeedbackFragment.class.getName())
                        || currentFragment.getClass().getName().equalsIgnoreCase(FragmentTrancationHistory.class.getName())
                        || currentFragment.getClass().getName().equalsIgnoreCase(ProximityDealsFragment.class.getName())) {
                    clearBackStack();
//                    Bundle bundle = new Bundle();
//                    bundle.putBoolean(CommonKeys.ForHomeFullImage, false);
                    pushFragmentDontIgnoreCurrent(null, new HomeFragment(), MIActivity.FRAGMENT_JUST_ADD);
                } else {
                    super.onBackPressed();
                }*/
                super.onBackPressed();
            }
        }
    }
}
