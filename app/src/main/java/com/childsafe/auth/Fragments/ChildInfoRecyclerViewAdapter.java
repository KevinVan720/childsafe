package com.childsafe.auth.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.childsafe.auth.Model.ServerAdapter;
import com.childsafe.auth.Model.User;
import com.childsafe.auth.R;
import com.childsafe.auth.Utils.NetWorkUtil;
import com.childsafe.auth.Utils.TimeUtil;
import com.childsafe.auth.Utils.UIUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ChildInfoRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<User> childList;
    private List<User> childrequestList;
    private int lastPosition = -1;
    private ServerAdapter mServer;
    private User currentUser;

    ChildInfoRecyclerViewAdapter(List<User> childList, List<User> childrequestList, ServerAdapter server, User user, Context context) {
        this.childList = childList;
        this.childrequestList = childrequestList;
        this.context = context;
        this.mServer = server;
        this.currentUser = user;
    }

    @Override
    public int getItemViewType(int position) {
        // Determine whether is a child card or child request card
        if (position < childList.size()) {
            return 0;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_card, parent, false);
            return new ChildInfoHolder(layoutView);
        } else {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_request_card, parent, false);
            return new ChildRequestHolder(layoutView);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //Update child card
        if (childList != null && position < childList.size()) {
            ChildInfoHolder childinfoholder = (ChildInfoHolder) holder;
            User child = childList.get(position);

            childinfoholder.childName.setText(child.getName());
            childinfoholder.childEmail.setText(child.getEmail());
            childinfoholder.child = child;

            childinfoholder.childImage.setImageResource(R.mipmap.ic_portrait);
            String imageurl = child.getImageUrl();
            if (imageurl != null) {
                String trueimageurl = imageurl.substring(36);
                //Log.i("Image url", imageurl);
                StorageReference image_ref = mServer.getStorageRef().child(trueimageurl);
                final long ONE_MEGABYTE = 1024 * 1024;
                image_ref.getBytes(ONE_MEGABYTE).addOnSuccessListener((byte[] bytes) -> {
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    childinfoholder.childImage.setImageBitmap(decodedByte);
                }).addOnFailureListener((@NonNull Exception exception) -> {
                    Toast.makeText(context, context.getText(R.string.update_child_photo_fail), Toast.LENGTH_SHORT).show();
                });
            }


            if (child.getStatus().equals("Safe")) {
                childinfoholder.childStatus.setText(context.getText(R.string.child_status_safe));
                childinfoholder.childStatus.setTextColor(context.getResources().getColor(R.color.safe_green));
            } else {
                childinfoholder.childStatus.setText(context.getText(R.string.child_status_lost));
                childinfoholder.childStatus.setTextColor(context.getResources().getColor(R.color.lost_red));
            }

            if (child.getLastTime() != null) {
                Long seconds=TimeUtil.getTimeDiff(child.getLastTime(), TimeUtil.getCurrentTime());
                Long days=seconds/(3600*24);
                seconds-=days*(3600*24);
                Long hours = seconds / 3600;
                seconds -= (hours * 3600);
                Long minutes = seconds / 60;
                String timediff;
                if(days>0)
                {
                    timediff=days.toString()+context.getText(R.string.time_day);
                }
                else if(hours>0)
                {
                    timediff=hours.toString()+context.getText(R.string.time_hour);
                }
                else
                {
                    timediff=minutes.toString()+context.getText(R.string.time_minute);
                }
                childinfoholder.lasttime.setText(context.getText(R.string.for_time) + " " + timediff);
            } else {
                Log.i("status update", ((Integer) position).toString());
                mServer.updateChildStatus(child.getUid(), child.getStatus());
            }

            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
                childinfoholder.itemView.startAnimation(animation);
                lastPosition = position;
            }
        }

        //Update child request card
        if (childrequestList != null && position >= childList.size() && position < childList.size() + childrequestList.size()) {
            ChildRequestHolder childrequestholder = (ChildRequestHolder) holder;
            User child = childrequestList.get(position - childList.size());
            childrequestholder.childName.setText(child.getName());
            childrequestholder.childEmail.setText(child.getEmail());

            Button accept_button = childrequestholder.itemView.findViewById(R.id.accept_request_button);
            accept_button.setOnClickListener((View view) -> {
                User temp = childrequestList.get(position - childList.size());
                childrequestList.remove(position - childList.size());
                notifyItemRemoved(position);
                mServer.updateBindingStatus(currentUser.getUid(), temp.getUid(), 1);
            });

            Button decline_button = childrequestholder.itemView.findViewById(R.id.decline_request_button);
            decline_button.setOnClickListener((View view) -> {
                User temp = childrequestList.get(position - childList.size());
                childrequestList.remove(position - childList.size());
                notifyItemRemoved(position);
                mServer.removeBinding(currentUser.getUid(), temp.getUid());
            });

            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
                childrequestholder.itemView.startAnimation(animation);
                lastPosition = position;
            }
        }
    }

    @Override
    public int getItemCount() {
        return childList.size() + childrequestList.size();
    }

    public List<User> getChildList() {
        return childList;
    }

    //Child card view Holder
    private class ChildInfoHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        public TextView childName;
        public TextView childStatus;
        public TextView childEmail;
        public TextView lasttime;
        public ImageView childImage;
        public User child;

        public ChildInfoHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnLongClickListener(this);
            childName = itemView.findViewById(R.id.child_name);
            childStatus = itemView.findViewById(R.id.child_status);
            childEmail = itemView.findViewById(R.id.child_email);
            childImage = itemView.findViewById(R.id.child_card_image);
            lasttime = itemView.findViewById(R.id.last_time);

        }

        @Override
        public boolean onLongClick(View view) {
            // Handle long click

            if (NetWorkUtil.hasNetworkConnection(context)) {
                //Log.i("child status", childStatus.getText().toString());
                if (childStatus.getText().toString().toUpperCase().equals(context.getString(R.string.child_status_safe).toUpperCase())) {
                    Snackbar snackbar = UIUtil.makeSnackbar(view, context.getString(R.string.claim_child_lost), context.getColor(R.color.lost_red));
                    snackbar.setAction(context.getText(R.string.confirm), (View v) -> {
                        mServer.getUserById(child.getUid()).addOnCompleteListener((@NonNull Task<User> task) -> {
                            if (task.isSuccessful()) {
                                User child = task.getResult();
                                if (child.getStatus().equals("Safe")) {
                                    mServer.updateChildStatus(child.getUid(), "Lost");
                                    //call 911
                                    if (context.checkSelfPermission(
                                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                        return;
                                    }
                                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(context.getText(R.string.tel_head) + context.getResources().getString(R.string.call_phone)));
                                    context.startActivity(intent);

                                } else {
                                    Toast.makeText(context, context.getText(R.string.child_already_lost), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, context.getText(R.string.unable_commu_server), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                    snackbar.show();
                } else {
                    Snackbar snackbar = UIUtil.makeSnackbar(view, context.getString(R.string.claim_child_found), context.getColor(R.color.safe_green));
                    snackbar.setAction(context.getText(R.string.confirm), (View v) -> {
                        mServer.getUserById(child.getUid()).addOnCompleteListener((@NonNull Task<User> task) -> {
                            if (task.isSuccessful()) {
                                User child = task.getResult();
                                if (child.getStatus().equals("Lost")) {
                                    //do some clean up
                                    mServer.updateChildStatus(child.getUid(), "Safe");
                                    mServer.clearGPSLocation(child.getUid());
                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                                    notificationManager.cancel(child.getUid().hashCode());
                                } else {
                                    Toast.makeText(context, context.getText(R.string.child_already_found), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, context.getText(R.string.unable_commu_server), Toast.LENGTH_SHORT).show();
                            }
                        });

                    });
                    snackbar.show();
                }

            } else {
                Toast.makeText(context, context.getText(R.string.unable_commu_server), Toast.LENGTH_SHORT).show();
            }
            // Return true to indicate the click was handled
            return true;
        }
    }

    //Child request card view Holder
    private class ChildRequestHolder extends RecyclerView.ViewHolder {
        public TextView childName;
        public TextView childEmail;

        public ChildRequestHolder(@NonNull View itemView) {
            super(itemView);
            childName = itemView.findViewById(R.id.child_request_name);
            childEmail = itemView.findViewById(R.id.child_request_email);
        }

    }

}

