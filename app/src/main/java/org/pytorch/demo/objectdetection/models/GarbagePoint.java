package org.pytorch.demo.objectdetection.models;

import com.google.firebase.firestore.GeoPoint;
import com.google.type.DateTime;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GarbagePoint {
    private final String idGarbagePoint;
    private final String idDrone;
    private String imageUrl;
    private GeoPoint coords;
    private Date time;

    public GarbagePoint(String idGarbagePoint, String idDrone, String imageUrl, GeoPoint coords, Date time) {
        this.idGarbagePoint = idGarbagePoint;
        this.idDrone = idDrone;
        this.imageUrl = imageUrl;
        this.coords = coords;
        this.time = time;
    }

    public String getIdGarbagePoint() {
        return idGarbagePoint;
    }

    public String getIdDrone() {
        return idDrone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public GeoPoint getCoords() {
        return coords;
    }

    public void setCoords(GeoPoint coords) {
        this.coords = coords;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Map<String, Object> toMap() {
        Map<String,Object> garbagePoint = new HashMap<>();

        garbagePoint.put("id_garbage_point", idGarbagePoint);
        garbagePoint.put("id_drone", idDrone);
        garbagePoint.put("image_url", imageUrl);
        garbagePoint.put("coords", coords);
        garbagePoint.put("time", time);

        return garbagePoint;
    }
}
