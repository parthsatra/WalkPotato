package csc495.potato.walk.walkpotato.UI.fitlib.common;

import android.util.Log;

/**
 * Created by Constantine Mars on 1/19/2015.
 */
public abstract class Display {
    private String tag;

    protected Display(String tag) {
        this.tag = tag;
    }

    public abstract void show(String msg);

    public void log(String msg) {
        Log.d(tag, msg);
    }
}
