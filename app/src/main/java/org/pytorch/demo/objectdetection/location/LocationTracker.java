package org.pytorch.demo.objectdetection.location;

import android.location.Location;

public interface LocationTracker {
    interface LocationUpdateListener{
        void onUpdate(Location oldLoc, long oldTime, Location newLoc, long newTime);
    }

    void start();
    void start(LocationUpdateListener update);

    void stop();

    boolean hasLocation();

    boolean hasPossiblyStaleLocation();

    Location getLocation();

    Location getPossiblyStaleLocation();
}
