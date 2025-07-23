package com.github.cresc28.speedrun.data;

import java.util.List;
import java.util.UUID;

public class RecordInfo {
    private final String courseName;
    private final UUID targetUuid;
    private final List<String> recordTime;

    public RecordInfo(String courseName, UUID targetUuid, List<String> recordTime){
        this.courseName = courseName;
        this.targetUuid = targetUuid;
        this.recordTime = recordTime;
    }

    public String getCourseName(){
        return courseName;
    }

    public UUID getTargetUuid(){
        return targetUuid;
    }

    public List<String> getRecordTimes(){
        return recordTime;
    }
}
