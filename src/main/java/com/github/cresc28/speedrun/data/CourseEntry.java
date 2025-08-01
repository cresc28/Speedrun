package com.github.cresc28.speedrun.data;

/**
 * 地点タイプとコース名を管理するクラス。
 */
public class CourseEntry {
    private final PointType type;
    private final String courseName;

    public CourseEntry(PointType type, String courseName) {
        this.type = type;
        this.courseName = courseName;
    }

    public PointType getType() {
        return type;
    }

    public String getCourseName() {
        return courseName;
    }
}