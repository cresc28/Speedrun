package com.github.cresc28.speedrun.manager;

import org.bukkit.Location;

import java.util.*;

public class CheckpointManager {
    private static final Map<UUID, Map<String, Location>> checkpoints = new HashMap<>();
    private static final Map<UUID, Location> currentCp = new HashMap<>(); //最後に設定したCP

    //名前を自動で設定しCPを設定する。
    public static void registerCheckpoint(UUID uuid, Location loc){
        registerCheckpoint(uuid, loc, generateName());
    }

    //指定された名前のCPを設定する。
    public static void registerCheckpoint(UUID uuid, Location loc, String cpName){
        Map<String, Location> map = checkpoints.computeIfAbsent(uuid, k -> new LinkedHashMap<>());
        map.put(cpName, loc);
        currentCp.put(uuid, loc);
    }

    //指定された名前のCPを削除する。
    public static boolean removeCheckpoint(UUID uuid, String cpName){
        Map<String, Location> map = checkpoints.get(uuid);
        if (map != null && map.containsKey(cpName)) {
            if(map.get(cpName) == currentCp.get(uuid)) currentCp.put(uuid, null); //削除したCPがcurrentCpならcurrentCpにnullを代入
            map.remove(cpName);
            return true;
        }
        return false;
    }

    //cpNameを持つCPをcurrentCpとし、そのLocationを返す
    public static Location selectCp(UUID uuid, String cpName){
        Map<String, Location> map = checkpoints.get(uuid);
        if (map != null && map.containsKey(cpName)) {
            currentCp.put(uuid, map.get(cpName));
            return map.get(cpName);
        }
        return null;
    }

    //あるuuidのプレイヤーが所持するcpの名前をリストとして返す。
    public static Collection<String> getCheckpointNames(UUID uuid){
        Map<String, Location> map = checkpoints.get(uuid);
        if (map == null) return Collections.emptyList();
        return new ArrayList<>(map.keySet());
    }

    //最後に設定したCPを返す。
    public static Location getCurrentCpLocation(UUID uuid){
        return currentCp.get(uuid);
    }

    //自動的に名前を割り当てる
    private static String generateName(){
        return "test";
    }
}
