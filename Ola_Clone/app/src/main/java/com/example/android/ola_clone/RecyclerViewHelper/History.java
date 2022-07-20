package com.example.android.ola_clone.RecyclerViewHelper;

public class History {
    private long time;
    private String rideId;


    public History(long time, String rideId)
    {
        this.time = time;
        this.rideId = rideId;
    }
    public History(){}

    public String getRideId() {
        return rideId;
    }
    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
