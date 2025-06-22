package com.github.cresc28.speedrun.manager;

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
 */

public class CourseDataManager {
    private static final Map<Location, String> startMap = new HashMap<>();
    private static final Map<Location, String> endMap = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger("CourseDataManager");
    private static final File FILE = new File("plugins/Speedrun/course.yml");
    private static final File TMP_FILE = new File("plugins/Speedrun/course.yml.tmp");
    private static final FileConfiguration config = YamlConfiguration.loadConfiguration(FILE);

    /**
     * Yamlファイルからスタート地点・ゴール地点の情報をロードする。
     */
    public static void load() {
        startMap.clear();
        endMap.clear();

        loadToMap(startMap,"start");
        loadToMap(endMap,"end");
    }

    private static void loadToMap(Map<Location, String> map, String type) {
        if (!config.isConfigurationSection(type)) return;

        for (String key : config.getConfigurationSection(type).getKeys(false)) {
            String[] parts = key.split(",");
            if (parts.length != 4) continue;

            String worldName = parts[0];
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);

            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            Location loc = new Location(world, x, y, z);
            String courseName = config.getString(type + "." + key);
            map.put(loc, courseName);
        }
    }

    /**
     * スタート地点・ゴール地点の情報をYamlファイルに保存する。
     */
    public static void save() {
        File parentDir = FILE.getParentFile();
        FileConfiguration tempConfig = new YamlConfiguration();

        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                LOGGER.log(Level.SEVERE, "Speedrunディレクトリの作成に失敗しました。");
                return;
            }
        }

        saveMap(tempConfig, startMap,"start");
        saveMap(tempConfig, endMap,"end");

        try {
            tempConfig.save(TMP_FILE);
            Files.move(TMP_FILE.toPath(), FILE.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); //一時ファイルから本ファイルにコピー
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "本ファイルへのコピーに失敗しました。");
        }
    }

    private static void saveMap(FileConfiguration config, Map<Location, String> map, String type) {
        for (Map.Entry<Location, String> entry : map.entrySet()) {
            Location loc = entry.getKey();
            String key = String.format("%s,%d,%d,%d", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            config.set(type + "." + key, entry.getValue());
        }
    }

    /**
     * 指定した座標にコース名を登録する。既に同じ座標に登録があれば上書きされる。
     *
     * @param loc        登録する座標
     * @param courseName コース名
     * @param type       "start" または "end"
     */
    public static void registerCourse(Location loc, String courseName, String type){
        //重複が起きないよう現在座標をキーとする値を一旦削除
        startMap.remove(loc);
        endMap.remove(loc);

        if (type.equalsIgnoreCase("start")) {
            CourseDataManager.getStartMap().put(loc, courseName);
        }
        else if (type.equalsIgnoreCase("end")) {
            CourseDataManager.getEndMap().put(loc, courseName);
        }

        save();
    }

    /**
     * 指定されたコース名の登録を削除する。
     *
     * @param courseName 削除対象のコース名
     * @param type       "start" または "end"
     * @return 削除に成功したか
     */
    public static boolean removeCourse(String courseName, String type) {
        Map<Location, String> map;

        if (type.equalsIgnoreCase("start")) {
            map = startMap;
        }
        else if (type.equalsIgnoreCase("end")) {
            map = endMap;
        }
        else return false;

        int size = map.size();
        map.keySet().removeIf(key -> courseName.equals(map.get(key))); //指定された名前のコースを削除。
        if(size == map.size()) return false; //mapのsizeが変わっていないなら削除されていない。

        save();
        return true;
    }

    /**
     * 指定された座標のコース名を取得する。
     *
     * @param loc  座標
     * @param type "start" または "end"
     * @return コース名、存在しない場合はnull
     */
    public static String getCourseName(Location loc, String type){
        if (type.equalsIgnoreCase("start")) {
            return startMap.get(loc);
        }
        else if (type.equalsIgnoreCase("end")) {
            return endMap.get(loc);
        }
        else return null;
    }

    public static Map<Location,String> getStartMap(){
        return startMap;
    }

    public static Map<Location,String> getEndMap(){
        return endMap;
    }
}
