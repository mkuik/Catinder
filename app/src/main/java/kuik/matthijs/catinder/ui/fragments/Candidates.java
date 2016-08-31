package kuik.matthijs.catinder.ui.fragments;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import org.json.JSONObject;
import kuik.matthijs.catinder.*;
import kuik.matthijs.catinder.ui.HeartsOverlay;
import kuik.matthijs.catinder.util.CachedBitmapUserAdapter;
import kuik.matthijs.catinder.util.Net;

public class Candidates extends Fragment implements
        SwipeFlingAdapterView.onFlingListener, View.OnClickListener,
        View.OnLayoutChangeListener, Net.Adapter {

    private CachedBitmapUserAdapter cadidates;
    private SwipeFlingAdapterView flingContainer;
    private ImageButton reshuffle;
    public static final String TAG = "Candidates";
    HeartsOverlay hearts;
    Net.GetRandomUser task = null;
    User removedUser = null;
    boolean getUserBlock = false;

    public void like(final int id) {
        new Net.AddLike(id) {
            @Override
            protected void onFailed() {
                super.onFailed();
                like(id);
            }
        }.execute(new JSONObject());
    }

    public void dislike(final int id) {
        new Net.AddDislike(id) {
            @Override
            protected void onFailed() {
                super.onFailed();
                dislike(id);
            }
        }.execute(new JSONObject());
    }

    public void getUser() {
        if (Net.getProfile() != null && task == null && Net.getProfile().isConnected()) {
            task = new Net.GetRandomUser() {
                @Override
                protected void onReceivedUser(User user) {
                    if (reshuffle != null) reshuffle.setVisibility(View.GONE);
                    getUserBlock = false;
                    add(user);
                }

                @Override
                protected void onNoUserAvailable() {
                    reshuffle.setVisibility(View.VISIBLE);
                    getUserBlock = true;
                }

                @Override
                protected void onFailed() {
                    super.onFailed();
                    Net.addSyncListener(Candidates.this);
                    task = null;
                }

                @Override
                protected void onPostExecute(JSONObject response) {
                    super.onPostExecute(response);
                    task = null;
                }

                @Override
                protected void onCancelled() {
                    super.onCancelled();
                    task = null;
                }
            };
            task.execute(new JSONObject());
        }
    }

    public void reshuffle() {
        new Net.ReShuffle() {
            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                getUserBlock = false;
                getUser();
            }
        }.execute(new JSONObject());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Net.addSyncListener(this);
        cadidates = new CachedBitmapUserAdapter(
                getContext(), R.layout.vlees_item, CachedBitmapUserAdapter.ImageSize.LARGE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Net.removeSyncListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (task != null) {
            task.cancel(false);
            task = null;
        }
        cadidates.clearCache();
        hearts.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_candidates, container, false);
        if (view != null) {
            Log.d(TAG, "onCreateView");
            flingContainer = (SwipeFlingAdapterView) view.findViewById(R.id.fapper);
            ImageButton left = (ImageButton) view.findViewById(R.id.left);
            ImageButton right = (ImageButton) view.findViewById(R.id.right);
            hearts = (HeartsOverlay) view.findViewById(R.id.hearts_overlay);
            reshuffle = (ImageButton) view.findViewById(R.id.reshuffle);
            hearts.addOnLayoutChangeListener(this);

            flingContainer.setAdapter(cadidates);
            left.setOnClickListener(this);
            right.setOnClickListener(this);
            reshuffle.setOnClickListener(this);
            flingContainer.setFlingListener(this);
        }
        return view;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
        final int height = bottom - top;
        final int width = right - left;
        switch (v.getId()) {
            case R.id.hearts_overlay: {
                if (cadidates.getCount() != 0 && hearts.getCount() == 0) {
                    hearts.newGroup(width, height);
                }
                break;
            }
        }
    }

    public void newHeartsGroup() {
        if (hearts.getCount() == 0)
            hearts.newGroup();
    }

    public void add(User user) {
        if (removedUser == user) return;
        for (int i = 0; i != cadidates.getCount(); ++i) {
            if (cadidates.getItem(i).getId() == user.getId())
                return;
        }
        reshuffle.setVisibility(View.GONE);
        cadidates.add(user);
        cadidates.notifyDataSetChanged();
        if (cadidates.getCount() == 1) {
            newHeartsGroup();
        }
        Log.i(TAG, "add " + user.toString());
    }

    @Override
    public void removeFirstObjectInAdapter() {
        if (cadidates.getCount() >  0) {
            removedUser = cadidates.getItem(0);
            cadidates.remove(0);
        }
        cadidates.notifyDataSetChanged();
        getUserBlock = false;
    }

    @Override
    public void onLeftCardExit(Object dataObject) {
        hearts.breakHearts();
        dislike(((User) dataObject).getId());
    }

    @Override
    public void onRightCardExit(Object dataObject) {
        hearts.moveHeartsToRight();
        like(((User) dataObject).getId());
    }

    @Override
    public void onAdapterAboutToEmpty(int itemsInAdapter) {
        getUser();
    }

    @Override
    public void onScroll(float scrollProgressPercent) {
        View view = flingContainer.getSelectedView();
        if (view != null) {
            view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
            view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right: {
                if (cadidates.getCount() > 0)
                    flingContainer.getTopCardListener().selectRight();
                break;
            }
            case R.id.left: {
                if (cadidates.getCount() > 0)
                    flingContainer.getTopCardListener().selectLeft();
                break;
            }
            case R.id.reshuffle: {
                reshuffle();
            }
        }
    }

    @Override
    public void OnConnectionFailed(JSONObject task) {

    }

    @Override
    public void OnDataSyncEnd() {

    }

    @Override
    public void OnDataSyncStart() {

    }

    @Override
    public void OnConnectionSucces(JSONObject out, JSONObject in) {

    }

    @Override
    public void OnLogin() {
        cadidates.clear();
        getUserBlock = false;
        getUser();
    }

    @Override
    public void OnWrongCredentials() {

    }
}