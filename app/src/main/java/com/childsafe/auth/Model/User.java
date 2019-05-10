package com.childsafe.auth.Model;

import com.childsafe.auth.Utils.TimeUtil;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User implements Serializable {

    private String uid;
    private String name;
    private String email;
    private String role;
    private String status;
    private String imageurl;
    private String currentDevice;
    private String lastTime;
    private Map<String, Integer> linkedaccounts = new HashMap<>();

    public User() {
    }

    public User(String uid, String email, String role, String status, String name) {
        this.uid = uid;
        this.name = name;
        this.status = status;
        this.role = role;
        this.email = email;
        this.lastTime= "2019-001-00-00";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrentDevice() {
        return currentDevice;
    }

    public void setCurrentDevice(String currentDevice) {
        this.currentDevice = currentDevice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public String getUid() {
        return uid;
    }

    public String getImageUrl() {
        return imageurl;
    }

    public void setImageUrl(String imageurl) {
        this.imageurl=imageurl;
    }

    public Map<String, Integer> getLinkedaccounts()
    {
        return linkedaccounts;
    }

    public String getLastTime()
    {
        return lastTime;
    }

    public void setLastTime(String lastTime)
    {
        this.lastTime=lastTime;
    }

}