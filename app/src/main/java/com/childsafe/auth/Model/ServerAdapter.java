package com.childsafe.auth.Model;

import android.util.Log;

import com.childsafe.auth.Utils.TimeUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class ServerAdapter {

    private static final String TAG = "Firebase";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;

    public ServerAdapter() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance("gs://childsafe-3abbd.appspot.com");
    }


    public Task<AuthResult> createAccount(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    public void createUserInfo(String uid, String email, String role) {
        User user = new User(uid, email, role, "Safe", "Unknown");
        updateUserInfo(user);
        if (role.equals("Child")) {
            updateUserInfo(uid, "lastTime", TimeUtil.getCurrentTime());
        }
    }

    public Task<AuthResult> signInWithEmailAndPassword(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password);
    }

    public Task<Void> sendEmailVerification() {
        final FirebaseUser user = mAuth.getCurrentUser();
        mAuth.useAppLanguage();
        return user.sendEmailVerification();
    }

    public Task<Void> sendPasswordResetEmail(String email) {
        mAuth.useAppLanguage();
        return mAuth.sendPasswordResetEmail(email);
    }

    public void signOut() {
        mAuth.signOut();
    }


    public Task<User> getCurrentUser() {
        TaskCompletionSource<User> taskSource = new TaskCompletionSource<>();

        FirebaseUser fuser = mAuth.getCurrentUser();
        if (fuser == null || fuser.isEmailVerified() == false) {
            Log.i(TAG, "Not signed in!");
            taskSource.setResult(null);
        } else {
            getUsersRef().child(fuser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User currentuser = dataSnapshot.getValue(User.class);
                        taskSource.setResult(currentuser);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }
        return taskSource.getTask();

    }

    public Task<User> getUserById(String uid) {
        TaskCompletionSource<User> taskSource = new TaskCompletionSource<>();
        getUsersRef().child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User currentuser = dataSnapshot.getValue(User.class);
                    taskSource.setResult(currentuser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return taskSource.getTask();
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public DatabaseReference getUsersRef() {
        return mDatabase.child("users");
    }

    public StorageReference getStorageRef() {
        return mStorage.getReference();
    }

    public Task<UploadTask.TaskSnapshot> uploadImage(byte[] data, String storage_path) {

        final StorageReference ref = getStorageRef().child(storage_path + "/portrait.jpg");
        return ref.putBytes(data);
    }

    public void addGPSLocation(String uid, String date, String latitude, String longitude) {
        mDatabase.child("locations").child(uid).child(date).child("Lat").setValue(latitude);
        mDatabase.child("locations").child(uid).child(date).child("Long").setValue(longitude);
    }

    public void clearGPSLocation(String uid) {
        mDatabase.child("locations").child(uid).removeValue();
    }

    public Task<ArrayList<ArrayList<String>>> getTimeStampedGPSData(String uid) {
        TaskCompletionSource<ArrayList<ArrayList<String>>> taskSource = new TaskCompletionSource<>();
        Query locQuery = mDatabase.child("locations").child(uid).orderByKey();
        locQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.i("GPS data number", ((Long) dataSnapshot.getChildrenCount()).toString());
                    ArrayList<ArrayList<String>> rst = new ArrayList<>();
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        ArrayList<String> temp = new ArrayList<>();
                        String time = data.getKey();
                        String lat = data.child("Lat").getValue(String.class);
                        String lng = data.child("Long").getValue(String.class);
                        temp.add(time);
                        temp.add(lat);
                        temp.add(lng);
                        rst.add(temp);
                    }
                    taskSource.setResult(rst);
                } else {
                    //No GPS data, might throw an exception?
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return taskSource.getTask();
    }

    public void updateUserInfo(String uid, String attr, Object value) {
        getUsersRef().child(uid).child(attr).setValue(value);
    }

    public void updateUserInfo(User user) {
        getUsersRef().child(user.getUid()).setValue(user);
    }

    public void updateChildStatus(String childId, String status) {
        updateUserInfo(childId, "status", status);
        updateUserInfo(childId, "lastTime", TimeUtil.getCurrentTime());
    }


    public DatabaseReference getBindingRef(String parentId, String childId) {
        return getUsersRef().child(parentId).child("linkedaccounts").child(childId);
    }

    public void updateBindingStatus(String parentId, String childId, Integer islinked) {
        getBindingRef(parentId, childId).setValue(islinked);
    }

    public void removeBinding(String parentId, String childId) {
        getBindingRef(parentId, childId).removeValue();
    }

    public Query getUserByEmail(String email) {
        return getUsersRef().orderByChild("email").equalTo(email);
    }

    public Query getLostChildrenList() {
        return getUsersRef().orderByChild("status").equalTo("Lost");
    }
}
