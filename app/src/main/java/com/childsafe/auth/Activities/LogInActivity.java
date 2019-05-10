package com.childsafe.auth.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.childsafe.auth.Model.User;
import com.childsafe.auth.R;
import com.childsafe.auth.Utils.NetWorkUtil;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import androidx.annotation.NonNull;

public class LogInActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "SignIn";

    private EditText mEmailField;
    private EditText mPasswordField;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Views
        mEmailField = findViewById(R.id.fieldEmail);
        mPasswordField = findViewById(R.id.fieldPassword);

        // Buttons
        findViewById(R.id.logInButton).setOnClickListener(this);
        findViewById(R.id.emailCreateAccountButton).setOnClickListener(this);
        findViewById(R.id.forgetPasswordButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.emailCreateAccountButton) {
            Intent activityIntent = new Intent(LogInActivity.this, SignUpActivity.class);
            startActivity(activityIntent);
            finish();
        } else if (i == R.id.logInButton) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
        else if (i==R.id.forgetPasswordButton)
        {
            Intent activityIntent = new Intent(LogInActivity.this, ResetPasswordActivity.class);
            startActivity(activityIntent);
            finish();
        }
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        mServer.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, (@NonNull Task<AuthResult> task) -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");


                        mServer.getCurrentUser().addOnCompleteListener(
                                (@NonNull Task<User> usertask) -> {
                                    if (usertask.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.i(TAG, "signInWithEmail:success");
                                        User user = usertask.getResult();
                                        if (user != null) {
                                            String currentDevice = NetWorkUtil.getMacAddr();
                                            user.setCurrentDevice(currentDevice);
                                            mServer.updateUserInfo(user.getUid(),"currentDevice",currentDevice);
                                            if (user.getRole().equals("Parent")) {
                                                Intent activityIntent = new Intent(LogInActivity.this, MainParentActivity.class);
                                                activityIntent.putExtra(getString(R.string.user_intent), user);
                                                startActivity(activityIntent);
                                                finish();
                                            } else if (user.getRole().equals("Child")) {
                                                Intent activityIntent = new Intent(LogInActivity.this, MainChildActivity.class);
                                                activityIntent.putExtra(getString(R.string.user_intent), user);
                                                startActivity(activityIntent);
                                                finish();
                                            }
                                            else if (user.getRole().equals("Police")) {
                                                Intent activityIntent = new Intent(LogInActivity.this, MainPoliceActivity.class);
                                                activityIntent.putExtra(getString(R.string.user_intent), user);
                                                startActivity(activityIntent);
                                                finish();
                                            }

                                        }
                                        else {
                                            Log.i(TAG, "verify email");
                                            Toast.makeText(LogInActivity.this, getString(R.string.please_verify_email),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LogInActivity.this, getString(R.string.auth_failed),
                                Toast.LENGTH_LONG).show();
                    }

                    hideProgressDialog();
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError(getString(R.string.required));
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError(getString(R.string.required));
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

}
