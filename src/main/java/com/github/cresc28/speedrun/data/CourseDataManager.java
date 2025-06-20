package com.github.cresc28.speedrun.data;

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

public class CourseDataManager {
    private static final Map<Location, String> startMap = new HashMap<>();
    private static final Map<Location, String> endMap = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger("Speedrun");
    private static final File FILE = new File("plugins/Speedrun/course.yml");
    private static final File TMP_FILE = new File("plugins/Speedrun/course.yml.tmp");
    private static final FileConfiguration config = YamlConfiguration.loadConfiguration(FILE);

    //スタート/ゴール地点データのロード
    public static void load() {
        startMap.clear();
        endMap.clear();

        loadPointMap(startMap,"start");
        loadPointMap(endMap,"end");
    }

    private static void loadPointMap(Map<Location, String> map, String type) {
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

    //スタート/ゴール地点のロード
    public static void save() {
        File parentDir = FILE.getParentFile();
        FileConfiguration tempConfig = new YamlConfiguration();

        //ディレクトリが存在しないなら作成
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                LOGGER.log(Level.SEVERE, "speedrunディレクトリの作成に失敗しました。");
                return;
            }
        }

        savePointMap(tempConfig, startMap,"start");
        savePointMap(tempConfig, endMap,"end");

        try {
            // 一時ファイルに保存
            tempConfig.save(TMP_FILE);
            //一時ファイルから本ファイルにコピー
            Files.move(TMP_FILE.toPath(), FILE.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "本ファイルへのコピーに失敗しました。");
        }
    }

    private static void savePointMap(FileConfiguration config, Map<Location, String> map, String type) {
        for (Map.Entry<Location, String> entry : map.entrySet()) {
            Location loc = entry.getKey();
            String key = String.format("%s,%d,%d,%d", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            config.set(type + "." + key, entry.getValue());
        }
    }

    //コースの登録
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

    //登録されているコースの削除。typeはSTARTまたはENDを指定する。
    public static boolean removeCourse(String CourseName, String type) {
        Map<Location, String> map;

        if (type.equalsIgnoreCase("start")) {
            map = startMap;
        }
        else if (type.equalsIgnoreCase("end")) {
            map = endMap;
        }
        else return false;

        List<Location> keysToRemove = new ArrayList<>();
        for (Map.Entry<Location, String> entry : map.entrySet()) {
            if (entry.getValue().equals(CourseName)) {
                keysToRemove.add(entry.getKey());
            }
        }

        if(keysToRemove.isEmpty()) return false;
        keysToRemove.forEach(map::remove);
        save();
        return true;
    }

    //locをキーとする値を返す。typeはSTARTまたはENDを指定する。
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
