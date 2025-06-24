package com.github.cresc28.speedrun.database;

import com.github.cresc28.speedrun.data.CourseType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * コースデータを管理するクラス。
 */
public class CourseDao {
    private static final Logger LOGGER = Logger.getLogger("CourseDataDao");

    /**
     * コースデータを登録する。
     *
     * @param type コースのタイプ
     * @param courseName コース名
     * @param loc 位置
     */
    public void insert(CourseType type, String courseName, Location loc){
        String sql = "INSERT OR REPLACE INTO courseData " +
                "(type, course_name, world_uid, x, y, z) " +
                "VALUES(?, ?, ?, ?, ?, ?)";

        try(PreparedStatement ps = CourseDatabase.getConnection().prepareStatement(sql)){
            ps.setInt(1, type.getId());
            ps.setString(2, courseName);
            ps.setString(3, loc.getWorld().getUID().toString());
            ps.setDouble(4, loc.getX());
            ps.setDouble(5, loc.getY());
            ps.setDouble(6, loc.getZ());
            ps.executeUpdate();
        } catch(SQLException e){
            LOGGER.log(Level.SEVERE,"INSERT文でエラーが発生しました。");
        }
    }

    /**
     * コースを削除する。
     *
     * @param type コースのタイプ
     * @param courseName コース名
     * @return 削除に成功した行数
     */
    public boolean delete(CourseType type, String courseName){
        String sql = "DELETE FROM courseData WHERE type = ? AND course_name = ?";
        try(PreparedStatement ps = CourseDatabase.getConnection().prepareStatement(sql)){
            ps.setInt(1, type.getId());
            ps.setString(2, courseName);
            return ps.executeUpdate() > 0;
        } catch(SQLException e){
            LOGGER.log(Level.SEVERE,"DELETE文でエラーが発生しました。");
            return false;
        }
    }

    /**
     * コースをタイプによらず削除する。
     *
     * @param courseName コース名
     * @return 削除に成功した行数
     */
    public boolean delete(String courseName){
        String sql = "DELETE FROM courseData WHERE course_name = ?";
        try(PreparedStatement ps = CourseDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, courseName);
            return ps.executeUpdate() > 0;
        } catch(SQLException e){
            LOGGER.log(Level.SEVERE,"DELETE文でエラーが発生しました。");
            return false;
        }
    }

    /**
     * コースをすべて取得する。
     *
     * @return コースを格納したマップ
     */
    public Map<Location, Map<CourseType, String>> getCourses(){
        Map<Location, Map<CourseType, String>> result = new HashMap<>();
        String sql = "SELECT type, course_name, world_uid, x, y, z FROM courseData";

        try(PreparedStatement ps = CourseDatabase.getConnection().prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String courseName = rs.getString("course_name");
                int typeId = rs.getInt("type");
                String worldUid = rs.getString("world_uid");

                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");

                World world = Bukkit.getWorld(UUID.fromString(worldUid));
                if (world == null) continue; // ワールドが存在しないならスキップ

                Location location = new Location(world, x, y, z);
                CourseType type = CourseType.fromId(typeId); // enum変換（要 fromId 実装）

                result.computeIfAbsent(location, loc -> new EnumMap<>(CourseType.class)).put(type, courseName);
            }
        } catch(SQLException e){
            LOGGER.log(Level.SEVERE, "SELECT文でエラーが発生しました。");
        }

        return result;
    }
}
