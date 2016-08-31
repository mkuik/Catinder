package kuik.matthijs.catinder.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONObject;

import java.util.HashMap;

import kuik.matthijs.catinder.*;
import kuik.matthijs.catinder.util.Net;
import kuik.matthijs.catinder.util.UserAdapter;

public class Matches extends Fragment implements AdapterView.OnItemClickListener, Net.Adapter {

    private UserAdapter adapter;
    private static final String TAG = "Matches";
    private Adapter listener;

    public void add(User user) {
        adapter.add(user);
    }

    public void refillAdapter(User[] update) {
        adapter.clear();
        for (Integer i = 0; i != update.length; ++i) {
            add(update[i]);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new UserAdapter(getContext(), R.layout.match_item, UserAdapter.ImageSize.SMALL);
        Net.addSyncListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Net.removeSyncListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_matches, container, false);
        if (view != null) {
            GridView gridView = (GridView) view.findViewById(R.id.gridView);
            gridView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            gridView.setOnItemClickListener(this);
        }

        getMatches();
        refillAdapter(new User[0]);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Adapter) {
            listener = (Adapter) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Adapter");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        User user = adapter.getItem(position);
        if (listener != null) listener.onMatchClick(user);
    }

    public void getMatches() {
        if (Net.getProfile() != null && Net.getProfile().isConnected()) {
            new Net.GetMatches() {
                @Override
                protected void onReceivedMatches(User[] matches) {
                    refillAdapter(matches);
                }
                @Override
                protected void onFailed() {
                    super.onFailed();
                    Net.addSyncListener(Matches.this);
                }
            }.execute(new JSONObject());
        } else {
            Log.e(TAG, "Profile is null");
        }
    }

    @Override
    public void OnConnectionFailed(JSONObject task) {

    }

    @Override
    public void OnDataSyncStart() {

    }

    @Override
    public void OnDataSyncEnd() {

    }

    @Override
    public void OnConnectionSucces(JSONObject out, JSONObject in) {

    }

    @Override
    public void OnLogin() {
        getMatches();
    }

    @Override
    public void OnWrongCredentials() {

    }

    public interface Adapter {
        void onMatchClick(User user);
    }


}
