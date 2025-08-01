package com.github.cresc28.speedrun.data;

import java.util.List;
import java.util.UUID;

/**
 * コース名、UUID、およびそのコースでのそのプレイヤーの記録リストを管理するクラス。
 */
public class RecordInfo {
    private final String courseName;
    private final UUID targetUuid;
    private final List<String> recordTimes;

    public RecordInfo(String courseName, UUID targetUuid, List<String> recordTimes){
        this.courseName = courseName;
        this.targetUuid = targetUuid;
        this.recordTimes = recordTimes;
    }

    public String getCourseName(){
        return courseName;
    }

    public UUID getTargetUuid(){
        return targetUuid;
    }

    public List<String> getRecordTimes(){
        return recordTimes;
    }
}
