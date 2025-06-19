package com.github.cresc28.speedrun.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CourseDataManager {

    private static final String FILE_PATH = "speedrun_data.txt";

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
            e.printStackTrace();
        }
    }

    //スタート/ゴール地点データのセーブ
    public static void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
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
            e.printStackTrace();
        }
    }

    public static Map<Location, String> getStartMap() {
        return startMap;
    }

    public static Map<Location, String> getEndMap() {
        return endMap;
    }
}
