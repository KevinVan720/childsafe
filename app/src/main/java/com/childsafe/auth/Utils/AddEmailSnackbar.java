package com.childsafe.auth.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.childsafe.auth.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

public class AddEmailSnackbar extends BaseTransientBottomBar<AddEmailSnackbar> {

    public EditText textView;

    private AddEmailSnackbar(ViewGroup parent, View content, ContentViewCallback callback) {
        super(parent, content, callback);
    }

    public static AddEmailSnackbar make(@NonNull ViewGroup parent, @Duration int duration) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View content = inflater.inflate(R.layout.add_parent_snac_layout, parent, false);
        final ContentViewCallback viewCallback = new ContentViewCallback(content);
        final AddEmailSnackbar customSnackbar = new AddEmailSnackbar(parent, content, viewCallback);

        customSnackbar.getView().setPadding(0, 0, 0, 0);
        customSnackbar.setDuration(duration);
        return customSnackbar;
    }

    public AddEmailSnackbar setInputText() {
        textView = getView().findViewById(R.id.snackbar_text);
        return this;
    }

    public AddEmailSnackbar setAction(CharSequence text, final View.OnClickListener listener) {
        Button actionView = (Button) getView().findViewById(R.id.snackbar_action);
        actionView.setText(text);
        actionView.setVisibility(View.VISIBLE);
        actionView.setOnClickListener((View view) -> {
            listener.onClick(view);
            // Now dismiss the Snackbar
            dismiss();
        });
        return this;
    }

    private static class ContentViewCallback implements BaseTransientBottomBar.ContentViewCallback {

        private View content;

        public ContentViewCallback(View content) {
            this.content = content;
        }

        @Override
        public void animateContentIn(int delay, int duration) {
            ViewCompat.setScaleY(content, 0f);
            ViewCompat.animate(content).scaleY(1f).setDuration(duration).setStartDelay(delay);
        }

        @Override
        public void animateContentOut(int delay, int duration) {
            ViewCompat.setScaleY(content, 1f);
            ViewCompat.animate(content).scaleY(0f).setDuration(duration).setStartDelay(delay);
        }
    }

}
