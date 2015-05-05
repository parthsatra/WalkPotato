package csc495.potato.walk.walkpotato.UI.backgroundtasks;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import csc495.potato.walk.walkpotato.R;
import csc495.potato.walk.walkpotato.UI.Fragments.SettingsFragment;
import csc495.potato.walk.walkpotato.UI.Fragments.StepStatusFragment;
import csc495.potato.walk.walkpotato.UI.MainActivity;

public class LogReaderService extends Service implements Runnable {

    public Context context;
    private Thread thread = null;
    private SharedPreferences sPref;
    private SharedPreferences curOpen;
    private Handler handler;
    private NotificationManager mNotificationManager;
    private long timeStarted = System.currentTimeMillis();

    public LogReaderService() {
    }

    @Override
    public void onCreate() {


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        context = this;

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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
        SharedPreferences prefs = getSharedPreferences("walkpotato", Context.MODE_MULTI_PROCESS);
        int goalSteps = prefs.getInt("goal", SettingsFragment.DEFAULT_GOAL);

        while (true)
            try {
                final List<ActivityManager.RunningAppProcessInfo> pis = mActivityManager.getRunningAppProcesses();
                for (ActivityManager.RunningAppProcessInfo pi : pis) {
                    if (pi.pkgList.length == 1 && pi.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        SharedPreferences blockedApps = this.getSharedPreferences("blocked_apps", Context.MODE_PRIVATE);
                        Set<String> blockedAppSet = blockedApps.getStringSet("app_list", new HashSet<String>());
                        if (blockedAppSet.contains(pi.pkgList[0].trim())) {

                            Message message = new Message();
                            message.what = 1;
                            message.obj = pi.pkgList[0].trim();
                            //handler.sendMessage(message);

                            if (StepStatusFragment.getStepsTakenToday() < goalSteps) {

                                double timeLeftPerc = (double) StepStatusFragment.getStepsTakenToday() / goalSteps;

                                if (StepStatusFragment.getEntertainmentTime() <= 0
                                        || timeLeftPerc == 0) {
                                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("csc495.potato.walk.walkpotato");
                                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                                    startActivity(launchIntent);
                                } else {
                                    createNotification(timeLeftPerc);
                                }
                            } else {
                                StepStatusFragment.resetTimer();
                            }

                            break;
                        }
                    }

                    Message message = new Message();
                    message.what = 2;
                    //handler.sendMessage(message);
                    mNotificationManager.cancel(45);
                    StepStatusFragment.setUsedUpTime((int) ((System.currentTimeMillis() - timeStarted) / 1000));
                    StepStatusFragment.setEntertainmentTime();
                    timeStarted = System.currentTimeMillis();
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
                    break;

                    case 2: {
                        Map<String, ?> lastMap = curOpen.getAll();
                        SharedPreferences.Editor editor = curOpen.edit();
                        SharedPreferences.Editor usageEditor = sPref.edit();

                        for (String appName : lastMap.keySet()) {
                            usageEditor.putLong(appName,
                                    sPref.getLong(appName, 0L)
                                            + (System.currentTimeMillis() - curOpen.getLong(appName, 0L)));
                            editor.remove(appName);
                        }

                        editor.commit();
                        usageEditor.commit();
                    }
                    break;
                }
                return false;
            }
        });
    }

    private void createNotification(double timeLeftPerc) {

        long percTime = (long) ((timeLeftPerc * StepStatusFragment.MAX_ENTERTAINMENT_TIME) - StepStatusFragment.getUsedUpTime());
        long diff = System.currentTimeMillis() - timeStarted;
        long timeLeftNow = (percTime - (diff / 1000)) * 1000;

        if (timeLeftNow == 0)
            StepStatusFragment.clearEntertainmentTime();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(convertTime(timeLeftNow))
                        .setContentText("Achieve goal to reset the timer!");

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(45, mBuilder.build());
    }

    private String convertTime(long millis) {
        return String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }
}
