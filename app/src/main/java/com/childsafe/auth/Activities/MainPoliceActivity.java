package com.childsafe.auth.Activities;

import android.os.Bundle;

import com.childsafe.auth.Fragments.LostChildrenGridFragment;
import com.childsafe.auth.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainPoliceActivity extends MainBaseActivity {

    private static final String TAG = MainPoliceActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();
        FloatingActionButton fabTop = findViewById(R.id.fabTop);
        fabTop.hide();
        FloatingActionButton fabcall = findViewById(R.id.fabcall);
        fabcall.hide();

    }

    @Override
    public void launchHomeFragment()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LostChildrenGridFragment frag = new LostChildrenGridFragment();
        frag.addCurrentUser(currentUser);
        fragmentTransaction.replace(R.id.main_content, frag,"LOSTCHILDRENGRIDFRAGMENT");
        fragmentTransaction.commit();
    }
}
