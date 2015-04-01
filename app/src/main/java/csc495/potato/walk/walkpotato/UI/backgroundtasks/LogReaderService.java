package csc495.potato.walk.walkpotato.UI.backgroundtasks;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogReaderService extends Service implements Runnable {

    private Thread thread = null;
    public Context context;

    public LogReaderService() {
    }

    @Override
    public void onCreate() {


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        context = this;
        thread = new Thread(this);
        thread.start();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void run() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        while (true)
            try {
                final List<ActivityManager.RunningAppProcessInfo> pis = mActivityManager.getRunningAppProcesses();
                for (ActivityManager.RunningAppProcessInfo pi : pis) {
                    if (pi.pkgList.length == 1 && pi.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        SharedPreferences blockedApps = this.getSharedPreferences("blocked_apps", Context.MODE_PRIVATE);
                        Set<String> blockedAppSet = blockedApps.getStringSet("app_list", new HashSet<String>());
                        if (blockedAppSet.contains(pi.pkgList[0].trim())) {
                            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("csc495.potato.walk.walkpotato");
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                            startActivity(launchIntent);
                        }
                        break;
                    }
                }

                //Modif this to control how often the app stack is polled!
                Thread.sleep(250);
            } catch (Exception e) {
                Log.e("Service Error : ", e.getMessage());
            }
    }
}
