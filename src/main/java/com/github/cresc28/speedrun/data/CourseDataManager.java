package com.github.cresc28.speedrun.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.*;

public class CourseDataManager {

    private static final String FILE_PATH = "speedrun_data.txt";
    private static final String TEMP_FILE_PATH = FILE_PATH + ".tmp";
    private static final Logger LOGGER = Logger.getLogger(CourseDataManager.class.getName());
    private static final Map<Location, String> startMap = new HashMap<>();
    private static final Map<Location, String> endMap = new HashMap<>();

    //スタート/ゴール地点データのロード
    public static void load() {
        startMap.clear();
        endMap.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String type = parts[0];
                    String worldName = parts[1];
                    int x = Integer.parseInt(parts[2]);
                    int y = Integer.parseInt(parts[3]);
                    int z = Integer.parseInt(parts[4]);
                    String name = parts[5];

                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        continue;
                    }

                    Location loc = new Location(world, x, y, z);

                    if (type.equals("start")) {
                        startMap.put(loc, name);
                    } else if (type.equals("end")) {
                        endMap.put(loc, name);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "コースファイルの読み込みに失敗しました", e);
        }
    }

    //スタート/ゴール地点データのセーブ
    public static void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEMP_FILE_PATH))) {
            for (Map.Entry<Location, String> entry : startMap.entrySet()) {
                Location loc = entry.getKey();
                writer.write(String.format("start,%s,%d,%d,%d,%s\n",
                        loc.getWorld().getName(),
                        loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
                        entry.getValue()));
            }
            for (Map.Entry<Location, String> entry : endMap.entrySet()) {
                Location loc = entry.getKey();
                writer.write(String.format("end,%s,%d,%d,%d,%s\n",
                        loc.getWorld().getName(),
                        loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
                        entry.getValue()));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "一時ファイルへの書き込みに失敗しました", e);
            return;
        }

        try {
            Files.move(Paths.get(TEMP_FILE_PATH), Paths.get(FILE_PATH), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "コースファイルの保存に失敗しました", e);
        }
    }

    //コースの登録
    public static void registarCourse(Location loc, String courseName, String type){
        //重複が起きないよう現在座標をキーとする値を一旦削除
        CourseDataManager.getStartMap().remove(loc);
        CourseDataManager.getEndMap().remove(loc);

        if (type.equalsIgnoreCase("start")) {
            CourseDataManager.getStartMap().put(loc, courseName);
        }
        else if (type.equalsIgnoreCase("end")) {
            CourseDataManager.getEndMap().put(loc, courseName);
        }

        save();
    }

    //登録されているコースの削除
    public static boolean removeCourse(Map<Location, String> map, String CourseName) {
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

    public static Map<Location, String> getStartMap() {
        return startMap;
    }

    public static Map<Location, String> getEndMap() {
        return endMap;
    }
}
