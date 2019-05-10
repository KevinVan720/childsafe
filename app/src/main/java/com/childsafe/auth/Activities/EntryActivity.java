package com.childsafe.auth.Activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.childsafe.auth.Model.ServerAdapter;
import com.childsafe.auth.Model.User;
import com.childsafe.auth.R;
import com.childsafe.auth.Utils.NetWorkUtil;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;

public class EntryActivity extends BaseActivity {

    public static final int REQUEST_CODE = 400;
    public static final String CHANNEL_ID = "child_safe";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkMyPermissions();
        }
        else
        {
            startMainActivity();
        }
        setContentView(R.layout.activity_entry);
    }

    private void checkMyPermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            //Permission Check
            requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE},
                    REQUEST_CODE);
        }
        else
        {
            startMainActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startMainActivity();

                } else {

                    Toast.makeText(this, getString(R.string.please_grant_permission), Toast.LENGTH_LONG).show();
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startMain);
                }
                return;
            }
        }
    }

    public void startMainActivity()
    {
        mServer = new ServerAdapter();
        mServer.getCurrentUser().addOnCompleteListener(
                (@NonNull Task<User> task) -> {
                    if (task.isSuccessful()) {
                        User user = task.getResult();
                        if (user != null && user.getCurrentDevice()!=null && user.getCurrentDevice().equals(NetWorkUtil.getMacAddr())) {
                            if (user.getRole().equals("Parent")) {
                                Intent activityIntent = new Intent(EntryActivity.this, MainParentActivity.class);
                                activityIntent.putExtra(getString(R.string.user_intent), user);
                                startActivity(activityIntent);
                                finish();
                            } else if (user.getRole().equals("Child")) {
                                Intent activityIntent = new Intent(EntryActivity.this, MainChildActivity.class);
                                activityIntent.putExtra(getString(R.string.user_intent), user);
                                startActivity(activityIntent);
                                finish();
                            }
                            else
                            {
                                //Police need to log in every time
                                Intent activityIntent = new Intent(EntryActivity.this, LogInActivity.class);
                                startActivity(activityIntent);
                                finish();
                            }
                        } else {
                            Intent activityIntent = new Intent(EntryActivity.this, LogInActivity.class);
                            startActivity(activityIntent);
                            finish();
                        }
                    }
                });
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Child is lost!";
            String description = "Child is lost!";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
