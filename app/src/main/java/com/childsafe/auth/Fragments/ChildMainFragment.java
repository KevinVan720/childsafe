package com.childsafe.auth.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.childsafe.auth.Model.User;
import com.childsafe.auth.R;
import com.childsafe.auth.Utils.AddEmailSnackbar;
import com.childsafe.auth.Utils.UIUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ChildMainFragment extends MainBaseFragment {

    private AddEmailSnackbar addEmailSnackbar;
    private Snackbar dangerSnackbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_child_main, parent, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        FloatingActionButton fabcall = getActivity().findViewById(R.id.fabcall);
        fabcall.hide();

        initFabs();
    }

    @Override
    public void onPause() {
        super.onPause();
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.hide();
        FloatingActionButton fabTop = getActivity().findViewById(R.id.fabTop);
        fabTop.hide();
        if (addEmailSnackbar != null) {
            addEmailSnackbar.dismiss();
        }
        if (dangerSnackbar != null) {
            dangerSnackbar.dismiss();
        }
    }

    private void initFabs()
    {
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        updateFabIcon(fab, R.drawable.ic_add);
        fab.setOnClickListener((View view) -> {
            if (addEmailSnackbar == null) {
                updateFabIcon(fab, R.drawable.ic_close);
                addEmailSnackbar = AddEmailSnackbar.make((ViewGroup) view.getParent(), AddEmailSnackbar.LENGTH_INDEFINITE);
                addEmailSnackbar.setInputText();
                addEmailSnackbar.setAction(getString(R.string.link_parent), (View v) -> {
                    String parent_email = addEmailSnackbar.textView.getText().toString();
                    //Link parent account
                    if (parent_email != null) {
                        Query queryEmail = mServer.getUserByEmail(parent_email);

                        queryEmail.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() == 1) {
                                    for (DataSnapshot parentdata : dataSnapshot.getChildren()) {
                                        User parent = parentdata.getValue(User.class);
                                        //Log.i("parent", parent.getEmail());
                                        if (parent.getRole().equals("Parent")) {

                                            sendLinkRequest(parent);

                                        } else {
                                            Toast.makeText(getActivity(), getString(R.string.check_parent_account), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    Toast.makeText(getActivity(), getString(R.string.email_not_found), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    updateFabIcon(fab, R.drawable.ic_add);
                });
                addEmailSnackbar.show();
            } else {
                addEmailSnackbar.dismiss();
                addEmailSnackbar = null;
                updateFabIcon(fab, R.drawable.ic_add);
            }
        });

        FloatingActionButton fabTop = getActivity().findViewById(R.id.fabTop);
        fabTop.show();
        fabTop.setOnClickListener((View v) -> {
            dangerSnackbar = UIUtil.makeSnackbar(v, getString(R.string.are_you_in_danger), getResources().getColor(R.color.lost_red)).setAction(getString(R.string.yes), (View view) -> {
                if (currentUser.getStatus().equals("Safe")) {
                    Log.i("status", currentUser.getStatus());
                    mServer.updateChildStatus(currentUser.getUid(), "Lost");
                }
            });
            dangerSnackbar.show();
        });
    }

    private void updateFabIcon(FloatingActionButton fab, int resId) {
        fab.hide();
        fab.setImageResource(resId);
        fab.show();
    }

    private void sendLinkRequest(User parent) {
        mServer.getBindingRef(parent.getUid(), currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Integer islinked = dataSnapshot.getValue(Integer.class);
                            if (islinked > 0) {
                                Toast.makeText(getActivity(), parent.getEmail() + getString(R.string.parent_already_linked), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), getString(R.string.parent_already_sent), Toast.LENGTH_SHORT).show();
                            }
                        }
                        //Not sent ever
                        else {
                            mServer.updateBindingStatus(parent.getUid(), currentUser.getUid(), 0);
                            Toast.makeText(getActivity(), getString(R.string.link_sent) + parent.getEmail(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
    }

}
