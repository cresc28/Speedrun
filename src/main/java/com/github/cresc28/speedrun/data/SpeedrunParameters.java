package com.github.cresc28.speedrun.data;

import com.github.cresc28.speedrun.db.course.RecordDao;
import com.github.cresc28.speedrun.manager.CheckpointManager;
import com.github.cresc28.speedrun.manager.CourseManager;

/**
 * ParametersObject
 */
public class SpeedrunParameters {
    private final CourseManager courseManager;
    private final CheckpointManager cpManager;
    private final RecordDao recordDao;
    private final RecordSession recordSession;

    public SpeedrunParameters(CourseManager courseManager, CheckpointManager cpManager, RecordDao recordDao){
        this.courseManager = courseManager;
        this.cpManager = cpManager;
        this.recordDao = recordDao;
        this.recordSession = new RecordSession();
    }

    public CourseManager getCourseManager(){
        return courseManager;
    }

    public CheckpointManager getCpManager(){
        return cpManager;
    }

    public RecordDao getRecordDao(){
        return recordDao;
    }

    public RecordSession getRecordSession(){
        return recordSession;
    }
}
