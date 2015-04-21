package csc495.potato.walk.walkpotato.UI.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class ResetReceiver extends BroadcastReceiver {
    public ResetReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        SharedPreferences sPref = context.getSharedPreferences("app_usage", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.clear();
        editor.commit();
    }
}
