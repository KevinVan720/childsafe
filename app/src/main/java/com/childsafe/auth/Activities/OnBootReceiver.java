package com.childsafe.auth.Activities;

import android.content.*;

import com.childsafe.auth.Model.ServerAdapter;
import com.childsafe.auth.Model.User;
import com.childsafe.auth.Workers.ChildStatusMonitorWorker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class OnBootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    private ServerAdapter mServer;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            //Log.i(TAG, "Booted!");
            mServer = new ServerAdapter();
            mServer.getCurrentUser().addOnCompleteListener(
                    new OnCompleteListener<User>() {
                        @Override
                        public void onComplete(@NonNull Task<User> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                User user = task.getResult();
                                if (user != null && user.getRole().equals("Child")) {
                                    PeriodicWorkRequest.Builder statusMonitorBuilder =
                                            new PeriodicWorkRequest.Builder(ChildStatusMonitorWorker.class, 15,
                                                    TimeUnit.MINUTES).addTag("CHILDSTATUSMONITOR");
                                    PeriodicWorkRequest statusMonitorWork = statusMonitorBuilder.build();
                                    WorkManager.getInstance().enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.REPLACE,statusMonitorWork);
                                }
                            }
                        }

                    });

        }
    }
}
