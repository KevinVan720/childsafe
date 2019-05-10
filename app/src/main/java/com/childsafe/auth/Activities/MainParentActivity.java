package com.childsafe.auth.Activities;

import android.os.Bundle;

import com.childsafe.auth.Fragments.ChildrenGridFragment;
import com.childsafe.auth.R;
import com.childsafe.auth.Utils.SettingsUtil;
import com.childsafe.auth.Workers.ParentNotifyWorker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.TimeUnit;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class MainParentActivity extends MainBaseActivity {

    private static final String TAG = MainParentActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();
        FloatingActionButton fabTop = findViewById(R.id.fabTop);
        fabTop.hide();

        if (SettingsUtil.getNotification(getBaseContext())) {
            PeriodicWorkRequest.Builder notifyBuilder =
                    new PeriodicWorkRequest.Builder(ParentNotifyWorker.class, 15,
                            TimeUnit.MINUTES).addTag("PARENTNOTIFY");
            PeriodicWorkRequest notifyWork = notifyBuilder.build();
            WorkManager.getInstance().enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.REPLACE, notifyWork);
        } else {
            WorkManager.getInstance().cancelAllWorkByTag("PARENTNOTIFY");
        }

    }

    @Override
    public void launchHomeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ChildrenGridFragment frag = new ChildrenGridFragment();
        frag.addCurrentUser(currentUser);
        fragmentTransaction.replace(R.id.main_content, frag, "CHILDRENGRIDFRAGMENT");
        fragmentTransaction.commit();
    }

}

