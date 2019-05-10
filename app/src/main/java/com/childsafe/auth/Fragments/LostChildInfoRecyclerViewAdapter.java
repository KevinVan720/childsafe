package com.childsafe.auth.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.childsafe.auth.R;
import com.childsafe.auth.Model.ServerAdapter;
import com.childsafe.auth.Model.User;
import com.childsafe.auth.Utils.TimeUtil;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

public class LostChildInfoRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private AppCompatActivity mActivity;
    private List<User> childList;
    private int lastPosition = -1;
    private ServerAdapter mServer;

    LostChildInfoRecyclerViewAdapter(List<User> childList, ServerAdapter server, Context context, AppCompatActivity activity) {
        this.childList = childList;
        this.context = context;
        this.mServer = server;
        this.mActivity=activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_card, parent, false);
            return new LostChildInfoRecyclerViewAdapter.LostChildInfoHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //Update child card
        if (childList != null && position < childList.size()) {
            LostChildInfoRecyclerViewAdapter.LostChildInfoHolder childinfoholder = (LostChildInfoRecyclerViewAdapter.LostChildInfoHolder) holder;
            User child = childList.get(position);
            if(child.getEmail().equals("Unknown"))
            {
                return ;
            }
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
                childinfoholder.lasttime.setText(context.getText(R.string.for_time) +" "+ timediff);
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
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    //Child card view Holder
    private class LostChildInfoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView childName;
        public TextView childEmail;
        public ImageView childImage;
        public TextView childStatus;
        public TextView lasttime;
        public User child;

        public LostChildInfoHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            childName = itemView.findViewById(R.id.child_name);
            childEmail = itemView.findViewById(R.id.child_email);
            childImage = itemView.findViewById(R.id.child_card_image);
            childStatus = itemView.findViewById(R.id.child_status);
            lasttime=itemView.findViewById(R.id.last_time);
        }

        @Override
        public void onClick(View view) {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            GoogleMapFragment frag = new GoogleMapFragment();
            frag.addCurrentUser(child);
            fragmentTransaction.replace(R.id.main_content, frag, "GOOGLEMAPFRAGMENT");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }
}
