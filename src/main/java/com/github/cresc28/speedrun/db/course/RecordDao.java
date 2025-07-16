package com.github.cresc28.speedrun.db.course;

import com.github.cresc28.speedrun.config.ConfigManager;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Recordデータベースへのアクセスクラス。
 */
public class RecordDao {
    private static final Logger LOGGER = Logger.getLogger("RecordDao");

    public void insert(UUID uuid, String courseName, int record){
        String sql = "INSERT INTO record (uuid, course_name, finish_time, record_at) VALUES(?, ?, ?, datetime('now', 'localtime'))";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setString(2, courseName);
            ps.setInt(3, record);

            ps.executeUpdate();
        } catch(SQLException e) {
            LOGGER.log(Level.SEVERE,"insert()でエラーが発生しました。", e);
        }
    }

    public void deleteRecordIfExceedLimit(UUID uuid, String courseName){
        String sql = "DELETE FROM record WHERE record_id IN( " +
                "SELECT record_id FROM record WHERE uuid = ? AND course_name = ? ORDER BY finish_time DESC, record_at DESC " +
                "LIMIT ( SELECT MAX(COUNT(*) - ?, 0) FROM record WHERE uuid = ? AND course_name = ?))";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setString(2, courseName);
            ps.setInt(3, ConfigManager.getMaxRecordStored());
            ps.setString(4, uuid.toString());
            ps.setString(5, courseName);

            ps.executeUpdate();
        } catch(SQLException e){
            LOGGER.log(Level.SEVERE, "deleteRecordIfExceedLimit()でエラーが発生しました。", e);
        }
    }

    public void insertAndRemoveSomeRecord(UUID uuid, String courseName, int record){
        insert(uuid, courseName, record);
        deleteRecordIfExceedLimit(uuid, courseName);
    }

    public List<Map.Entry<String, String>> getTopRecordNoDup(String courseName, int startRank, int count){
        List<Map.Entry<String, String>> result = new ArrayList<>();
        String sql = "SELECT uuid, MIN(finish_time) AS best_time FROM record WHERE course_name = ? GROUP BY uuid ORDER BY best_time ASC LIMIT ? OFFSET ?";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, courseName);
            ps.setInt(2, count);
            ps.setInt(3, startRank - 1);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                String uuid = rs.getString("uuid");
                String playerName = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
                if (playerName == null) {
                    playerName = "UnknownPlayer";
                }
                String record = rs.getString("best_time");

                result.add(new AbstractMap.SimpleEntry<>(playerName, record));
            }
        } catch (SQLException e){
            LOGGER.log(Level.SEVERE, "getTopRecordNoDup()でエラーが発生しました。", e);
        }

        return result;
    }

    public List<Map.Entry<String, String>> getTopRecordDup(String courseName, int startRank, int count){
        List<Map.Entry<String, String>> result = new ArrayList<>();
        String sql = "SELECT uuid, finish_time FROM record WHERE course_name = ? ORDER BY finish_time ASC LIMIT ? OFFSET ?";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, courseName);
            ps.setInt(2, count);
            ps.setInt(3, startRank - 1);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                String uuid = rs.getString("uuid");
                String playerName = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
                if (playerName == null) {
                    playerName = "UnknownPlayer";
                }
                String record = rs.getString("finish_time");

                result.add(new AbstractMap.SimpleEntry<>(playerName, record));
            }
        } catch (SQLException e){
            LOGGER.log(Level.SEVERE, "getTopRecordDup()でエラーが発生しました。", e);
        }

        return result;
    }

    public Map.Entry<Integer, String> getRankAndRecordNoDup(UUID uuid, String courseName, boolean worstTie) {
        String sign = worstTie ? " <= " : " < ";
        String plusOne = worstTie ? " " : " + 1 ";

        //ROW_NUMBER()が使えないため強引に...。
        String sql = "SELECT best_time, (" +
                "SELECT COUNT(*) FROM (" +
                "SELECT uuid, MIN(finish_time) AS best_time FROM record WHERE course_name = ? GROUP BY uuid) AS sub " +
                "WHERE sub.best_time" + sign +  "ranked.best_time) " + plusOne + " AS rank FROM (" +
                "SELECT uuid, MIN(finish_time) AS best_time FROM record WHERE course_name = ? GROUP BY uuid) AS ranked WHERE uuid = ? LIMIT 1";

        try (PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)) {
            ps.setString(1, courseName);
            ps.setString(2, courseName);
            ps.setString(3, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                int rank = rs.getInt("rank");
                String record = rs.getString("best_time");

                return new AbstractMap.SimpleEntry<>(rank, record);
            }

        } catch (SQLException e){
            LOGGER.log(Level.SEVERE, "getRankAndRecordNoDup()でエラーが発生しました。", e);
        }

        return null;
    }

    public Map.Entry<Integer, String> getRankAndRecordDup(UUID uuid, String courseName, boolean worstTie) {
        String sign = worstTie ? " <= " : " < ";
        String plusOne = worstTie ? " " : " + 1 ";
        //ROW_NUMBER()が使えない。
        String sql = "SELECT finish_time, ( " +
                "SELECT COUNT(*) FROM record r2 WHERE r2.course_name = r1.course_name AND r2.finish_time"
                + sign + "r1.finish_time) " + plusOne + " AS rank FROM record" +
                " r1 WHERE r1.uuid = ? AND r1.course_name = ? ORDER BY r1.finish_time ASC LIMIT 1";

        try (PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, courseName);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                int rank = rs.getInt("rank");
                String record = rs.getString("finish_time");

                return new AbstractMap.SimpleEntry<>(rank, record);
            }

        } catch (SQLException e){
            LOGGER.log(Level.SEVERE, "getRankAndRecordDup()でエラーが発生しました。", e);
        }

        return null;
    }

    public boolean contains(String courseName){
        String sql = "SELECT 1 FROM record WHERE course_name = ?";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, courseName);

            ResultSet rs = ps.executeQuery();
            return rs.next();
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE, "contains()でエラーが発生しました", e);
        }

        return false;
    }
}
