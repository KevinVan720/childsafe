package com.childsafe.auth.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.childsafe.auth.R;
import com.childsafe.auth.Utils.SettingsUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import androidx.work.WorkManager;

public class SettingsFragment extends MainBaseFragment {

    private int checked = 0;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    public void onResume()
    {
        super.onResume();
        final Switch modeSwitch = getActivity().findViewById(R.id.dark_mode_switch);

        if(SettingsUtil.getDarkMode(getActivity()))
        {
            modeSwitch.setChecked(true);
        }

        modeSwitch.setOnCheckedChangeListener( (CompoundButton compoundButton, boolean bChecked) -> {
            if (bChecked) {
                SettingsUtil.setDarkMode(getActivity(), true);
                Intent intent = getActivity().getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                getActivity().finish();
                startActivity(intent);
            } else {
                SettingsUtil.setDarkMode(getActivity(), false);
                Intent intent = getActivity().getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                getActivity().finish();
                startActivity(intent);
            }
        });


        HashMap<String, String> langMap=new HashMap<>();
        if(SettingsUtil.getLocale(getContext()).equals("zh"))
        {
            langMap.put("zh", "中文");
            langMap.put("en", "英文(English)");
        }
        else if(SettingsUtil.getLocale(getContext()).equals("en"))
        {
            langMap.put("zh", "Chinese(中文)");
            langMap.put("en", "English");
        }

        Set<String> s=langMap.keySet();
        int n = s.size();
        String langList[] = new String[n];
        System.arraycopy(s.toArray(), 0, langList, 0, n);
        Collection<String> cs=langMap.values();
        String langDisplayList[] = new String[n];
        System.arraycopy(cs.toArray(), 0, langDisplayList, 0, n);

        ArrayAdapter adapter = new ArrayAdapter<String>(
                getContext(), R.layout.spinner_item_dropdown,langDisplayList);

        Spinner langSpinner=getActivity().findViewById(R.id.spinner1);
        langSpinner.setAdapter(adapter);

        for (int j=0; j<langList.length; j++)
        {
            if(SettingsUtil.getLocale(getContext()).equals(langList[j]))
            {
                langSpinner.setSelection(j, true);
                break;
            }
        }

        langSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                if(++checked > 0) {

                    for (Map.Entry<String, String> entry : langMap.entrySet())
                    {
                        if (parent.getItemAtPosition(pos).toString().equals(entry.getValue())) {
                            SettingsUtil.setLocale(parent.getContext(), entry.getKey());
                            break;
                        }
                    }
                    Intent intent = getActivity().getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    getActivity().finish();
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        //Only parent can change notification
        if(currentUser.getRole().equals("Parent"))
        {
            LinearLayout layout = getActivity().findViewById(R.id.settings_layout);

            Switch notificationSwitch= new Switch(getContext());
            notificationSwitch.setText(R.string.allow_notification);
            notificationSwitch.setTextAppearance(R.style.TextAppearance_ChildSafe_Settings);
            notificationSwitch.setPadding(dp_to_px(12),dp_to_px(26),0,0);

            layout.addView(notificationSwitch);

            if(SettingsUtil.getNotification(getActivity()))
            {
                notificationSwitch.setChecked(true);
            }
            else
            {
                notificationSwitch.setChecked(false);
            }

            notificationSwitch.setOnCheckedChangeListener((CompoundButton compoundButton, boolean bChecked) -> {
                if (bChecked) {
                    SettingsUtil.setNotification(getActivity(), true);
                } else {
                    SettingsUtil.setNotification(getActivity(), false);
                    WorkManager.getInstance().cancelAllWorkByTag("PARENTNOTIFY");
                }
            });
        }

    }

    public int dp_to_px(int dp)
    {
        final float scale = getResources().getDisplayMetrics().density;
        return (int)(dp*scale+0.5f);
    }

}
