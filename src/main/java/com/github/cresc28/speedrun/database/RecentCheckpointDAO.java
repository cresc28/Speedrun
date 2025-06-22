package com.github.cresc28.speedrun.database;

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

public class RecentCheckpointDAO {
    private static final Logger LOGGER = Logger.getLogger("RecentCheckpointsDAO");

    /**
     * 最後に設定したCPを登録する。
     *
     * @param uuid UUID
     * @param isGlobal そのワールド内の最新CPか
     * @param world ワールド
     * @param loc 位置
     */
    public void insert(UUID uuid, Boolean isGlobal, World world, Location loc){
        String sql = "INSERT OR REPLACE INTO recentCheckpoints " +
                "(uuid, isGlobal, world, x, y, z, yaw, pitch) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

        try(PreparedStatement ps = RecentCheckpointsDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setInt(2, isGlobal ? 1 : 0);
            ps.setString(3, world.getName());
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
        String sql = "SELECT x, y, z, yaw, pitch FROM recentCheckpoints WHERE uuid = ? AND world = ?";
        try(PreparedStatement ps = RecentCheckpointsDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setString(2, world.getName());
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
        String sql = "SELECT world, x, y, z, yaw, pitch FROM recentCheckpoints WHERE uuid = ? AND isGlobal = 1";
        try(PreparedStatement ps = RecentCheckpointsDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String worldStr = rs.getString("world");
                World world = Bukkit.getWorld(worldStr);
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

    public void updateGlobal(UUID uuid, World world){
        String sql1 = "UPDATE recentCheckpoints SET isGlobal = 0 WHERE uuid = ? AND isGlobal = 1";
        String sql2 = "UPDATE recentCheckpoints SET isGlobal = 1 WHERE uuid = ? AND world = ?";

        try (PreparedStatement ps1 = RecentCheckpointsDatabase.getConnection().prepareStatement(sql1);
             PreparedStatement ps2 = RecentCheckpointsDatabase.getConnection().prepareStatement(sql2)) {

            ps1.setString(1, uuid.toString());
            ps1.executeUpdate();

            ps2.setString(1, uuid.toString());
            ps2.setString(2, world.getName());
            ps2.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "UPDATE文でエラーが発生しました。", e);
        }
    }
}
