package thesociallions.myrug;

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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import nl.thesociallions.myrug.account.RUGAccount;


/**
 * A login screen that offers login via email/password.
 */
public class SetupBackupActivity extends ActionBarActivity {
    // UI references.
    private EditText mStudentIDView;
    private EditText mPasswordView;
    private EditText mGUIDView;
    //private View mProgressView;
    //private View mLoginFormView;

    private String studentid;
    private String password;
    private String guid;
    private final int sdkVersion = Build.VERSION.SDK_INT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_backup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        // Set up the login form.
        mStudentIDView = (EditText) findViewById(R.id.studentid);
        mPasswordView = (EditText) findViewById(R.id.password);
        mGUIDView = (EditText) findViewById(R.id.guid);
        mGUIDView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
        guid = mGUIDView.getText().toString();


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
        } else {
            RUGAccount rugAccount = new RUGAccount(studentid, password, guid, getApplicationContext());
            rugAccount.addToAccountManager();
            finishSetup();
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

    public void finishSetup(){

        Intent intent = new Intent(SetupBackupActivity.this, MainActivity.class);

        // Make sure the back button won't go back to the setup process
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }



    public void invalidCredentials(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SetupBackupActivity.this);
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SetupBackupActivity.this);
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
}