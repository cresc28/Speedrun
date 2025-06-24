package com.github.cresc28.speedrun.manager;

import com.github.cresc28.speedrun.database.CheckpointDao;
import com.github.cresc28.speedrun.database.RecentCheckpointDao;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

/**
 * チェックポイントのロード・セーブ・登録・削除・取得を行うクラス。
 * プレイヤーごと、ワールドごとにチェックポイントを管理し、
 * 現在位置や最後に踏んだチェックポイントの情報も保持する。
 */
public class CheckpointManager {
    private final Map<UUID, Location> globalRecentCp = new HashMap<>(); //最後に設定されたCP
    private final Map<UUID, Location> localRecentCp = new HashMap<>(); //現在のワールドで最後に設定されたCP
    private final CheckpointDao cpDao = new CheckpointDao();
    private final RecentCheckpointDao recentCpDao = new RecentCheckpointDao();

    /**
     * 一時的なチェックポイントを登録。
     *
     * @param uuid UUID
     * @param loc プレイヤーのいる座標
     */
    public void registerCheckpoint(UUID uuid, Location loc){
        registerCheckpoint(uuid, loc, "tmp");
    }

    /**
     * プレイヤーの現在位置を指定された名前でチェックポイントとして登録する。
     *
     * @param uuid UUID
     * @param loc プレイヤーのいる座標
     * @param cpName チェックポイント名
     */
    public void registerCheckpoint(UUID uuid, Location loc, String cpName){
        cpDao.insert(uuid, cpName, loc);

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
    public boolean removeCheckpoint(UUID uuid, Location loc, String cpName){
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
    public boolean selectCheckpoint(UUID uuid, Location loc, String cpName){
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
    public Collection<String> getCheckpointNames(UUID uuid, Location loc){
        World world = loc.getWorld();

        return cpDao.getCheckpointNames(uuid, world);
    }

    /**
     * 最後に設定したチェックポイントの位置を更新する。
     *
     * @param uuid UUID
     * @param isGlobal globalかlocalか
     * @param loc 位置
     */
    public void saveRecentCp(UUID uuid, Boolean isGlobal, Location loc){
        if(!isGlobal) recentCpDao.insert(uuid, false, loc);
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
    public void loadRecentLocalCp(UUID uuid, World world){
        Location loc = recentCpDao.getLocalLocation(uuid, world);
        localRecentCp.put(uuid, loc);
    }

    /**
     * 全ワールドで最後に設定したチェックポイントの位置を読み込む。
     *
     * @param uuid UUID
     */
    public void loadRecentGlobalCp(UUID uuid){
        Location loc = recentCpDao.getGlobalLocation(uuid);
        globalRecentCp.put(uuid, loc);
    }

    /**
     * recentCpから特定UUIDのプレイヤーの情報を削除する。(プレイヤー退出時用処理)
     *
     * @param uuid UUID
     */
    public void removeRecentCpFromMap(UUID uuid){
        globalRecentCp.remove(uuid);
        localRecentCp.remove(uuid);
    }

    public Location getGlobalRecentCpLocation(UUID uuid){
        return globalRecentCp.get(uuid);
    }

    public Location getLocalRecentCpLocation(UUID uuid){
        return localRecentCp.get(uuid);
    }
}
