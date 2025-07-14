package com.github.cresc28.speedrun.db.course;

import com.github.cresc28.speedrun.config.ConfigManager;
import org.bukkit.Bukkit;

import java.lang.Record;
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
        String sql = "INSERT INTO record (uuid, course_name, record_time, record_at) VALUES(?, ?, ?, datetime('now', 'localtime'))";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setString(2, courseName);
            ps.setInt(3, record);

            ps.executeUpdate();
        } catch(SQLException e) {
            LOGGER.log(Level.SEVERE,"insert()でエラーが発生しました。");
        }
    }

    public void deleteRecordIfExceedLimit(UUID uuid, String courseName){
        String sql = "DELETE FROM record WHERE id IN( " +
                "SELECT id FROM record WHERE uuid = ? AND course_name = ? ORDER BY record_time DESC, record_at DESC " +
                "LIMIT ( SELECT COUNT(*) - ? FROM record WHERE uuid = ? AND course_name = ?))";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setString(2, courseName);
            ps.setInt(3, ConfigManager.getMaxRecordStored());
            ps.setString(4, uuid.toString());
            ps.setString(5, courseName);

            ps.executeUpdate();
        } catch(SQLException e){
            LOGGER.log(Level.SEVERE, "deleteRecordIfExceedLimit()でエラーが発生しました。");
        }
    }

    public void insertAndRemoveSomeBadRecord(UUID uuid, String courseName, int record){
        insert(uuid, courseName, record);
        deleteRecordIfExceedLimit(uuid, courseName);
    }

    public List<Map.Entry<String, String>> getTopRecordNoDup(String courseName, int num){
        List<Map.Entry<String, String>> result = new ArrayList<>();
        String sql = "SELECT uuid, MIN(record_time) AS best_record FROM record WHERE course_name = ? GROUP BY uuid ORDER BY best_record ASC LIMIT ?";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, courseName);
            ps.setInt(2, num);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                String uuid = rs.getString("uuid");
                String playerName = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
                if (playerName == null) {
                    playerName = "UnknownPlayer";
                }
                String record = rs.getString("best_record");

                result.add(new AbstractMap.SimpleEntry<>(playerName, record));
            }
        } catch (SQLException e){
            LOGGER.log(Level.SEVERE, "insertAndRemoveSomeBadRecord()でエラーが発生しました。");
        }

        return result;
    }
}
