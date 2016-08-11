package com.byteshaft.hairrestorationcenter.account;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.hairrestorationcenter.MainActivity;
import com.byteshaft.hairrestorationcenter.R;
import com.byteshaft.hairrestorationcenter.fragments.EducationFragment;
import com.byteshaft.hairrestorationcenter.utils.AppGlobals;
import com.byteshaft.hairrestorationcenter.utils.WebServiceHelpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by husnain on 8/5/16.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEmail;
    private EditText mPassword;
    private Button mLoginButton;
    private Button mRegisterButton;

    private TextView mForgotPasswordTextView;

    private String mPasswordString;
    private String mEmailString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delegate_login);
        mEmail = (EditText) findViewById(R.id.email_address);
        mPassword = (EditText) findViewById(R.id.password);

        mLoginButton = (Button) findViewById(R.id.login);
        mRegisterButton = (Button) findViewById(R.id.register);
        mLoginButton.setOnClickListener(this);
        mRegisterButton.setOnClickListener(this);

        mForgotPasswordTextView = (TextView) findViewById(R.id.tv_forgot_password);
        mForgotPasswordTextView.setOnClickListener(this);
    }

    public boolean validate() {
        boolean valid = true;

        mEmailString = mEmail.getText().toString();
        mPasswordString = mPassword.getText().toString();

        if (mEmailString.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.
                matcher(mEmailString).matches()) {
            mEmail.setError("enter a valid email address");
            valid = false;
        } else {
            mEmail.setError(null);
        }

        if (mPasswordString.isEmpty() || mPassword.length() < 4) {
            mPassword.setError("Enter minimum 4 alphanumeric characters");
            valid = false;
        } else {
            mPassword.setError(null);
        }
        return valid;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login:
                if (validate()) {
                    new LogInTask().execute();
                }
                break;
            case R.id.register:
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                break;
            case R.id.tv_forgot_password:
                startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));

        }
    }

    class LogInTask extends AsyncTask<String, String, String> {
        private JSONObject jsonObject;
        private boolean noInternet = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            WebServiceHelpers.showProgressDialog(LoginActivity.this, "LoggingIn");
        }

        @Override
        protected String doInBackground(String... strings) {

            if (WebServiceHelpers.isNetworkAvailable() && WebServiceHelpers.isInternetWorking()){

                try {
                    jsonObject = WebServiceHelpers.logInUser(
                            mEmailString,
                            mPasswordString
                            );
                    Log.e("TAG", String.valueOf(jsonObject));
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            WebServiceHelpers.dismissProgressDialog();
            try {
                if (jsonObject.getString("Message").equals("Input is invalid")) {
                    AppGlobals.alertDialog(LoginActivity.this, "Login Failed!", "Invalid Email or Password" );

                }else if (jsonObject.getString("Message").equals("Successfully")) {
                    JSONObject details = jsonObject.getJSONObject("details");
                    System.out.println(jsonObject + "working");
                    String username = details.getString(AppGlobals.KEY_USER_NAME);
                    String userId = details.getString(AppGlobals.KEY_USER_ID);
                    String firstName = details.getString(AppGlobals.KEY_FIRSTNAME);
                    String lastName = details.getString(AppGlobals.KEY_LASTNAME);
                    String email = details.getString(AppGlobals.KEY_EMAIL);
                    String phoneNumber = details.getString(AppGlobals.KEY_PHONE_NUMBER);
                    String zipCode = details.getString(AppGlobals.KEY_ZIP_CODE);

                    //saving values
                    AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_FIRSTNAME, firstName);
                    Log.i("First name", " " + AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FIRSTNAME));
                    AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_LASTNAME, lastName);
                    AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_EMAIL, email);
                    AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PHONE_NUMBER, phoneNumber);
                    AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_ZIP_CODE, zipCode);
                    AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_USER_ID, userId);
                    AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_USER_NAME, username);
                    Toast.makeText(LoginActivity.this, "Log In Successful", Toast.LENGTH_SHORT).show();
                    AppGlobals.saveUserLogin(true);
                    finish();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
