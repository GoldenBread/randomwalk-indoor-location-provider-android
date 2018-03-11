package io.indoorlocation.manual;

import android.app.Activity;
import android.location.Location;
import android.util.Log;

import com.mapbox.services.commons.models.Position;

import io.indoorlocation.core.IndoorLocation;
import io.indoorlocation.core.IndoorLocationProvider;

import static com.mapbox.services.api.utils.turf.TurfMeasurement.destination;

public class RandomWalkIndoorLocationProvider extends IndoorLocationProvider {

    private long TIME_BW_UPDATES = 1000;
    private double DISTANCE_BW_UPDATES = 0.001;

    private double EURATECH_LATITUDE = 50.6330833;
    private double EURATECH_LONGITUDE = 3.0203175999999985;

    private volatile boolean isStarted = false;

    private IndoorLocation rIndoorLocation;

    private Position lastPosition;

    private Activity    mapActivity;


    public RandomWalkIndoorLocationProvider(Activity mapActivity) {//retrieve the layout activity to run the dispatcher on the same thread
        super();

        rIndoorLocation = new IndoorLocation("Manual", EURATECH_LATITUDE, EURATECH_LONGITUDE, Double.NaN, System.currentTimeMillis());

        lastPosition = Position.fromCoordinates(EURATECH_LONGITUDE, EURATECH_LATITUDE);

        this.mapActivity = mapActivity;
    }

    private void setIndoorLocation(double latitude, double longitude) {
        Location loc = new Location("Manual");
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);

        rIndoorLocation = new IndoorLocation(loc, null);

        mapActivity.runOnUiThread(new Runnable() {// avoid deadlock and other conflicts
            public void run()
            {
                dispatchIndoorLocationChange(rIndoorLocation);
            }
        });
    }

    private void refreshRandom() {
        try {
            lastPosition = destination(lastPosition, DISTANCE_BW_UPDATES, Math.random() * 360.0 - 180.0, "kilometers");
            setIndoorLocation(lastPosition.getLatitude(), lastPosition.getLongitude());

        } catch (Exception e) {
            Log.d("RLocProvider", e.getMessage());

        }
    }

    @Override
    public boolean supportsFloor() {
        return false;
    }

    @Override
    public void start() {
        if (isStarted)
            return;

        isStarted = true;

        new Thread(new Runnable() {
            public void run() {
                while (isStarted && !Thread.currentThread().isInterrupted()) {
                    refreshRandom();
                    try {
                        Thread.sleep(TIME_BW_UPDATES);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void stop() {
        if (isStarted) {
            this.isStarted = false;

        }
    }

    @Override
    public boolean isStarted() {
        return this.isStarted;
    }
}
