package com.github.cresc28.speedrun.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 記録の共有用クラス。
 */
public class RecordSession {
    private final Map<UUID, RecordInfo> sessionMap = new HashMap<>();

    public void setRecord(UUID uuid, RecordInfo recordInfo) {
        sessionMap.put(uuid, recordInfo);
    }

    public RecordInfo getRecord(UUID requesterUuid) {
        return sessionMap.get(requesterUuid);
    }

    public void clear(UUID requesterUuid) {
        sessionMap.remove(requesterUuid);
    }
}
