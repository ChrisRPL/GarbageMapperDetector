package org.pytorch.demo.objectdetection.models;

import java.util.HashMap;
import java.util.Map;

public class Drone {
    private final String idDrone;
    private String name;
    private int sessionsCount;
    private int distanceTraveled;

    public Drone(String idDrone, String name, int sessionsCount, int distanceTraveled) {
        this.idDrone = idDrone;
        this.name = name;
        this.sessionsCount = sessionsCount;
        this.distanceTraveled = distanceTraveled;
    }

    public String getIdDrone() {
        return idDrone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSessionsCount() {
        return sessionsCount;
    }

    public void setSessionsCount(int sessionsCount) {
        this.sessionsCount = sessionsCount;
    }

    public int getDistanceTraveled() {
        return distanceTraveled;
    }

    public void setDistanceTraveled(int distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
    }

    public Map<String, Object> toMap() {
        Map<String,Object> drone = new HashMap<>();

        drone.put("id_drone", idDrone);
        drone.put("name", name);
        drone.put("sessions_count", sessionsCount);
        drone.put("distance_traveled", distanceTraveled);

        return drone;
    }
}
