package com.childsafe.auth.Fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.childsafe.auth.R;
import com.childsafe.auth.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Vector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LostChildrenGridFragment extends MainBaseFragment {

    private static final String TAG = LostChildrenGridFragment.class.getSimpleName();

    private Handler mHandler;
    private Runnable mRunnable;

    private List<User> children;
    private RecyclerView recyclerView;
    private RecyclerView.ItemDecoration cardDecoration=null;
    private LostChildInfoRecyclerViewAdapter mAdapter;
    private ValueEventListener lostChildrenListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.fragment_children_grid, parent, false);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "resumed");

        children = new Vector<>();

        initRecyclerView();

        lostChildrenListener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    children.clear();
                    for (DataSnapshot childdata : dataSnapshot.getChildren()) {
                        User child = childdata.getValue(User.class);
                        if(child.getRole().equals("Child"))
                        {
                            children.add(child);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                } else {
                    //no lost child
                    Toast.makeText(getContext(),R.string.no_child_lost,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mServer.getLostChildrenList().addValueEventListener(lostChildrenListener);

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

        mServer.getUsersRef().removeEventListener(lostChildrenListener);
        clearRecyclerView();

        mHandler.removeCallbacks(mRunnable);

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void initRecyclerView()
    {
        // Set up the RecyclerView
        recyclerView = mView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mView.getContext(), 1, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);
        mAdapter = new LostChildInfoRecyclerViewAdapter(children, mServer, mView.getContext(),(AppCompatActivity) getActivity());
        recyclerView.setAdapter(mAdapter);

        if(cardDecoration==null)
        {
            int sidePadding = getResources().getDimensionPixelSize(R.dimen.grid_spacing_small);
            cardDecoration=new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view,
                                           RecyclerView parent, RecyclerView.State state) {
                    outRect.left = sidePadding;
                    outRect.right = sidePadding;
                }
            };
            recyclerView.addItemDecoration(cardDecoration);
        }

    }

    private void clearRecyclerView()
    {
        //clear card
        recyclerView.removeItemDecoration(cardDecoration);
        cardDecoration=null;
        mAdapter = null;
    }

}
