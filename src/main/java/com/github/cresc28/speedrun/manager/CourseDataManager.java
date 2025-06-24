package com.github.cresc28.speedrun.manager;

import com.github.cresc28.speedrun.data.CourseType;
import com.github.cresc28.speedrun.database.CourseDao;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.*;

/**
 * コースのロード・セーブ・登録・削除・取得を行うクラス。
 *
 */

public class CourseDataManager {
    private final CourseDao courseDao = new CourseDao();
    private final Map<Location, Map<CourseType, String>> courseMap;

    /**
     * コンストラクタにてデータベースからコースを読み込む。
     *
     */
    public CourseDataManager() {
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
        courseMap.computeIfAbsent(loc, k -> new EnumMap<>(CourseType.class)).put(type, courseName);
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
        Iterator<Map.Entry<Location, Map<CourseType, String>>> iterator = courseMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Location, Map<CourseType, String>> entry = iterator.next();
            Map<CourseType, String> typeMap = entry.getValue();
            String name = typeMap.get(type);

            if (courseName.equals(name)) {
                typeMap.remove(type);

                if (typeMap.isEmpty()) {
                    iterator.remove();
                }
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
        Iterator<Map.Entry<Location, Map<CourseType, String>>> iterator = courseMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Location, Map<CourseType, String>> entry = iterator.next();
            Map<CourseType, String> typeMap = entry.getValue();

            typeMap.entrySet().removeIf(typeEntry -> courseName.equals(typeEntry.getValue()));

            if (typeMap.isEmpty()) {
                iterator.remove();
            }
        }

        return courseDao.delete(courseName);
    }

    /**
     * 指定された座標、タイプのコース名を取得する。
     *
     * @param loc  座標
     * @param type       START,END,VIA_POINT
     * @return コース名、存在しない場合はnull
     */
    public String getCourseName(Location loc, CourseType type){
        Map<CourseType, String> typeMap = courseMap.get(loc);
        return typeMap == null ? null : typeMap.get(type);
    }

    /**
     * 指定されたタイプで登録されているコースの名リストを取得する。
     *
     * @param type       START,END,VIA_POINT
     * @return コース名リスト
     */
    public Set<String> getAllCourseName(CourseType type){
        Set<String> set = new HashSet<>();

        for (Map<CourseType, String> typeMap : courseMap.values()) {
            String name = typeMap.get(type);
            set.add(name);
        }

        return set;
    }

    /**
     * 指定されたタイプで登録されているコースの名リストを取得する。
     *
     * @return コース名リスト
     */
    public Set<String> getAllCourseName(){
        Set<String> set = new HashSet<>();

        for (Map<CourseType, String> typeMap : courseMap.values()) {
            set.addAll(typeMap.values());
        }

        return set;
    }
}
