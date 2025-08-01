package com.github.cresc28.speedrun.data;

/**
 * 地点タイプの列挙クラス。
 */

public enum PointType {
    START(1),
    VIA_POINT(2),
    END(3);

    private final int id;

    PointType(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static PointType fromId(int id) {
        for (PointType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid CourseType id: " + id);
    }

    public static PointType fromString(String s) {
        if (s == null) return null;

        switch (s.toLowerCase()) {
            case "start": return START;
            case "via_point": return VIA_POINT;
            case "end": return END;
            default: return null;
        }
    }
}
