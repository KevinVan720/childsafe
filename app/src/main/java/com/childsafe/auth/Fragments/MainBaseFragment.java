package com.childsafe.auth.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.childsafe.auth.Model.ServerAdapter;
import com.childsafe.auth.Model.User;
import com.childsafe.auth.R;
import com.childsafe.auth.Utils.NetWorkUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import androidx.fragment.app.Fragment;

public class MainBaseFragment extends Fragment {

    protected ServerAdapter mServer;
    protected User currentUser;
    protected ValueEventListener mUserListenr;
    protected View mView;

    public void addCurrentUser(User user) {
        currentUser = user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        mView = view;
        mServer = new ServerAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("MainbaseFrag", "resumed");
        mUserListenr = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get user value
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    // User is null, error out
                    if (user.getEmail() != currentUser.getEmail() || user.getName() != currentUser.getName() || user.getImageUrl() != currentUser.getImageUrl()) {
                        currentUser=user;
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        mServer.getUsersRef().child(currentUser.getUid()).addValueEventListener(mUserListenr);
    }

    public void onPause() {
        super.onPause();
        mServer.getUsersRef().child(currentUser.getUid()).removeEventListener(mUserListenr);
    }
}
