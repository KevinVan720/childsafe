package com.childsafe.auth.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.childsafe.auth.R;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;

public class ResetPasswordActivity extends BaseActivity implements
        View.OnClickListener {

    TextView mEmailField;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reset_password);

        // Views
        mEmailField = findViewById(R.id.fieldEmail);

        // Buttons
        findViewById(R.id.sendResetEmailButton).setOnClickListener(this);
        findViewById(R.id.backToLogInButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sendResetEmailButton) {
            sendResetEmail(mEmailField.getText().toString());

        } else if (i == R.id.backToLogInButton) {
            Intent activityIntent = new Intent(ResetPasswordActivity.this, LogInActivity.class);
            startActivity(activityIntent);
            finish();
        }
    }

    private void sendResetEmail(String email) {
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        mServer.sendPasswordResetEmail(email).addOnCompleteListener((@NonNull Task<Void> task) -> {
            if (task.isSuccessful()) {
                Toast.makeText(getBaseContext(), getString(R.string.reset_email_sent), Toast.LENGTH_SHORT).show();
                Intent activityIntent = new Intent(ResetPasswordActivity.this, LogInActivity.class);
                startActivity(activityIntent);
                finish();
            } else {
                Toast.makeText(getBaseContext(), getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
            }
        });

        hideProgressDialog();

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
        return valid;
    }

}
