package com.github.cresc28.speedrun.data;

/**
 * コースのタイプの列挙クラス。
 */

public enum CourseType {
    START(1),
    VIA_POINT(2),
    END(3);

    private final int id;

    CourseType(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CourseType fromId(int id) {
        for (CourseType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid CourseType id: " + id);
    }

    public static CourseType fromString(String s) {
        if (s == null) throw new IllegalArgumentException();

        switch (s.toLowerCase()) {
            case "start": return START;
            case "via_point": return VIA_POINT;
            case "end": return END;
            default:
                throw new IllegalArgumentException("StringをCourseTypeに変換できません。入力を確認してください。: " + s);
        }
    }
}
