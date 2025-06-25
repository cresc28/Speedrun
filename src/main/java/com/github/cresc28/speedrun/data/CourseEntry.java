package com.github.cresc28.speedrun.data;

/**
 * コースのタイプと名前を管理するクラス。
 */
public class CourseEntry {
    private final CourseType type;
    private final String courseName;

    public CourseEntry(CourseType type, String courseName) {
        this.type = type;
        this.courseName = courseName;
    }

    public CourseType getType() {
        return type;
    }

    public String getCourseName() {
        return courseName;
    }
}