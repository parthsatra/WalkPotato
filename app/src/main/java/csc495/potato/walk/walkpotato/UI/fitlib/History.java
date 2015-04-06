package csc495.potato.walk.walkpotato.UI.fitlib;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import csc495.potato.walk.walkpotato.UI.fitlib.common.Display;


public class History extends IntentService{
    private static GoogleApiClient client;
    private static final Display display = new Display(History.class.getName()) {
        @Override
        public void show(String msg) {
            log(msg);

            //add(FitPagerAdapter.FragmentIndex.HISTORY, msg);
//                                InMemoryLog.getInstance().add(FitPagerAdapter.FragmentIndex.HISTORY, msg);
        }
    };
    private static int stepsTakenToday=0;
    public static final String SERVICE_REQUEST_TYPE = "requestType";
    public static final String HISTORY_INTENT = "fitHistory";
    public static final String HISTORY_EXTRA_STEPS_TODAY = "stepsToday";
    public static final int TYPE_GET_STEP_TODAY_DATA = 1;
    public static final int TYPE_REQUEST_CONNECTION = 2;
    public static final String FIT_NOTIFY_INTENT = "fitStatusUpdateIntent";
    public static final String FIT_EXTRA_CONNECTION_MESSAGE = "fitFirstConnection";
    public static final String FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE = "fitExtraFailedStatusCode";

    public History()
    {
        super("HistoryService");
    }

    public void readWeekBefore(Date date) {
        Calendar cal = Calendar.getInstance();
//        Date now = new Date();
        cal.setTime(date);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        read(startTime, endTime);
    }
    public int currentDay(Date date) {
        Calendar cal = Calendar.getInstance();
//        Date now = new Date();
        cal.setTime(date);
        long endTime = cal.getTimeInMillis();
        //cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startTime = cal.getTimeInMillis();

        return read(startTime, endTime);
    }
    public int read(long start, long end) {
        final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");// SimpleDateFormat.getDateInstance();
        display.show("history reading range: " + dateFormat.format(start) + " - " + dateFormat.format(end));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(start, end, TimeUnit.MILLISECONDS)
                .build();

        Fitness.HistoryApi.readData(client, readRequest).setResultCallback(new ResultCallback<DataReadResult>() {

            @Override
            public void onResult(DataReadResult dataReadResult) {
                if (dataReadResult.getBuckets().size() > 0) {
                    display.show("DataSet.size(): "
                            + dataReadResult.getBuckets().size());
                    for (Bucket bucket : dataReadResult.getBuckets()) {
                        List<DataSet> dataSets = bucket.getDataSets();
                        for (DataSet dataSet : dataSets) {
                            display.show("dataSet.dataType: " + dataSet.getDataType().getName());
                            History.stepsTakenToday=0;
                            for (DataPoint dp : dataSet.getDataPoints()) {
                                for(Field field : dp.getDataType().getFields()) {
                                    History.stepsTakenToday+= dp.getValue(field).asInt();
                                    Log.d("History",dp.getValue(field).asInt()+"" );
                                }
                            }
                        }
                    }
                } else if (dataReadResult.getDataSets().size() > 0) {
                    display.show("dataSet.size(): " + dataReadResult.getDataSets().size());
                    for (DataSet dataSet : dataReadResult.getDataSets()) {
                        display.show("dataType: " + dataSet.getDataType().getName());

                        for (DataPoint dp : dataSet.getDataPoints()) {
                            for(Field field : dp.getDataType().getFields()) {
                                History.stepsTakenToday+= dp.getValue(field).asInt();
                            }
                        }
                    }
                }

            }
        });
        return History.stepsTakenToday;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("","In history dont think the service is working");
        int type = intent.getIntExtra(SERVICE_REQUEST_TYPE,-1);
        if(type==TYPE_GET_STEP_TODAY_DATA){
            int steps = currentDay(new Date());
            Intent intent1 = new Intent(HISTORY_INTENT);
            // You can also include some extra data.
            intent1.putExtra(HISTORY_EXTRA_STEPS_TODAY, steps);

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);
        }
    }

    public static void setClient(GoogleApiClient client) {
        History.client = client;
    }
}
