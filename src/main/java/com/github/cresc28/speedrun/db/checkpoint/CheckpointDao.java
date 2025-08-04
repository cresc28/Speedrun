package com.github.cresc28.speedrun.db.checkpoint;

import org.bukkit.Location;
import org.bukkit.World;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checkpointsデータベースへのアクセスクラス。
 */

public class CheckpointDao {
    private static final Logger LOGGER = Logger.getLogger("CheckpointsDao");

    /**
     * チェックポイントを登録する。
     *
     * @param uuid UUID
     * @param cpName チェックポイント名
     * @param loc 位置
     */
    public void insert(UUID uuid, String cpName, Location loc){
        String sql = "INSERT OR REPLACE INTO checkpoints " +
                "(uuid, world_uid, cp_name, x, y, z, yaw, pitch) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

        try(PreparedStatement ps = CheckpointDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setString(2, loc.getWorld().getUID().toString());
            ps.setString(3, cpName);
            ps.setDouble(4, loc.getX());
            ps.setDouble(5, loc.getY());
            ps.setDouble(6, loc.getZ());
            ps.setDouble(7, loc.getYaw());
            ps.setDouble(8, loc.getPitch());
            ps.executeUpdate();
        } catch(SQLException e){
            LOGGER.log(Level.SEVERE,"insert()でエラーが発生しました。", e);
        }
    }

    /**
     * チェックポイントを削除する。
     *
     * @param uuid UUID
     * @param world ワールド
     * @param cpName チェックポイント名
     * @return 削除に成功したか
     */
    public boolean delete(UUID uuid, World world, String cpName){
        String sql = "DELETE FROM checkpoints WHERE uuid = ? AND world_uid = ? AND cp_name =?";
        try(PreparedStatement ps = CheckpointDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setString(2, world.getUID().toString());
            ps.setString(3, cpName);
            return ps.executeUpdate() > 0;
        } catch(SQLException e){
            LOGGER.log(Level.SEVERE,"delete()でエラーが発生しました。", e);
            return false;
        }
    }

    /**
     * チェックポイントの位置を検索する。
     *
     * @param uuid UUID
     * @param world ワールド
     * @param cpName チェックポイント名
     * @return チェックポイントの位置
     */
    public Location getLocation(UUID uuid, World world, String cpName){
        String sql = "SELECT x, y, z, yaw, pitch FROM checkpoints WHERE uuid = ? AND world_uid = ? AND cp_name = ?";
        try(PreparedStatement ps = CheckpointDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setString(2, world.getUID().toString());
            ps.setString(3, cpName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                float yaw = (float) rs.getDouble("yaw");
                float pitch = (float) rs.getDouble("pitch");
                return new Location(world, x, y, z, yaw, pitch);
            }
        } catch(SQLException e){
            LOGGER.log(Level.SEVERE,"getLocation()でエラーが発生しました。", e);
        }

        return null;
    }

    /**
     * 指定されたプレイヤーが指定されたワールドで登録しているチェックポイント名をすべて取得する。
     *
     * @param uuid UUID
     * @param world ワールド
     * @return チェックポイント名のリスト
     */
    public List<String> getCheckpointNames(UUID uuid, World world){
        List<String> names = new ArrayList<>();
        String sql = "SELECT cp_name FROM checkpoints WHERE uuid = ? AND world_uid = ?";

        try(PreparedStatement ps = CheckpointDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setString(2, world.getUID().toString());
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                names.add(rs.getString("cp_name"));
            }
        } catch(SQLException e){
            LOGGER.log(Level.SEVERE, "getCheckpointNames()でエラーが発生しました。", e);
        }

        return names;
    }
}