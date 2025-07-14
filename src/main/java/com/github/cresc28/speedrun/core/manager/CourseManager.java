package com.github.cresc28.speedrun.core.manager;

import com.github.cresc28.speedrun.data.CourseEntry;
import com.github.cresc28.speedrun.data.CourseType;
import com.github.cresc28.speedrun.db.course.CourseDao;
import org.bukkit.Location;

import java.util.*;

/**
 * コースのロード・セーブ・登録・削除・取得を行うクラス。
 *
 */

public class CourseManager {
    private final CourseDao courseDao;
    private final Map<Location, CourseEntry> courseMap;
    /**
     * コンストラクタにてデータベースからコースを読み込む。
     *
     */
    public CourseManager(CourseDao courseDao) {
        this.courseDao = courseDao;
        courseMap = courseDao.getCourses();
    }

    /**
     * 指定した座標にコース名を登録する。既に同じ座標に同じタイプで登録があれば上書きされる。
     *
     * @param type       START,END,VIA_POINT
     * @param courseName コース名
     * @param loc        登録する座標
     */
    public void registerCourse(CourseType type, String courseName, Location loc){
        courseMap.put(loc, new CourseEntry(type, courseName));

        courseDao.delete(loc); //既に登録があれば削除。(重複対策)
        courseDao.insert(type, courseName, loc);
    }

    /**
     * 指定されたコース名、コースタイプの登録を削除する。
     *
     * @param courseName 削除対象のコース名
     * @param type       START,END,VIA_POINT
     * @return 削除に成功したか
     */
    public boolean removeCourse(String courseName, CourseType type) {
        Iterator<Map.Entry<Location, CourseEntry>> iterator = courseMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Location, CourseEntry> entry = iterator.next();
            CourseEntry courseEntry = entry.getValue();

            // タイプと名前の両方が一致する場合に削除
            if (courseEntry.getType() == type && courseName.equals(courseEntry.getCourseName())) {
                iterator.remove();
            }
        }

        return courseDao.delete(type, courseName);
    }

    /**
     * 指定されたコース名の登録を削除する。
     *
     * @param courseName 削除対象のコース名
     * @return 削除に成功したか
     */
    public boolean removeCourse(String courseName) {
        Iterator<Map.Entry<Location, CourseEntry>> iterator = courseMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Location, CourseEntry> entry = iterator.next();
            CourseEntry courseEntry = entry.getValue();

            if (courseName.equals(courseEntry.getCourseName())) {
                iterator.remove();
            }
        }

        return courseDao.delete(courseName);
    }


    /**
     * 指定されたタイプで登録されているコースの名リストを取得する。
     *
     * @param type       START,END,VIA_POINT
     * @return コース名リスト
     */
    public Set<String> getAllCourseName(CourseType type){
        Set<String> set = new HashSet<>();
        if(type == null) return set;

        for (CourseEntry entry : courseMap.values()) {
            if (entry.getType() == type) {
                set.add(entry.getCourseName());
            }
        }

        return set;
    }

    /**
     * 指定されたタイプ、名前の登録位置を一つ返す。
     *
     * @param type       START,END,VIA_POINT
     * @return 位置
     */
    public Location getLocation(CourseType type, String courseName){
        for (Map.Entry<Location, CourseEntry> entry : courseMap.entrySet()) {
            CourseEntry courseEntry = entry.getValue();
            if (courseEntry.getType() == type && courseEntry.getCourseName().equals(courseName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 指定されたタイプで登録されているコースの名リストを取得する。
     *
     * @return コース名リスト
     */
    public Set<String> getAllCourseName(){
        Set<String> set = new HashSet<>();

        for (CourseEntry entry : courseMap.values()) {
            set.add(entry.getCourseName());
        }

        return set;
    }

    public CourseEntry getCourseEntry(Location loc){
        return courseMap.get(loc);
    }
}
