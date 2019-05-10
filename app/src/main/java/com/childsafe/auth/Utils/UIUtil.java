package com.childsafe.auth.Utils;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import com.childsafe.auth.R;
import com.google.android.material.snackbar.Snackbar;

public class UIUtil {
    static public Snackbar makeSnackbar(View v, String text, int actionTextColor)
    {
        final ForegroundColorSpan whiteSpan = new ForegroundColorSpan(v.getContext().getColor(R.color.colorAccent));
        SpannableStringBuilder snackbarText = new SpannableStringBuilder(text);
        snackbarText.setSpan(whiteSpan, 0, snackbarText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        Snackbar snackbar= Snackbar.make(v, snackbarText, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(actionTextColor);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(v.getContext().getResources().getColor(R.color.DarkcolorPrimary));
        return snackbar;
    }
}
