package com.github.cresc28.speedrun.manager;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * チェックポイントのロード・セーブ・登録・削除・取得を行うクラス。
 * プレイヤーごと、ワールドごとにチェックポイントを管理し、
 * 現在位置や最後に踏んだチェックポイントの情報も保持する。
 */
public class CheckpointManager {
    private static final Map<UUID, Map<World, Map<String, Location>>> checkpoints = new HashMap<>(); //uuid->ワールド->CP名->座標と辿れる構成
    private static final Map<UUID, Location> globalCurrentCp = new HashMap<>(); //最後に設定されたCP
    private static final Map<UUID, Map<World, Location>> localCurrentCp = new HashMap<>(); //現在のワールドで最後に設定されたCP
    private static boolean isCrossWorldTpAllowed; //異なるワールドへのTPを許可するか

    /**
     * 一時的なチェックポイントを登録。
     *
     * @param player プレイヤー
     */
    public static void registerCheckpoint(Player player){
        registerCheckpoint(player, "tmp");
    }

    /**
     * プレイヤーの現在位置を指定された名前でチェックポイントとして登録する。
     *
     * @param player プレイヤー
     * @param cpName チェックポイント名
     */
    public static void registerCheckpoint(Player player, String cpName){
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation();
        World world = loc.getWorld();
        Map<String, Location> map = checkpoints.computeIfAbsent(uuid, k -> new HashMap<>()).computeIfAbsent(world, w -> new HashMap<>());

        map.put(cpName, loc);
        globalCurrentCp.put(uuid, loc);
        localCurrentCp.computeIfAbsent(uuid, k -> new HashMap<>()).put(world, loc);
        player.sendMessage("CPを設定しました。");
    }

    /**
     * 指定された名前のチェックポイントを削除する。
     *
     * @param player プレイヤー
     * @param cpName 削除したいチェックポイント名
     * @return 削除に成功したか
     */
    public static boolean removeCheckpoint(Player player, String cpName){
        UUID uuid = player.getUniqueId();
        World world = player.getWorld();
        Map<World, Map<String, Location>> worldMap = checkpoints.get(uuid);
        Map<World, Location> localCpMap = localCurrentCp.get(uuid);
        if(worldMap == null) return false;

        Map<String, Location> cpMap = worldMap.get(world);
        if(cpMap == null) return false;

        if (cpMap.containsKey(cpName)) {
            if(cpMap.get(cpName).equals(globalCurrentCp.get(uuid))) globalCurrentCp.put(uuid, null); //削除したCPがcurrentCpならcurrentCpにnullを代入
            if(localCpMap != null && cpMap.get(cpName).equals(localCpMap.get(world))) localCpMap.put(world, null);
            cpMap.remove(cpName);
            return true;
        }
        return false;
    }

    /**
     * 指定された名前のチェックポイントを今のチェックポイントに設定し、その位置を返す。
     *
     * @param player プレイヤー
     * @param cpName チェックポイント名
     * @return 選択に成功したか
     */
    public static boolean selectCheckpoint(Player player, String cpName){
        UUID uuid = player.getUniqueId();
        World world = player.getWorld();
        Map<World, Map<String, Location>> worldMap = checkpoints.get(uuid);
        if(worldMap == null) return false;

        Map<String, Location> cpMap = worldMap.get(world);
        if (cpMap == null) return false;

        if (cpMap.containsKey(cpName)) {
            globalCurrentCp.put(uuid, cpMap.get(cpName));
            localCurrentCp.computeIfAbsent(uuid, k -> new HashMap<>()).put(world, cpMap.get(cpName));
            return true;
        }
        return false;
    }

    /**
     * 現在のワールドにおいてプレイヤーが登録しているチェックポイント名の一覧を返す。
     *
     * @param player プレイヤー
     * @return チェックポイント名の一覧
     */
    public static Collection<String> getCheckpointNames(Player player){
        UUID uuid = player.getUniqueId();
        World world = player.getWorld();

        Map<World, Map<String, Location>> worldMap = checkpoints.get(uuid);
        if (worldMap == null) return Collections.emptyList();

        Map<String, Location> cpMap = worldMap.get(world);
        if (cpMap == null) return Collections.emptyList();

        return new ArrayList<>(cpMap.keySet());
    }

    public static void setCrossWorldTpAllowed(boolean allowed){
        isCrossWorldTpAllowed = allowed;
    }

    public static boolean isCrossWorldTpAllowed(){
        return isCrossWorldTpAllowed;
    }

    public static Location getCurrentGlobalCpLocation(UUID uuid){
        return globalCurrentCp.get(uuid);
    }

    public static Location getCurrentLocalCpLocation(Player player){
        UUID uuid = player.getUniqueId();
        World world = player.getWorld();
        Map<World, Location> worldMap = localCurrentCp.get(uuid);
        if (worldMap == null) return null;

        return worldMap.get(world);
    }
}
