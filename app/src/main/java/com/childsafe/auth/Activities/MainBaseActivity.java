package com.childsafe.auth.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.childsafe.auth.Fragments.EditProfileFragment;
import com.childsafe.auth.Fragments.HelpFragment;
import com.childsafe.auth.Fragments.MainBaseFragment;
import com.childsafe.auth.Fragments.SettingsFragment;
import com.childsafe.auth.Model.User;
import com.childsafe.auth.R;
import com.childsafe.auth.Utils.NetWorkUtil;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.WorkManager;

public class MainBaseActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected User currentUser;
    protected ValueEventListener mUserListenr;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_base);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        currentUser = (User) intent.getExtras().getSerializable(getString(R.string.user_intent));
        updateUserProfile(currentUser.getName(), currentUser.getEmail(), currentUser.getImageUrl());

        launchHomeFragment();

    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MainbaseAct", "resumed");
        mUserListenr = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get user value
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    // User is null, error out
                    if (user.getEmail() != currentUser.getEmail() || user.getName() != currentUser.getName() || user.getImageUrl() != currentUser.getImageUrl()) {
                        updateUserProfile(user.getName(), user.getEmail(), user.getImageUrl());
                    }

                    if (!user.getCurrentDevice().equals(NetWorkUtil.getMacAddr())) {
                        Toast.makeText(getBaseContext(),R.string.signed_out,Toast.LENGTH_LONG).show();
                        logOut(user);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        mServer.getUsersRef().child(currentUser.getUid()).addValueEventListener(mUserListenr);
    }

    protected void onPause() {
        super.onPause();
        mServer.getUsersRef().child(currentUser.getUid()).removeEventListener(mUserListenr);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("MainAct", "activity received");
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

            launchHomeFragment();

        } else if (id == R.id.nav_profile) {

            EditProfileFragment profile_frag = new EditProfileFragment();
            commitFragment(profile_frag);

        } else if (id == R.id.nav_settings) {

            SettingsFragment settings_frag = new SettingsFragment();
            commitFragment(settings_frag);

        } else if (id == R.id.nav_help) {

            HelpFragment help_frag = new HelpFragment();
            commitFragment(help_frag);

        } else if (id == R.id.nav_logout) {

            logOut(currentUser);

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void updateUserProfile(String name, String email, String imageurl) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView name_view = headerView.findViewById(R.id.personal_name);
        TextView email_view = headerView.findViewById(R.id.personal_email);
        ImageView portrait_view = headerView.findViewById(R.id.personal_photo);
        name_view.setText(name);
        email_view.setText(email);
        currentUser.setEmail(email);
        currentUser.setName(name);
        currentUser.setImageUrl(imageurl);
        if (imageurl != null) {
            String realimageurl = imageurl.substring(36);
            Log.i("url", realimageurl);
            StorageReference image_ref = mServer.getStorageRef().child(realimageurl);
            final long ONE_MEGABYTE = 1024 * 1024;
            image_ref.getBytes(ONE_MEGABYTE).addOnSuccessListener((byte[] bytes) -> {
                Bitmap decodedByte = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                portrait_view.setImageBitmap(decodedByte);
            }).addOnFailureListener((@NonNull Exception exception) ->
                    Toast.makeText(getBaseContext(), getString(R.string.unable_commu_server), Toast.LENGTH_SHORT).show()
            );
        }
    }

    public void logOut(User user) {
        if (user.getRole().equals("Child")) {
            WorkManager.getInstance().cancelAllWorkByTag("CHILDSTATUSMONITOR");
        } else if (user.getRole().equals("Parent")) {
            WorkManager.getInstance().cancelAllWorkByTag("PARENTNOTIFY");
        }
        mServer.signOut();
        Intent activityIntent = new Intent(MainBaseActivity.this, LogInActivity.class);
        startActivity(activityIntent);
        finish();
    }

    protected void commitFragment(MainBaseFragment frag) {
        frag.addCurrentUser(currentUser);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_content, frag, frag.getClass().getSimpleName());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();
    }

    protected void launchHomeFragment() {
    }

}
