package nl.thesociallions.myrug;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import nl.thesociallions.myrug.account.RUGAccount;
import nl.thesociallions.myrug.helper.Constants;


/**
 * A login screen that offers login via email/password.
 */
public class SetupActivity extends ActionBarActivity {
    // UI references.
    private EditText mStudentIDView;
    private EditText mPasswordView;
    //private View mProgressView;
    //private View mLoginFormView;

    // Value references
    private String lastURL;
    private String studentid;
    private String password;
    private final int sdkVersion = Build.VERSION.SDK_INT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        setTitle("Welkom");

        // Set up the login form.
        mStudentIDView = (EditText) findViewById(R.id.studentid);
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

        Button mSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        Button mForgotPassword = (Button) findViewById(R.id.password_forget_button);
        mForgotPassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent reset = new Intent(Intent.ACTION_VIEW);
                String url = "https://diy.rug.nl/pwm/public/ForgottenPassword";
                reset.setData(Uri.parse(url));
                startActivity(reset);
            }
        });
        Button mManualSetup = (Button)findViewById(R.id.manual_setup);
        mManualSetup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent manual = new Intent(SetupActivity.this, SetupBackupActivity.class);
                startActivity(manual);
            }
        });

        //mLoginFormView = findViewById(R.id.email_login_form);
        //mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);

        // Check network
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        Boolean isConnected =  activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        /**if (mAuthTask != null) {
            return;
        }*/

        // Reset errors.
        mStudentIDView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        studentid = mStudentIDView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid studentid address.
        if (TextUtils.isEmpty(studentid)) {
            mStudentIDView.setError(getString(R.string.error_field_required));
            focusView = mStudentIDView;
            cancel = true;
        } else if (!isStudentIDValid(studentid)) {
            mStudentIDView.setError(getString(R.string.error_invalid_email));
            focusView = mStudentIDView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else if (isConnected) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);
            /**mAuthTask = new UserLoginTask(studentid, password);
            mAuthTask.execute((Void) null);*/
            // Setup installation ui
            setContentView(R.layout.activity_setup_progress);
            Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
            setSupportActionBar(toolbar);

            testCredentials(studentid, password);
        } else {
            noConn();
        }
    }

    private boolean isStudentIDValid(String studentid) {
        return (studentid.charAt(0) == 'S' | studentid.charAt(0) == 's') && studentid.length() >= 8;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8 && password.length() <= 61;
    }

    /**
     * Functions related to Accounts
     */

    @SuppressLint("AddJavascriptInterface")
    public void testCredentials(final String studentid, final String password){
        // Prepare webview
        WebView myWebView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.addJavascriptInterface(new guidGetter(this), "GUID");
        myWebView.setWebViewClient(new WebViewClient() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            public void onPageFinished(WebView myWebView, String url) {
                Log.e("My RUG", url);
                if (url.contains("login")) {
                    if (url.equals(getLastURL())) {
                        myWebView.stopLoading();
                        clearLastURL();
                        invalidCredentials();
                    } else {
                        myWebView.loadUrl("javascript:(function(){document.getElementsByName('user_id')[0].value='" + studentid + "';document.getElementsByName('password')[0].value='" + password + "';validate_form(document.getElementsByName('login')[0], false, false);document.getElementsByName('login')[0].submit();})();");
                        setLastURL(url);
                    }
                } else if (url.contains("178")) {
                        myWebView.loadUrl("javascript:window.GUID.processGUID(document.getElementsByTagName('input')[0].value);");
                }
            }
        });
        myWebView.loadUrl("https://nestor.rug.nl/webapps/login/?new_loc=%2Fwebapps%2Fportal%2Fexecute%2Ftabs%2FtabAction?tab_tab_group_id=_178_1");
    }

    public void finishSetup(){
        Intent intent = new Intent(SetupActivity.this, MainActivity.class);

        // Make sure the back button won't go back to the setup process
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void setLastURL(String url){
        lastURL = url;
    }

    public String getLastURL(){
        return lastURL;
    }

    public void clearLastURL(){
        lastURL = "";
    }

    public void invalidCredentials(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SetupActivity.this);
        alertDialogBuilder.setTitle(getString(R.string.SETUP_nosched));
        alertDialogBuilder
                .setMessage(getString(R.string.setup_error))
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        setContentView(R.layout.activity_setup);
                        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
                        setSupportActionBar(toolbar);
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void noConn(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SetupActivity.this);
        alertDialogBuilder.setTitle(getString(R.string.SETUP_nosched));
        alertDialogBuilder
                .setMessage(getString(R.string.setup_error))
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        setContentView(R.layout.activity_setup);
                        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
                        setSupportActionBar(toolbar);
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    class guidGetter {
        Context mContext;

        guidGetter(Context c){
            mContext = c;
        }

        @SuppressWarnings("unused")
        @JavascriptInterface
        public void processGUID(String guid) {
            Log.d(Constants.TAG, guid);

            if (guid.equals("")){
                invalidCredentials();
            } else {
                String theGuid = guid.replace("http://ical.citesi.nl/?icalguid=", "");
                String finalGuid = theGuid.replace("\n", "");
                RUGAccount rugAccount = new RUGAccount(studentid, password, finalGuid, getApplicationContext());
                rugAccount.addToAccountManager();
                finishSetup();
            }
        }
    }
}