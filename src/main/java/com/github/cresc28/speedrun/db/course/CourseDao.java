package com.github.cresc28.speedrun.db.course;

import com.github.cresc28.speedrun.data.CourseEntry;
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
 * Courseデータベースへのアクセスクラス。
 */
public class CourseDao {
    private static final Logger LOGGER = Logger.getLogger("CourseDao");

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
            LOGGER.log(Level.SEVERE,"insert()でエラーが発生しました。", e);
        }
    }

    /**
     * コースを削除する。
     *
     * @param type コースのタイプ
     * @param courseName コース名
     * @return 削除に成功したか
     */
    public boolean delete(CourseType type, String courseName){
        String prefix = courseName + ".%";
        String sql = "DELETE FROM courseData WHERE type = ? AND (course_name = ? OR course_name LIKE ?)"; //courseName.から始まるもの(中継地点の場合)も削除
        try(PreparedStatement ps = CourseDatabase.getConnection().prepareStatement(sql)){
            ps.setInt(1, type.getId());
            ps.setString(2, courseName);
            ps.setString(3, prefix);
            return ps.executeUpdate() > 0;
        } catch(SQLException e){
            LOGGER.log(Level.SEVERE,"delete()でエラーが発生しました。", e);
            return false;
        }
    }

    /**
     * コースをタイプによらず削除する。
     *
     * @param courseName コース名
     * @return 削除に成功したか
     */
    public boolean delete(String courseName){
        String prefix = courseName + ".%";
        String sql = "DELETE FROM courseData WHERE course_name = ? OR course_name LIKE ?"; //courseName.から始まるもの(中継地点の場合)も削除
        try(PreparedStatement ps = CourseDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, courseName);
            ps.setString(2, prefix);
            return ps.executeUpdate() > 0;
        } catch(SQLException e){
            LOGGER.log(Level.SEVERE,"delete()でエラーが発生しました。", e);
            return false;
        }
    }

    /**
     * コースを削除する。
     *
     * @param loc 位置
     * @return 削除に成功したか
     */
    public boolean delete(Location loc) {
        String sql = "DELETE FROM courseData WHERE world_uid = ? AND x = ? AND y = ? AND z = ?";

        try (PreparedStatement ps = CourseDatabase.getConnection().prepareStatement(sql)) {
            ps.setString(1, loc.getWorld().getUID().toString());
            ps.setDouble(2, loc.getX());
            ps.setDouble(3, loc.getY());
            ps.setDouble(4, loc.getZ());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "delete()でエラーが発生しました。", e);
            return false;
        }
    }

    /**
     * コースをすべて取得する。
     *
     * @return コースを格納したマップ
     */
    public Map<Location, CourseEntry> getCourses(){
        Map<Location, CourseEntry> result = new HashMap<>();
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

                Location loc = new Location(world, x, y, z);
                CourseType type = CourseType.fromId(typeId); // enum変換（要 fromId 実装）

                result.put(loc, new CourseEntry(type, courseName));
            }
        } catch(SQLException e){
            LOGGER.log(Level.SEVERE, "getCourses()でエラーが発生しました。", e);
        }

        return result;
    }
}
