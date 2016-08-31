package kuik.matthijs.catinder.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.json.JSONObject;

import kuik.matthijs.catinder.*;
import kuik.matthijs.catinder.Profile;
import kuik.matthijs.catinder.util.Net;

/**
 * Created by Matthijs Kuik on 7/21/2016.
 */
public class Login extends Fragment implements View.OnClickListener {

    private TextInputEditText email;
    private TextInputEditText password;
    private Adapter adapter;
    private Button loginButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        email = (TextInputEditText) view.findViewById(R.id.login_email);
        password = (TextInputEditText) view.findViewById(R.id.login_password);
        loginButton = (Button) view.findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);

        if (Net.getProfile() != null) {
            this.email.setText(Net.getProfile().getEmail());
            this.password.setText(Net.getProfile().getPassword());
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Adapter) {
            adapter = (Adapter) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Login.Connect");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        adapter = null;
    }

    @Override
    public void onClick(View v) {
        String email = this.email.getText().toString();
        String password = this.password.getText().toString();
        connect(email, password);
    }

    public void connect(String email, String password) {
        Net.setProfile(new Profile("", email, -1, password));
        new Net.UserLoginTask(){
            @Override
            protected void onPostExecute(JSONObject response) {
                super.onPostExecute(response);
                loginButton.setEnabled(true);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loginButton.setEnabled(false);
            }

            @Override
            protected void onReceivedProfile(kuik.matthijs.catinder.Profile profile) {
                super.onReceivedProfile(profile);
            }
        }.execute(new JSONObject());
    }

    public interface Adapter {
    }
}
