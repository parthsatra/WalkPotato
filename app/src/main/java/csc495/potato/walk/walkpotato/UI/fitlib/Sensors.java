package csc495.potato.walk.walkpotato.UI.fitlib;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import csc495.potato.walk.walkpotato.UI.fitlib.common.Display;

/**
 * Created by Constantine Mars on 1/19/2015.
 */
public class Sensors {
    private GoogleApiClient client;
    private Display display;
    private ArrayList<OnDataPointListener> listeners = new ArrayList<>();

    public ArrayList<String> getDatasources() {
        return datasources;
    }
    private ArrayList<String> datasources = new ArrayList<>();

    public interface DatasourcesListener {
        public void onDatasourcesListed();
    }
    private DatasourcesListener datasourcesListener;

    public Sensors(GoogleApiClient client, DatasourcesListener datasourcesListener, Display display) {
        this.client = client;
        this.display = display;
        this.datasourcesListener = datasourcesListener;
    }

    public void listDatasourcesAndSubscribe() {
        Fitness.SensorsApi.findDataSources(client, new DataSourcesRequest.Builder()
                .setDataTypes(
                        DataType.TYPE_LOCATION_SAMPLE,
                        DataType.TYPE_STEP_COUNT_DELTA,
                        DataType.TYPE_DISTANCE_DELTA,
                        DataType.TYPE_HEART_RATE_BPM )
                .setDataSourceTypes(DataSource.TYPE_RAW, DataSource.TYPE_DERIVED)
                .build())
                .setResultCallback(new ResultCallback<DataSourcesResult>() {
                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {

                        datasources.clear();
                        listeners.clear();
                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                            Device device = dataSource.getDevice();
                            String fields = dataSource.getDataType().getFields().toString();
                            datasources.add(device.getManufacturer() + " " + device.getModel() + " [" + dataSource.getDataType().getName() + " " + fields + "]");

                            final DataType dataType = dataSource.getDataType();
                            if (    dataType.equals(DataType.TYPE_LOCATION_SAMPLE) ||
                                    dataType.equals(DataType.TYPE_STEP_COUNT_DELTA) ||
                                    dataType.equals(DataType.TYPE_DISTANCE_DELTA) ||
                                    dataType.equals(DataType.TYPE_HEART_RATE_BPM)) {

                                final OnDataPointListener listener = new OnDataPointListener() {
                                    @Override
                                    public void onDataPoint(DataPoint dataPoint) {
                                        String msg = "onDataPoint: ";
                                        for (Field field : dataPoint.getDataType().getFields()) {
                                            Value value = dataPoint.getValue(field);
                                            msg += field + "=" + value + ", ";
                                        }
                                        display.show(msg);
                                    }
                                };

                                Fitness.SensorsApi.add(client,
                                        new SensorRequest.Builder()
                                                .setDataSource(dataSource)
                                                .setDataType(dataType)
                                                .setSamplingRate(10, TimeUnit.SECONDS)
                                                .build(),
                                        listener)
                                        .setResultCallback(new ResultCallback<Status>() {
                                            @Override
                                            public void onResult(Status status) {
                                                if (status.isSuccess()) {
                                                    listeners.add(listener);
                                                    display.show("Listener for " + dataType.getName() + " registered");
                                                } else {
                                                    display.show("Failed to register listener for " + dataType.getName());
                                                }
                                            }
                                        });
                            }
                        }
                        datasourcesListener.onDatasourcesListed();
                    }
                });

    }

    public void subscribeToHeartRate() {
        Fitness.SensorsApi.findDataSources(client, new DataSourcesRequest.Builder()
                .setDataTypes( DataType.TYPE_HEART_RATE_BPM )
                .setDataSourceTypes(DataSource.TYPE_RAW, DataSource.TYPE_DERIVED)
                .build())
                .setResultCallback(new ResultCallback<DataSourcesResult>() {
                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {

                        datasources.clear();
                        listeners.clear();
                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                            Device device = dataSource.getDevice();
                            String fields = dataSource.getDataType().getFields().toString();
                            datasources.add(device.getManufacturer() + " " + device.getModel() + " [" + dataSource.getDataType().getName() + " " + fields + "]");

                            final DataType dataType = dataSource.getDataType();
                            if (dataType.equals(DataType.TYPE_HEART_RATE_BPM)) {

                                final OnDataPointListener listener = new OnDataPointListener() {
                                    @Override
                                    public void onDataPoint(DataPoint dataPoint) {
                                        String msg = "onDataPoint: ";
                                        for (Field field : dataPoint.getDataType().getFields()) {
                                            Value value = dataPoint.getValue(field);
                                            msg += field + "=" + value + ", ";
                                        }
                                        display.show(msg);
                                    }
                                };

                                Fitness.SensorsApi.add(client,
                                        new SensorRequest.Builder()
                                                .setDataSource(dataSource)
                                                .setDataType(dataType)
                                                .setSamplingRate(30, TimeUnit.SECONDS)
                                                .build(),
                                        listener)
                                        .setResultCallback(new ResultCallback<Status>() {
                                            @Override
                                            public void onResult(Status status) {
                                                if (status.isSuccess()) {
                                                    listeners.add(listener);
                                                    display.show("Listener for " + dataType.getName() + " registered");
                                                } else {
                                                    display.show("Failed to register listener for " + dataType.getName());
                                                }
                                            }
                                        });
                            }
                        }
                        datasourcesListener.onDatasourcesListed();
                    }
                });
    }

    public void unsubscribe() {
        for (OnDataPointListener listener : listeners) {
            Fitness.SensorsApi.remove(
                    client,
                    listener)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                display.show("Listener was removed!");
                            } else {
                                display.show("Listener was not removed.");
                            }
                        }
                    });
        }
        listeners.clear();
    }
}
