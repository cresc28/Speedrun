package com.github.cresc28.speedrun.manager;

import com.github.cresc28.speedrun.database.CheckpointDAO;
import com.github.cresc28.speedrun.database.RecentCheckpointDAO;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

/**
 * チェックポイントのロード・セーブ・登録・削除・取得を行うクラス。
 * プレイヤーごと、ワールドごとにチェックポイントを管理し、
 * 現在位置や最後に踏んだチェックポイントの情報も保持する。
 */
public class CheckpointManager {
    private static final Map<UUID, Location> globalRecentCp = new HashMap<>(); //最後に設定されたCP
    private static final Map<UUID, Location> localRecentCp = new HashMap<>(); //現在のワールドで最後に設定されたCP
    private static boolean isCrossWorldTpAllowed; //異なるワールドへのTPを許可するか
    private static final CheckpointDAO cpDao = new CheckpointDAO();
    private static final RecentCheckpointDAO recentCpDao = new RecentCheckpointDAO();

    /**
     * 一時的なチェックポイントを登録。
     *
     * @param uuid UUID
     * @param loc プレイヤーのいる座標
     */
    public static void registerCheckpoint(UUID uuid, Location loc){
        registerCheckpoint(uuid, loc, "tmp");
    }

    /**
     * プレイヤーの現在位置を指定された名前でチェックポイントとして登録する。
     *
     * @param uuid UUID
     * @param loc プレイヤーのいる座標
     * @param cpName チェックポイント名
     */
    public static void registerCheckpoint(UUID uuid, Location loc, String cpName){
        World world = loc.getWorld();
        cpDao.insert(uuid, world, cpName, loc);

        globalRecentCp.put(uuid, loc);
        localRecentCp.put(uuid, loc);
    }

    /**
     * 指定された名前のチェックポイントを削除する。
     * そのCPがcurrentCpに設定されている場合はcurrentCpをnullにする。
     *
     * @param uuid UUID
     * @param loc プレイヤーのいる座標
     * @param cpName 削除したいチェックポイント名
     * @return 削除に成功したか
     */
    public static boolean removeCheckpoint(UUID uuid, Location loc, String cpName){
        World world = loc.getWorld();

        Location toDelete = cpDao.getLocation(uuid, world, cpName);
        if(toDelete == null) return false;

        cpDao.delete(uuid, world, cpName);

        if(toDelete.equals(globalRecentCp.get(uuid))) {
            globalRecentCp.put(uuid, null); //削除したCPがcurrentCpならcurrentCpにnullを代入
        }
        if(toDelete.equals(localRecentCp.get(uuid))) {
            localRecentCp.put(uuid, null);
        }

        return true;
    }

    /**
     * 指定された名前のチェックポイントを今のチェックポイントに設定し、その位置を返す。
     *
     * @param uuid UUID
     * @param loc プレイヤーのいる座標
     * @param cpName チェックポイント名
     * @return 選択に成功したか
     */
    public static boolean selectCheckpoint(UUID uuid, Location loc, String cpName){
        World world = loc.getWorld();
        Location selectedLoc = cpDao.getLocation(uuid, world, cpName);

        if(selectedLoc == null) return false;

        globalRecentCp.put(uuid, selectedLoc);
        localRecentCp.put(uuid, selectedLoc);
        return true;
    }

    /**
     * 現在のワールドにおいてプレイヤーが登録しているチェックポイント名の一覧を返す。
     *
     * @param uuid UUID
     * @param loc プレイヤーのいる座標
     * @return チェックポイント名の一覧
     */
    public static Collection<String> getCheckpointNames(UUID uuid, Location loc){
        World world = loc.getWorld();

        return cpDao.getCheckpointNames(uuid, world);
    }

    /**
     * 最後に設定したチェックポイントの位置を更新する。
     *
     * @param uuid UUID
     * @param world ワールド
     * @param loc 位置
     */
    public static void saveRecentCp(UUID uuid, Boolean isGlobal, World world, Location loc){
        if(!isGlobal) recentCpDao.insert(uuid, false, world, loc);
        else {
            World recentCpWorld = globalRecentCp.get(uuid).getWorld();
            if(recentCpWorld == null) return;
            recentCpDao.updateGlobal(uuid, recentCpWorld);
        }
    }

    /**
     * そのワールドで最後に設定したチェックポイントの位置を読み込む。
     *
     * @param uuid UUID
     * @param world ワールド
     */
    public static void loadRecentLocalCp(UUID uuid, World world){
        Location loc = recentCpDao.getLocalLocation(uuid, world);
        localRecentCp.put(uuid, loc);
    }

    /**
     * 全ワールドで最後に設定したチェックポイントの位置を読み込む。
     *
     * @param uuid UUID
     */
    public static void loadRecentGlobalCp(UUID uuid){
        Location loc = recentCpDao.getGlobalLocation(uuid);
        globalRecentCp.put(uuid, loc);
    }

    public static void setCrossWorldTpAllowed(boolean allowed){
        isCrossWorldTpAllowed = allowed;
    }

    public static boolean isCrossWorldTpAllowed(){
        return isCrossWorldTpAllowed;
    }

    public static Location getGlobalRecentCpLocation(UUID uuid){
        return globalRecentCp.get(uuid);
    }

    public static Location getLocalRecentCpLocation(UUID uuid){
        return localRecentCp.get(uuid);
    }
}
