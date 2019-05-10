package com.childsafe.auth.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.childsafe.auth.R;

import androidx.fragment.app.Fragment;

public class HelpFragment extends MainBaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.fragment_help, parent, false);
        return root;
    }
}
