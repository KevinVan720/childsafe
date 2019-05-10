package com.childsafe.auth.Fragments;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.childsafe.auth.R;
import com.childsafe.auth.Model.ServerAdapter;
import com.childsafe.auth.Utils.SettingsUtil;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

import androidx.annotation.NonNull;

import static android.app.Activity.RESULT_OK;

public class EditProfileFragment extends MainBaseFragment {

    private static final String TAG = EditProfileFragment.class.getSimpleName();

    private static int RESULT_LOAD_IMAGE = 20;
    protected EditText nametext;
    protected ImageView portrait;
    protected Uri selectedImageURI;

    public EditProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mServer = new ServerAdapter();
        Log.i(TAG, "resumed");
        ((TextView) getView().findViewById(R.id.profile_name)).setText(currentUser.getName());
        ((TextView) getView().findViewById(R.id.profile_email)).setText(currentUser.getEmail());
        nametext = getView().findViewById(R.id.profile_edit_name);
        portrait = getView().findViewById(R.id.profile_image);
        ImageView portrait_cam = getView().findViewById(R.id.profile_image_camera);
        if(SettingsUtil.getDarkMode(getActivity()))
        {
            portrait_cam.setImageResource(R.drawable.ic_camera_white);
        }

        if (currentUser.getImageUrl() != null) {
            portrait.setImageDrawable(((ImageView) getActivity().findViewById(R.id.personal_photo)).getDrawable());
        }

        if (selectedImageURI != null) {
            Glide.with(getContext()).load(selectedImageURI)
                    .into(portrait);
           // Picasso.with(getContext()).load(selectedImageURI).noPlaceholder().centerCrop().fit()
            // .into(portrait);
        }

        portrait.setOnClickListener((View v) -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
        });


        getView().findViewById(R.id.profile_save_button).setOnClickListener((View v) -> {
            if (nametext == null
                    || nametext.getText().toString().length() == 0
                    || nametext.getText().toString().length() > 50
                    || nametext.getText().toString().contains("\n")) {
                Toast.makeText(getActivity(), getString(R.string.invalid_name), Toast.LENGTH_SHORT).show();
            } else {
                String new_name = nametext.getText().toString();
                mServer.updateUserInfo(currentUser.getUid(),"name", new_name);
                if (selectedImageURI != null) {
                    byte[] data = null;
                    try {
                        ContentResolver cr = getContext().getContentResolver();
                        InputStream inputStream = cr.openInputStream(selectedImageURI);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
                        data = baos.toByteArray();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    mServer.uploadImage(data, currentUser.getUid()).addOnFailureListener((@NonNull Exception exception) -> {
                        Toast.makeText(getActivity(), getString(R.string.unable_commu_server), Toast.LENGTH_SHORT).show();
                    }).addOnSuccessListener((UploadTask.TaskSnapshot taskSnapshot) -> {
                        String uniqueID = UUID.randomUUID().toString();
                        mServer.updateUserInfo(currentUser.getUid(),"imageurl", uniqueID + currentUser.getUid() + "/portrait.jpg");
                    });
                }
                Log.i("new name set", new_name);
                if (getFragmentManager().getBackStackEntryCount() != 0) {
                    getFragmentManager().popBackStack();
                }
            }
        });

        getView().findViewById(R.id.profile_cancel_button).setOnClickListener((View v) -> {
            if (getFragmentManager().getBackStackEntryCount() != 0) {
                getFragmentManager().popBackStack();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("ProfileFrag", "activity received");
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_LOAD_IMAGE) {

                selectedImageURI = data.getData();
                Glide.with(getContext()).load(selectedImageURI)
                        .into(portrait);
            }

        }
    }

}
