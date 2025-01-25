package com.clase.engenios_manuelimdbapp_v20.models;

public class ActivityLog {
    private String loginTime;
    private String logoutTime;

    public ActivityLog(String loginTime, String logoutTime) {
        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
    }

    // Getters y Setters
    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public String getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(String logoutTime) {
        this.logoutTime = logoutTime;
    }
}