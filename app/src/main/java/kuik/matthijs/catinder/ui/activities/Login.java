package kuik.matthijs.catinder.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;

import org.json.JSONObject;

import kuik.matthijs.catinder.Globals;
import kuik.matthijs.catinder.Profile;
import kuik.matthijs.catinder.R;
import kuik.matthijs.catinder.ui.fragments.Register;
import kuik.matthijs.catinder.util.Net;

public class Login extends Network implements
        Register.Adapter, kuik.matthijs.catinder.ui.fragments.Login.Adapter {

    private static final String TAG = "Login";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FragmentTabHost tabHost = (FragmentTabHost) findViewById(R.id.tabhost);

        tabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        tabHost.addTab(tabHost.newTabSpec("Login").setIndicator("Login"),
                kuik.matthijs.catinder.ui.fragments.Login.class, null);
        tabHost.addTab(tabHost.newTabSpec("Registreer").setIndicator("Registreer"),
                Register.class, null);
    }

    @Override
    public void OnLogin() {
        super.OnLogin();
        finish();
        saveProfile();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
