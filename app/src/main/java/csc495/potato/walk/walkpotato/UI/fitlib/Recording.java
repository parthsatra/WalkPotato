package csc495.potato.walk.walkpotato.UI.fitlib;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;

import csc495.potato.walk.walkpotato.UI.fitlib.common.Display;

/**
 * Created by mars on 1/19/15.
 */
public class Recording {
    private GoogleApiClient client;
    private Display display;

    public Recording(GoogleApiClient googleApiClient, Display display) {
        this.client = googleApiClient;
        this.display = display;
    }

    public void subscribe() {
        subscribe(DataType.TYPE_STEP_COUNT_DELTA);
    }

    public void subscribe(DataType dataType) {
        Fitness.RecordingApi.subscribe(client, dataType)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode() == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                display.show("Existing subscription for activity detected.");
                            } else {
                                display.show("Successfully subscribed!");
                            }
                        } else {
                            display.show("There was a problem subscribing.");
                        }
                    }
                });
    }

    public void listSubscriptions() {
        Fitness.RecordingApi.listSubscriptions(client, DataType.TYPE_STEP_COUNT_DELTA).setResultCallback(new ResultCallback<ListSubscriptionsResult>() {
                    @Override
                    public void onResult(ListSubscriptionsResult listSubscriptionsResult) {
                        for (Subscription sc : listSubscriptionsResult.getSubscriptions()) {
                            DataType dt = sc.getDataType();
                            display.show("found subscription for data type: " + dt.getName());
                        }
                    }
                });
    }

    public void unsubscribe() {
        Fitness.RecordingApi.unsubscribe(client, DataType.TYPE_STEP_COUNT_DELTA).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            display.show("Successfully unsubscribed for data type: " + DataType.TYPE_STEP_COUNT_DELTA.toString());
                        } else {
                            display.show("Failed to unsubscribe for data type: " + DataType.TYPE_STEP_COUNT_DELTA.toString());
                        }
                    }
                });
    }
}
