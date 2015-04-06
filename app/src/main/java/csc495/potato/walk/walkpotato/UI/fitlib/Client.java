package csc495.potato.walk.walkpotato.UI.fitlib;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;

import csc495.potato.walk.walkpotato.UI.fitlib.common.Display;


public class Client {
    private static final int REQUEST_OAUTH = 1;

    private GoogleApiClient client;
    private boolean authInProgress = false;

    private Display display;

    public interface Connection {
        public void onConnected();
    }
    private Connection connection;

    public Client(final Activity activity, final Connection connection, final Display display) {
        this.display = display;
        this.connection = connection;
        client = new GoogleApiClient.Builder(activity)
                .addApi(Fitness.API)
                .addScope(Fitness.SCOPE_LOCATION_READ)
                .addScope(Fitness.SCOPE_ACTIVITY_READ)
                .addScope(Fitness.SCOPE_BODY_READ_WRITE)
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                display.show("Connected");
                                connection.onConnected();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                display.show("Connection suspended");
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    display.show("Connection lost. Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    display.show("Connection lost. Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(final ConnectionResult connectionResult) {
                                if (connectionResult.hasResolution()) {
                                    // This problem can be fixed. So let's try to fix it.
                                    try {
                                        // launch appropriate UI flow (which might, for example, be the
                                        // sign-in flow)
                                        connectionResult.startResolutionForResult(activity, REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException e) {
                                        // Try connecting again
                                        client.connect();
                                    }
                                } else {
                                    GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, 0).show();
                                }
                            }
                        }
                )
                .build();
    }

    public void connect() {
        client.connect();
    }

    public void disconnect() {
        if (client.isConnected()) {
            client.disconnect();
        }
    }

    //        disable should be called only for revoking authorization in GoogleFit
    public void revokeAuth() {
        PendingResult<Status> pendingResult = Fitness.ConfigApi.disableFit(client);
    }

    public GoogleApiClient getClient() {
        return client;
    }


    public boolean isConnected()
    {
        return client.isConnected();
    }
    public boolean isConnecting()
    {
        return client.isConnecting();
    }
}
