package com.childsafe.auth.Activities;

import android.os.Bundle;

import com.childsafe.auth.Fragments.ChildMainFragment;
import com.childsafe.auth.R;
import com.childsafe.auth.Workers.ChildStatusMonitorWorker;

import java.util.concurrent.TimeUnit;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class MainChildActivity extends MainBaseActivity {

    private static final String TAG = MainChildActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onStart() {
        super.onStart();

        PeriodicWorkRequest.Builder statusMonitorBuilder =
                new PeriodicWorkRequest.Builder(ChildStatusMonitorWorker.class, 15,
                        TimeUnit.MINUTES).addTag("CHILDSTATUSMONITOR");
        PeriodicWorkRequest statusMonitorWork = statusMonitorBuilder.build();
        WorkManager.getInstance().enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.REPLACE, statusMonitorWork);
    }

    @Override
    public void launchHomeFragment()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ChildMainFragment frag = new ChildMainFragment();
        frag.addCurrentUser(currentUser);
        fragmentTransaction.replace(R.id.main_content, frag);
        fragmentTransaction.commit();
    }

}
