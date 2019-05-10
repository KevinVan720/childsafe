package com.childsafe.auth.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.childsafe.auth.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class SignUpActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "SignUp";

    private EditText mEmailField;
    private EditText mPasswordField;
    private RadioGroup mradioGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Views
        mEmailField = findViewById(R.id.fieldEmail);
        mPasswordField = findViewById(R.id.fieldPassword);

        // Buttons
        findViewById(R.id.emailCreateAccountButton).setOnClickListener(this);
        findViewById(R.id.backToLogInButton).setOnClickListener(this);
        mradioGroup = findViewById(R.id.radioGroup);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.emailCreateAccountButton) {
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
        else if(i==R.id.backToLogInButton)
        {
            Intent activityIntent = new Intent(SignUpActivity.this, LogInActivity.class);
            startActivity(activityIntent);
            finish();
        }
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        int selectedId = mradioGroup.getCheckedRadioButtonId();
        RadioButton rolebutton = (RadioButton) findViewById(selectedId);
        String role = rolebutton.getText().toString();

        mServer.createAccount(email, password)
                .addOnCompleteListener(this, (@NonNull Task<AuthResult> task) -> {
                    if (task.isSuccessful()) {
                        // Send Email verification
                        Log.d(TAG, "createUserWithEmail:success");
                        mServer.sendEmailVerification().addOnCompleteListener(SignUpActivity.this, (@NonNull Task<Void> usertask) -> {

                            if (usertask.isSuccessful()) {
                                Toast.makeText(getBaseContext(),
                                        getString(R.string.verify_email_sent) + email,
                                        Toast.LENGTH_SHORT).show();

                                mServer.createUserInfo(mServer.getAuth().getCurrentUser().getUid(), mServer.getAuth().getCurrentUser().getEmail(), role);

                                //Back to log in
                                Intent activityIntent = new Intent(SignUpActivity.this, LogInActivity.class);
                                startActivity(activityIntent);
                                finish();
                            } else {
                                Log.e(TAG, "sendEmailVerification", task.getException());
                                Toast.makeText(getBaseContext(),
                                        getString(R.string.verify_email_not_sent),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        // If sign up fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpActivity.this, getString(R.string.status_verification_failed),
                                Toast.LENGTH_SHORT).show();
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