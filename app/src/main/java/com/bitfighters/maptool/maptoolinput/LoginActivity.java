package com.bitfighters.maptool.maptoolinput;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    public static UserLoginTask userLoginTaskInstance;

    // UI references.
    private EditText mServerIpView, mUsernameView, mPasswordView, mPortView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.

        mServerIpView = (EditText) findViewById(R.id.serverIp);
        mServerIpView.setText(PreferenceManager.getDefaultSharedPreferences(this).getString("serverIP", ""));

        mPortView = (EditText) findViewById(R.id.port);
        mPortView.setText(PreferenceManager.getDefaultSharedPreferences(this).getString("port", ""));

        mUsernameView = (EditText) findViewById(R.id.username);
        mUsernameView.setText(PreferenceManager.getDefaultSharedPreferences(this).getString("username", ""));

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mConnectButton = (Button) findViewById(R.id.email_sign_in_button);
        mConnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mServerIpView.setError(null);
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String serverIp = mServerIpView.getText().toString();
        String portString = mPortView.getText().toString();
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        int port = 51234;

        boolean cancel = false;
        View focusView = null;

        // Check for valid login (only syntax).
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }
        if (TextUtils.isEmpty(serverIp)) {
            mServerIpView.setError(getString(R.string.error_field_required));
            focusView = mServerIpView;
            cancel = true;
        }

        if (!TextUtils.isEmpty(portString)) {
            try{
                port = Integer.parseInt(portString);
            }catch(NumberFormatException e){
                mPortView.setError(getString(R.string.error_wrongFormat));
                focusView = mPortView;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(serverIp, username, port, password);
            mAuthTask.execute((Void) null);
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public LoginActivity getThis(){
        return this;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mServerIp;
        private final String mUsername;
        private final String mPassword;
        private final int mPort;
        private ConnectionState state;
        private String connectionError;
        private Connector conn;

        public void setConnectionError(String error){
            connectionError = error;
            state = ConnectionState.connectionError;
        }

        UserLoginTask(String serverIp, String username, int port, String password) {
            mServerIp = serverIp;
            mUsername = username;
            mPassword = password;
            mPort = port;
            state = ConnectionState.none;
            userLoginTaskInstance = this;
        }

        public void connectionDone(){
            state = ConnectionState.connectionDone;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            MyData.instance = new MyData(mUsername);
            state = ConnectionState.connecting;

            conn = new Connector(mServerIp, mPort,mUsername, mPassword);
            conn.doConnect();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }

            return state == ConnectionState.connectionDone;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                PreferenceManager.getDefaultSharedPreferences(getThis()).edit().putString("serverIP", mServerIp).commit();

                PreferenceManager.getDefaultSharedPreferences(getThis()).edit().putString("port", String.valueOf(mPort)).commit();

                PreferenceManager.getDefaultSharedPreferences(getThis()).edit().putString("username", mUsername).commit();

                Intent intent = new Intent(getBaseContext(), MainTab.class);
                startActivity(intent);
            } else {
                if(connectionError.contains("host") || connectionError.contains("out")){
                    mServerIpView.setError(connectionError);
                    mServerIpView.requestFocus();
                }else if(connectionError.toLowerCase().contains("pass")){
                    mPasswordView.setError(connectionError);
                    mPasswordView.requestFocus();
                }else{
                    mUsernameView.setError(connectionError);
                    mUsernameView.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

    }
    public enum ConnectionState {none, connecting, connectionError, connectionDone};
}

