package com.github.cresc28.speedrun;

import org.bukkit.Location;

public class PlayerState {
    private int startTime; //スタート時のtick
    private boolean isRunning; //スタートを踏んだ後か
    private boolean isOnEnd; //ゴール地点の上にいるか(ゴールの連発防止用)
    private String currentCourse; //現在走行中のコース
    private Location lastStartLocation; //最後に踏んだスタートの位置

    public PlayerState() {
        this.isRunning = false;
        this.isOnEnd = false;
        this.startTime = 0;
        this.currentCourse = null;
        this.lastStartLocation = null;
    }

    public int getStartTime() {
        return startTime;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isOnEnd() {
        return isOnEnd;
    }

    public String getCurrentCourse() {
        return currentCourse;
    }

    public Location getLastStartLocation() {
        return lastStartLocation;
    }

    //計測開始
    public boolean start(String courseName, int tick, Location currentLoc) {
        //直前に同じスタート地点を踏んでいなければ処理を開始。
        if (lastStartLocation == null || !lastStartLocation.equals(currentLoc)) {
            this.currentCourse = courseName;
            this.startTime = tick;
            this.isRunning = true;
            this.lastStartLocation = currentLoc;
            return true;
        }
        return false;
    }

    //計測終了
    public void stop() {
        this.isRunning = false;
    }

    public void setOnEnd(boolean onEnd) {
        this.isOnEnd = onEnd;
    }

    public void updateLastStartLocation(Location loc) {
        this.lastStartLocation = loc;
    }
}
