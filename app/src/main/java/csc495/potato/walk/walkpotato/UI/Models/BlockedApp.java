package csc495.potato.walk.walkpotato.UI.Models;

import android.graphics.drawable.Drawable;

public class BlockedApp {
    private String appName;
    private String androidAppStr;
    private Drawable appIcon;
    private long id;

    public BlockedApp() {
    }

    public BlockedApp(String appName, String androidAppStr, Drawable appIcon, long id) {
        this.appName = appName;
        this.androidAppStr = androidAppStr;
        this.appIcon = appIcon;
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAndroidAppStr() {
        return androidAppStr;
    }

    public void setAndroidAppStr(String androidAppStr) {
        this.androidAppStr = androidAppStr;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
