package com.childsafe.auth.Fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.childsafe.auth.Model.User;
import com.childsafe.auth.R;
import com.childsafe.auth.Utils.UIUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ChildrenGridFragment extends MainBaseFragment {

    private static final String TAG = ChildrenGridFragment.class.getSimpleName();

    private Handler mHandler;
    private Runnable mRunnable;

    private ChildEventListener mChildListListener;
    private Map<String, ValueEventListener> mChildrenInfoListener;
    private Map<String, Integer> childrenIndexMap;
    private List<User> children;
    private List<User> children_request;
    private RecyclerView recyclerView;
    private ChildInfoRecyclerViewAdapter mAdapter;
    private RecyclerView.ItemDecoration cardDecoration = null;
    CardSwipeController cardSwipeController = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_children_grid, parent, false);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "started");

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "resumed");


        FloatingActionButton fabcall = getActivity().findViewById(R.id.fabcall);
        fabcall.show();
        fabcall.setOnClickListener((View v) -> {
            Snackbar snackbar = UIUtil.makeSnackbar(v, getActivity().getString(R.string.call_911), getActivity().getColor(R.color.lost_red));
            snackbar.setAction(getString(R.string.yes), (View view2) -> {
                if (getContext().checkSelfPermission(
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(getString(R.string.tel_head) + getString(R.string.call_phone)));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
            snackbar.show();
        });

        mChildrenInfoListener = new HashMap<>();
        childrenIndexMap = new HashMap<>();
        children = new Vector<>();
        children_request = new Vector<>();

        initRecyclerView();
        initChildListener();
        Log.i("childrenList",((Integer)children.size()).toString());

        mHandler = new Handler();

        //update lost time every minute
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
                mHandler.postDelayed(mRunnable, 60 * 1000);
            }
        };

        mHandler.post(mRunnable);

    }


    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "paused");

        FloatingActionButton fabcall = getActivity().findViewById(R.id.fabcall);
        fabcall.hide();

        clearChildListener();
        clearRecyclerView();

        mHandler.removeCallbacks(mRunnable);

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "stopped");


    }


    private void initRecyclerView() {
        // Set up the RecyclerView
        recyclerView = mView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mView.getContext(), 1, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);
        mAdapter = new ChildInfoRecyclerViewAdapter(children, children_request, mServer, currentUser, mView.getContext());
        recyclerView.setAdapter(mAdapter);


        //Decoration should only be added once!!!
        if (cardDecoration == null) {
            //Log.i("item decoration", "null");
            int sidePadding = getResources().getDimensionPixelSize(R.dimen.grid_spacing_small);
            cardDecoration = new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view,
                                           RecyclerView parent, RecyclerView.State state) {
                    outRect.left = sidePadding;
                    outRect.right = sidePadding;
                }

                @Override
                public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                    cardSwipeController.onDraw(c);
                }
            };
            recyclerView.addItemDecoration(cardDecoration);
        }

        if (cardSwipeController == null) {
            cardSwipeController = new CardSwipeController(new CardSwipeController.SwipeControllerActions() {
                @Override
                public void onClicked(int position) {
                    Snackbar unlinkSnackbar = UIUtil.makeSnackbar(getView(), getString(R.string.unlink_confirm), getResources().getColor(R.color.lost_red)).setAction(getString(R.string.yes), (View view) -> {
                        User child = mAdapter.getChildList().get(position);
                        mServer.removeBinding(currentUser.getUid(), child.getUid());
                        ChildrenGridFragment frag = (ChildrenGridFragment) getFragmentManager().findFragmentByTag("CHILDRENGRIDFRAGMENT");
                        if (frag != null && frag.isVisible()) {
                            FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                            fragTransaction.detach(frag);
                            fragTransaction.attach(frag);
                            fragTransaction.commit();
                        }
                    });
                    unlinkSnackbar.show();
                }
            }, getContext());
        }
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(cardSwipeController);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

    private void clearRecyclerView() {
        //clear card
        recyclerView.removeItemDecoration(cardDecoration);
        cardDecoration = null;
        cardSwipeController.clearCache();
        mAdapter = null;
    }

    private void initChildListener() {
        mChildListListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                /**
                 * Initialize all the child the parent is linked with
                 */
                String childuid = dataSnapshot.getKey();
                Integer islinked = dataSnapshot.getValue(Integer.class);
                if (islinked > 0) {
                    children.add(new User(childuid, "Unknown", "Child", "Safe", "Unknown"));
                    int index = children.size() - 1;
                    childrenIndexMap.put(childuid, index);
                    mAdapter.notifyDataSetChanged();

                    ValueEventListener childlistenr = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user value
                            User user = dataSnapshot.getValue(User.class);
                            if (user != null) {
                                int index = childrenIndexMap.get(user.getUid());
                                children.set(index, user);
                                mAdapter.notifyItemChanged(index);
                                //Log.i("change user at", ((Integer) index).toString());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    };

                    mChildrenInfoListener.put(childuid, childlistenr);
                    mServer.getUsersRef().child(childuid).addValueEventListener(mChildrenInfoListener.get(childuid));
                } else {
                    mServer.getUserById(childuid).addOnCompleteListener(
                            (@NonNull Task<User> task) -> {
                                if (task.isSuccessful()) {
                                    User user = task.getResult();
                                    if (user != null && user.getRole().equals("Child")) {
                                        children_request.add(user);
                                        mAdapter.notifyDataSetChanged();

                                    }
                                } else {
                                    Toast.makeText(mView.getContext(), getString(R.string.unable_commu_server),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }

            }

            /**
             * Update the child
             */
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String childuid = dataSnapshot.getKey();
                Integer islinked = dataSnapshot.getValue(Integer.class);
                if (islinked > 0 && childrenIndexMap.containsKey(childuid) == false) {
                    children.add(new User(childuid, "Unknown", "Child", "Safe", "Unknown"));
                    int index = children.size() - 1;
                    childrenIndexMap.put(childuid, index);
                    mAdapter.notifyDataSetChanged();

                    ValueEventListener childlistener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user value
                            User user = dataSnapshot.getValue(User.class);
                            if (user != null) {
                                int index = childrenIndexMap.get(user.getUid());
                                children.set(index, user);
                                mAdapter.notifyItemChanged(index);
                                //Log.i("change user at:", ((Integer) index).toString());
                                //Log.i("user status:", user.getStatus());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    };
                    mChildrenInfoListener.put(childuid, childlistener);
                    mServer.getUsersRef().child(childuid).addValueEventListener(mChildrenInfoListener.get(childuid));
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String childuid = dataSnapshot.getKey();
                Integer islinked = dataSnapshot.getValue(Integer.class);
                if (islinked > 0) {
                    mServer.getUsersRef().child(childuid).removeEventListener(mChildrenInfoListener.get(childuid));
                    mChildrenInfoListener.remove(childuid);
                    int index = childrenIndexMap.get(childuid);
                    mAdapter.notifyItemRemoved(index);
                    children.remove(index);
                    childrenIndexMap.remove(childuid);
                    for (String s : childrenIndexMap.keySet()) {
                        if (childrenIndexMap.get(s) > index) {
                            childrenIndexMap.replace(s, childrenIndexMap.get(s) - 1);
                        }
                    }
                    //Log.i("child removed", childuid);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mServer.getUsersRef().child(currentUser.getUid()).child("linkedaccounts").addChildEventListener(mChildListListener);
    }

    private void clearChildListener() {
        mServer.getUsersRef().child(currentUser.getUid()).child("linkedaccounts").removeEventListener(mChildListListener);
        for (Map.Entry<String, ValueEventListener> entry : mChildrenInfoListener.entrySet()) {
            mServer.getUsersRef().child(entry.getKey()).removeEventListener(entry.getValue());
        }
    }

}
