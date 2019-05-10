package com.childsafe.auth.Workers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.childsafe.auth.Activities.EntryActivity;
import com.childsafe.auth.R;
import com.childsafe.auth.Model.ServerAdapter;
import com.childsafe.auth.Model.User;
import com.google.android.gms.tasks.Task;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ParentNotifyWorker extends Worker {
    private static final String TAG = ParentNotifyWorker.class.getSimpleName();
    public static final String CHANNEL_ID = "child_safe";
    private ServerAdapter mServer;
    private Context mContext;

    /**
     * Creates an instance of the {@link Worker}.
     *
     * @param appContext   the application {@link Context}
     * @param workerParams the set of {@link WorkerParameters}
     */
    public ParentNotifyWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        mContext = appContext;
        mServer = new ServerAdapter();

    }

    @NonNull
    @Override
    public Worker.Result doWork() {
        try {
            mServer.getCurrentUser().addOnCompleteListener((@NonNull Task<User> task) -> {
                if (task.isSuccessful()) {
                    User user = task.getResult();
                    if (user.getRole().equals("Parent")) {
                        Map<String, Integer> linkedaccounts = user.getLinkedaccounts();
                        for (Map.Entry<String, Integer> entry : linkedaccounts.entrySet()) {
                            String pid = entry.getKey();
                            Integer islinked = entry.getValue();
                            if (islinked == 1) {
                                mServer.getUserById(pid).addOnCompleteListener((@NonNull Task<User> task2) -> {
                                    User child = task2.getResult();
                                    if (child.getStatus().equals("Lost")) {
                                        Log.i(TAG, "User is lost!");
                                        // Create an explicit intent for an Activity in your app
                                        Intent intent = new Intent(mContext, EntryActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                .setContentTitle(mContext.getString(R.string.notify_child_lost))
                                                .setContentText(child.getName() + " " + mContext.getString(R.string.notify_child_lost_text))
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                // Set the intent that will fire when the user taps the notification
                                                .setContentIntent(pendingIntent)
                                                .setAutoCancel(true);

                                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
                                        // notificationId is a unique int for each notification that you must define
                                        notificationManager.notify(child.getUid(), child.getUid().hashCode(), builder.build());
                                    } else {
                                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
                                        notificationManager.cancel(child.getUid(), child.getUid().hashCode());
                                    }
                                });
                            }
                        }
                    } else {
                        Log.i(TAG, "User is not Parent!");
                    }

                } else {
                    throw new RuntimeException("failed to connect to server");
                }
            });
            return Result.success();
        } catch (Throwable throwable) {
            Log.e(TAG, "failed to connect to server");
            return Result.failure();
        }
    }
}
