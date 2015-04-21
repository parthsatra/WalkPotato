package csc495.potato.walk.walkpotato.UI.backgroundtasks;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import csc495.potato.walk.walkpotato.UI.Fragments.StepStatusFragment;

public class LogReaderService extends Service implements Runnable {

    public Context context;
    private Thread thread = null;
    private SharedPreferences sPref;
    private SharedPreferences curOpen;
    private Handler handler;

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

        sPref = getSharedPreferences("app_usage", MODE_PRIVATE);
        curOpen = getSharedPreferences("opened_apps", MODE_PRIVATE);

        setupHandler();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        Intent resetIntent = new Intent("reset_prefs");
        PendingIntent pResetIntent = PendingIntent.getBroadcast(context, 0, resetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), 1000 * 60 * 60 * 24, pResetIntent);

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
                            if (StepStatusFragment.getStepsTakenToday() < StepStatusFragment.getGoalSteps()) {
                                Message message = new Message();
                                message.what = 1;
                                message.obj = pi.pkgList[0].trim();
                                handler.sendMessage(message);
                                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("csc495.potato.walk.walkpotato");
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                                startActivity(launchIntent);
                            }
                        }
                        break;
                    }
                }

                //Modify this to control how often the app stack is polled!
                Thread.sleep(250);
            } catch (Exception e) {
                Log.e("Service Error : ", e.getMessage());
            }
    }

    private void setupHandler() {
        HandlerThread ht = new HandlerThread("potato_handler");
        ht.start();

        handler = new Handler(ht.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1: {
                        if (!curOpen.contains(msg.obj.toString())) {
                            Map<String, ?> lastMap = curOpen.getAll();
                            SharedPreferences.Editor editor = curOpen.edit();
                            SharedPreferences.Editor usageEditor = sPref.edit();

                            for (String appName : lastMap.keySet()) {
                                Log.e("TWERP", appName + " 1");
                                usageEditor.putLong(appName,
                                        sPref.getLong(appName, 0L)
                                                + (System.currentTimeMillis() - curOpen.getLong(appName, 0L)));
                                editor.remove(appName);
                            }

                            editor.commit();
                            usageEditor.commit();

                            editor.putLong(msg.obj.toString(), System.currentTimeMillis());
                            editor.commit();
                        }
                    }
                }
                return false;
            }
        });
    }
}
