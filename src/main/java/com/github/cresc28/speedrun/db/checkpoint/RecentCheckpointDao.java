package com.github.cresc28.speedrun.db.checkpoint;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 最後に設定したチェックポイントを管理するクラス。
 */

public class RecentCheckpointDao {
    private static final Logger LOGGER = Logger.getLogger("RecentCheckpointsDao");

    /**
     * 最後に設定したCPを登録する。
     *
     * @param uuid UUID
     * @param isGlobal そのワールド内の最新CPか
     * @param loc 位置
     */
    public void insert(UUID uuid, Boolean isGlobal, Location loc){
        String sql = "INSERT OR REPLACE INTO recentCheckpoints " +
                "(uuid, is_global, world_uid, x, y, z, yaw, pitch) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

        try(PreparedStatement ps = RecentCheckpointDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setInt(2, isGlobal ? 1 : 0);
            ps.setString(3, loc.getWorld().getUID().toString());
            ps.setDouble(4, loc.getX());
            ps.setDouble(5, loc.getY());
            ps.setDouble(6, loc.getZ());
            ps.setDouble(7, loc.getYaw());
            ps.setDouble(8, loc.getPitch());
            ps.executeUpdate();
        } catch(SQLException e){
            LOGGER.log(Level.SEVERE,"INSERT文でエラーが発生しました。");
        }
    }

    /**
     * あるワールドで最後に登録したチェックポイントの位置を検索する。
     *
     * @param uuid UUID
     * @param world ワールド
     * @return チェックポイントの位置
     */
    public Location getLocalLocation(UUID uuid, World world){
        String sql = "SELECT x, y, z, yaw, pitch FROM recentCheckpoints WHERE uuid = ? AND world_uid = ?";
        try(PreparedStatement ps = RecentCheckpointDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setString(2, world.getUID().toString());
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
            LOGGER.log(Level.SEVERE,"SELECT文でエラーが発生しました。");
        }

        return null;
    }

    /**
     * 全ワールドで最後に登録したチェックポイントの位置を検索する。
     *
     * @param uuid UUID
     * @return チェックポイントの位置
     */
    public Location getGlobalLocation(UUID uuid){
        String sql = "SELECT world_uid, x, y, z, yaw, pitch FROM recentCheckpoints WHERE uuid = ? AND is_global = 1";
        try(PreparedStatement ps = RecentCheckpointDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String worldStr = rs.getString("world_uid");
                UUID worldUuid = UUID.fromString(worldStr);
                World world = Bukkit.getWorld(worldUuid);
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                float yaw = (float) rs.getDouble("yaw");
                float pitch = (float) rs.getDouble("pitch");

                if(world != null){
                    return new Location(world, x, y, z, yaw, pitch);
                }
            }
        } catch(SQLException e){
            LOGGER.log(Level.SEVERE,"SELECT文でエラーが発生しました。");
        }

        return null;
    }

    /**
     * ワールド全体で最後に設定されたチェックポイントを更新する。
     * 指定UUIDの全CPに対してisGlobal=0を設定し、
     * 指定されたワールドのチェックポイントをグローバルチェックポイント(isGlobal = 1)とする。。
     *
     * @param uuid uuid
     * @param world この操作の時点でプレイヤーが最後にいたワールドを渡せばよい。
     */
    public void updateGlobal(UUID uuid, World world){
        String sql1 = "UPDATE recentCheckpoints SET is_global = 0 WHERE uuid = ? AND is_global = 1";
        String sql2 = "UPDATE recentCheckpoints SET is_global = 1 WHERE uuid = ? AND world_uid = ?";

        try (PreparedStatement ps1 = RecentCheckpointDatabase.getConnection().prepareStatement(sql1);
             PreparedStatement ps2 = RecentCheckpointDatabase.getConnection().prepareStatement(sql2)) {

            ps1.setString(1, uuid.toString());
            ps1.executeUpdate();

            ps2.setString(1, uuid.toString());
            ps2.setString(2, world.getUID().toString());
            ps2.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "UPDATE文でエラーが発生しました。", e);
        }
    }
}
