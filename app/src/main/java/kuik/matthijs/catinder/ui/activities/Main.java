package kuik.matthijs.catinder.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TabHost;
import java.io.File;
import kuik.matthijs.catinder.User;
import kuik.matthijs.catinder.R;
import kuik.matthijs.catinder.ui.fragments.Candidates;
import kuik.matthijs.catinder.ui.fragments.Matches;
import kuik.matthijs.catinder.ui.fragments.Profile;
import kuik.matthijs.catinder.ui.Sun;
import kuik.matthijs.catinder.util.Net;

public class Main extends Network implements
        Matches.Adapter,
        PopupMenu.OnMenuItemClickListener,
        TabHost.OnTabChangeListener {

    private FragmentTabHost tabHost;
    private Sun sun;
    private static final String CANDIDATES_ID = "candidates";
    private static final String MATCHES_ID = "matches";
    private static final String PROFILE_ID = "profile";
    private static final String TAG = "Main";

    @Override
    public void OnSyncEnd() {
        super.OnSyncEnd();
        if (sun != null) sun.stopAnimation();
    }

    @Override
    public void OnSyncStart() {
        super.OnSyncStart();
        if (sun != null) sun.startAnimation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mojo);

        initAccount();

        sun = (Sun) findViewById(R.id.sun_icon);
        tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        setupTab(CANDIDATES_ID, R.drawable.ic_candidates, Candidates.class);
        setupTab(MATCHES_ID, R.drawable.ic_matches, Matches.class);
        setupTab(PROFILE_ID, R.drawable.ic_my_profile, Profile.class);

        tabHost.setOnTabChangedListener(this);
    }

    private void setupTab(final String tag, final int drawable, Class<?> c) {
        View tabview = createTabView(tabHost.getContext(), tag, drawable);
        TabHost.TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview);
        tabHost.addTab(setContent, c, null);
    }

    private static View createTabView(final Context context, final String text, final int drawable) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab, null);
        ImageView icon = (ImageView) view.findViewById(R.id.tab_icon);
        icon.setImageResource(drawable);
        return view;
    }

    @Override
    public void onMatchClick(User user) {
        Intent intent = new Intent(this, Gallery.class);
        intent.putExtra("email", user.getEmail());
        intent.putExtra("name", user.getName());
        startActivity(intent);
    }

    public void showPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.options_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Log.i(TAG, item.toString());
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                return true;
            case R.id.my_gallery:
                Intent intent = new Intent(this, Gallery.class);
                intent.putExtra("email", Net.getProfile().getEmail());
                intent.putExtra("name", Net.getProfile().getName());
                startActivity(intent);
                return true;
            case R.id.clear_cache:
                try {
                    deleteDir(getCacheDir());
                } catch (Exception e) {
                    Log.d(TAG, "clear cache " + e.toString());
                }
                return true;
            default:
                return false;
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else
            return dir != null && dir.isFile() && dir.delete();
    }

    public void logout() {
        Net.getProfile().setConnected(false);
        showLoginActivity();
    }

    @Override
    public void onTabChanged(String tabId) {
        Fragment fg = getSupportFragmentManager().findFragmentByTag(tabId);
        Log.d(TAG, "onTabChanged(): " + tabId + ", fragment " + fg);
    }
}
