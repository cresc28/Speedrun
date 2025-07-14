package com.github.cresc28.speedrun.db.course;

import com.github.cresc28.speedrun.config.ConfigManager;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Recordデータベースへのアクセスクラス。
 */
public class RecordDao {
    private static final Logger LOGGER = Logger.getLogger("RecordDao");

    public void insert(UUID uuid, String courseName, int record){
        String sql = "INSERT INTO record (uuid, course_name, record, record_at) VALUES(?, ?, ?, datetime('now', 'localtime'))";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setString(2, courseName);
            ps.setInt(3, record);

            ps.executeUpdate();
        } catch(SQLException e) {
            LOGGER.log(Level.SEVERE,"INSERT文でエラーが発生しました。");
        }
    }

    public void deleteRecordIfExceedLimit(UUID uuid, String courseName){
        String sql = "DELETE FROM record WHERE id IN( " +
                "SELECT id FROM record WHERE uuid = ? AND course_name = ? ORDER BY record DESC, record_at DESC " +
                "LIMIT ( SELECT COUNT(*) - ? FROM record WHERE uuid = ? AND course_name = ?))";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setString(2, courseName);
            ps.setInt(3, ConfigManager.getMaxRecordStored());
            ps.setString(4, uuid.toString());
            ps.setString(5, courseName);

            ps.executeUpdate();
        } catch(SQLException e){
            LOGGER.log(Level.SEVERE, "DELETE文でエラーが発生しました。");
        }
    }

    public void insertAndRemoveSomeBadRecord(UUID uuid, String courseName, int record){
        insert(uuid, courseName, record);
        deleteRecordIfExceedLimit(uuid, courseName);
    }
}
