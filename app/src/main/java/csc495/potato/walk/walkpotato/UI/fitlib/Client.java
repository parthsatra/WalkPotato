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

/**
 * Created by Constantine Mars on 1/19/2015.
 *
 * Wrapper for GoogleApiClient that provides clear interface for Fit API operations
 */
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
                            public void onConnectionFailed(ConnectionResult result) {
                                display.log("Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), activity, 0).show();
                                    return;
                                }

                                if (!authInProgress) {
                                    try {
                                        display.show("Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(activity, REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException e) {
                                        display.show("Exception while starting resolution activity: " + e.getMessage());
                                    }
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            display.log("onActivityResult: REQUEST_OAUTH");
            authInProgress = false;
            if (resultCode == Activity.RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!client.isConnecting() && !client.isConnected()) {
                    display.log("onActivityResult: client.connect()");
                    client.connect();
                }
            }
        }
    }
}
