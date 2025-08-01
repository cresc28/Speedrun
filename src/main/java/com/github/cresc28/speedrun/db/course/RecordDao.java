package com.github.cresc28.speedrun.db.course;

import com.github.cresc28.speedrun.config.ConfigManager;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Recordデータベースへのアクセスクラス。
 */
public class RecordDao {
    private static final Logger LOGGER = Logger.getLogger("RecordDao");

    /**
     * 記録の追加
     *
     * @param uuid 記録保持者のUUID
     * @param courseName コース名
     * @param record 記録(tick表記)
     * @return ID
     */
    public int insert(UUID uuid, String courseName, int record){
        String sql = "INSERT INTO record (uuid, course_name, finish_tick, record_at) VALUES(?, ?, ?, datetime('now', 'localtime'))";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, uuid.toString());
            ps.setString(2, courseName);
            ps.setInt(3, record);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);  //挿入されたIDを返す

        } catch(SQLException e) {
            LOGGER.log(Level.SEVERE,"insert()でエラーが発生しました。", e);
        }
        return -1;
    }

    /**
     * 記録の削除
     *
     * @param uuid 記録保持者のUUID
     * @param courseName コース名
     * @return 削除できたか
     */
    public boolean delete(UUID uuid, String courseName){
        String sql = "DELETE FROM record WHERE uuid = ? AND course_name = ?";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setString(2, courseName);
            int rows = ps.executeUpdate();
            return rows > 0;
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "delete()でエラーが発生しました。", e);
        }
        return false;
    }

    /**
     * 記録の削除
     *
     * @param uuid 記録保持者のUUID
     * @return 削除できたか
     */
    public boolean delete(UUID uuid){
        String sql = "DELETE FROM record WHERE uuid = ?";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            int rows = ps.executeUpdate();
            return rows > 0;
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "delete()でエラーが発生しました。", e);
        }
        return false;
    }

    /**
     * 記録の削除
     *
     * @param courseName コース名
     * @return 削除できたか
     */
    public boolean delete(String courseName){
        String sql = "DELETE FROM record WHERE course_name = ?";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, courseName);
            int rows = ps.executeUpdate();
            return rows > 0;
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "delete()でエラーが発生しました。", e);
        }
        return false;
    }

    /**
     * あるコースのあるUUIDのプレイヤーの記録を一つ削除する。（面倒なので同じ記録が複数ある場合は最新の記録を消すようにしている）
     *
     * @param uuid 記録保持者のuuid
     * @param courseName コース名
     * @param tick どの記録か
     * @return 削除できたか
     */
    public boolean delete(UUID uuid, String courseName, Integer tick){
        String sql = "DELETE FROM record WHERE ROWID IN ( SELECT ROWID FROM record WHERE uuid = ? AND course_name = ? AND finish_tick = ? ORDER BY record_at DESC LIMIT 1)";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setString(2, courseName);
            ps.setInt(3, tick);
            int rows = ps.executeUpdate();
            return rows > 0;
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "delete()でエラーが発生しました。", e);
        }
        return false;
    }

    /**
     * 記録の保持上限を超えたら削除する
     *
     * @param uuid 記録保持者のUUID
     * @param courseName コース名
     */
    public void deleteRecordIfExceedLimit(UUID uuid, String courseName){
        String sql = "DELETE FROM record WHERE record_id IN( " +
                "SELECT record_id FROM record WHERE uuid = ? AND course_name = ? ORDER BY finish_tick DESC, record_at DESC " +
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

    /**
     * 記録の挿入と上限超過時の削除を行う。
     *
     * @param uuid 記録保持者のUUID
     * @param courseName コース名
     * @param record 記録(tick表記)
     * @return ID
     */
    public int insertAndRemoveSomeRecord(UUID uuid, String courseName, int record){
        int id = insert(uuid, courseName, record);
        deleteRecordIfExceedLimit(uuid, courseName);
        return id;
    }

    /**
     * 各プレイヤーのベスト記録を比較し、記録を昇順に取得する。
     *
     * @param courseName コース名
     * @param startRank 何位から取得するか
     * @param count 何件取得するか
     * @return 記録保持者と記録
     */
    public List<Map.Entry<String, String>> getTopRecordNoDup(String courseName, int startRank, int count){
        List<Map.Entry<String, String>> result = new ArrayList<>();
        String sql = "SELECT uuid, MIN(finish_tick) AS best_time FROM record WHERE course_name = ? GROUP BY uuid ORDER BY best_time ASC LIMIT ? OFFSET ?";

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

    /**
     * 同一プレイヤーの複数記録を許し、記録を昇順に取得する。
     *
     * @param courseName コース名
     * @param startRank 何位から取得するか
     * @param count 何件取得するか
     * @return 記録保持者と記録
     */
    public List<Map.Entry<String, String>> getTopRecordDup(String courseName, int startRank, int count){
        List<Map.Entry<String, String>> result = new ArrayList<>();
        String sql = "SELECT uuid, finish_tick FROM record WHERE course_name = ? ORDER BY finish_tick ASC LIMIT ? OFFSET ?";

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
                String record = rs.getString("finish_tick");

                result.add(new AbstractMap.SimpleEntry<>(playerName, record));
            }
        } catch (SQLException e){
            LOGGER.log(Level.SEVERE, "getTopRecordDup()でエラーが発生しました。", e);
        }

        return result;
    }

    /**
     * 指定プレイヤーのベスト順位(各プレイヤーのベスト記録のみを考慮)とベスト記録を返す。
     *
     * @param uuid 記録保持者のUUID
     * @param courseName コース名
     * @param worstTie タイ記録の場合に悪い方に合わせるか、1位タイが対象プレイヤーを含め3人いる場合3位と返す。(特殊用途以外falseで良い)
     * @return ベスト順位とベスト記録
     */
    public Map.Entry<Integer, Integer> getRankAndRecordNoDup(UUID uuid, String courseName, boolean worstTie) {
        String sign = worstTie ? " <= " : " < ";
        String plusOne = worstTie ? " " : " + 1 ";

        //ROW_NUMBER()が使えないため強引に...。
        String sql = "SELECT best_time, (" +
                "SELECT COUNT(*) FROM (" +
                "SELECT uuid, MIN(finish_tick) AS best_time FROM record WHERE course_name = ? GROUP BY uuid) AS sub " +
                "WHERE sub.best_time" + sign +  "ranked.best_time) " + plusOne + " AS rank FROM (" +
                "SELECT uuid, MIN(finish_tick) AS best_time FROM record WHERE course_name = ? GROUP BY uuid) AS ranked WHERE uuid = ? LIMIT 1";

        try (PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)) {
            ps.setString(1, courseName);
            ps.setString(2, courseName);
            ps.setString(3, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                int rank = rs.getInt("rank");
                int record = rs.getInt("best_time");

                return new AbstractMap.SimpleEntry<>(rank, record);
            }

        } catch (SQLException e){
            LOGGER.log(Level.SEVERE, "getRankAndRecordNoDup()でエラーが発生しました。", e);
        }

        return null;
    }

    /**
     * 指定プレイヤーのベスト順位(同一プレイヤーの複数記録を考慮)とベスト記録を返す。
     *
     * @param uuid 記録保持者のUUID
     * @param courseName コース名
     * @param worstTie タイ記録の場合に悪い方に合わせるか、1位タイが対象プレイヤーを含め3人いる場合3位と返す。(特殊用途以外falseで良い)
     * @return ベスト順位とベスト記録
     */
    public Map.Entry<Integer, Integer> getRankAndRecordDup(UUID uuid, String courseName, boolean worstTie) {
        String sign = worstTie ? " <= " : " < ";
        String plusOne = worstTie ? " " : " + 1 ";
        //ROW_NUMBER()が使えない。
        String sql = "SELECT finish_tick, ( " +
                "SELECT COUNT(*) FROM record r2 WHERE r2.course_name = r1.course_name AND r2.finish_tick"
                + sign + "r1.finish_tick) " + plusOne + " AS rank FROM record" +
                " r1 WHERE r1.uuid = ? AND r1.course_name = ? ORDER BY r1.finish_tick ASC LIMIT 1";

        try (PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, courseName);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                int rank = rs.getInt("rank");
                int record = rs.getInt("finish_tick");

                return new AbstractMap.SimpleEntry<>(rank, record);
            }

        } catch (SQLException e){
            LOGGER.log(Level.SEVERE, "getRankAndRecordDup()でエラーが発生しました。", e);
        }

        return null;
    }

    /**
     * あるコースの記録が存在するか
     *
     * @param courseName コース名
     * @return 存在するか
     */
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

    /**
     * 中継地点通過タイムを追加する
     *
     * @param recordId どのIDの記録(recordテーブル)の中継地点の通過タイムか
     * @param viaPointRecord 中継地点名と通過タイム(tick表記)
     */
    public void insertViaPointRecord(int recordId, Map<String, Integer> viaPointRecord){
        for(Map.Entry<String, Integer> entry : viaPointRecord.entrySet()){
            String sql = "INSERT INTO record_via_point (record_id, via_point_name, tick) VALUES (?, ?, ?)";
            try (PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)) {
                ps.setInt(1, recordId);
                ps.setString(2, entry.getKey());
                ps.setInt(3, entry.getValue());
                ps.executeUpdate();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "registerViaPoint()でエラーが発生しました", e);
            }
        }
    }

    /**
     * 指定プレイヤーの最も良い記録の中継地点通過タイムを取得
     *
     * @param uuid 記録保持者のUUID
     * @param courseName コース名
     * @return 中継地点名と通過タイム(tick表記)
     */
    public Map<String, Integer> getViaPointRecord(UUID uuid, String courseName){
        Map<String, Integer> viaPointRecord = new LinkedHashMap<>();

        String sql = "SELECT v.via_point_name, v.tick FROM record_via_point v WHERE v.record_id = (" +
                "SELECT record_id FROM record WHERE uuid = ? AND course_name = ? ORDER BY finish_tick ASC LIMIT 1)";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setString(2, courseName);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                String viaPointName = rs.getString("via_point_name");
                int tick = rs.getInt("tick");

                viaPointRecord.put(viaPointName, tick);
            }
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "getViaPointRecord()でエラーが発生しました。", e);
        }

        return viaPointRecord;
    }

    /**
     * 順位を取得
     *
     * @param courseName コース名
     * @param finishTick タイム(tick表記)
     * @return 順位
     */
    public int getRank(String courseName, int finishTick){
        String sql = "SELECT COUNT(*) + 1 FROM (" +
                "SELECT MIN(finish_tick) AS best_time FROM record WHERE course_name = ? GROUP BY uuid) AS best_record WHERE best_time < ?";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, courseName);
            ps.setInt(2, finishTick);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) return rs.getInt(1);
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE, "getRank()でエラーが発生しました。", e);
        }

        return 1;
    }

    /**
     * あるプレイヤーがあるコースで持つ記録を全て取得する
     *
     * @param uuid 記録保持者のUUID
     * @param courseName コース名
     * @return 記録リスト
     */
    public List<Integer> getFinishTick(UUID uuid, String courseName) {
        List<Integer> result = new ArrayList<>();
        String sql = "SELECT finish_tick FROM record WHERE uuid = ? AND course_name = ? ORDER BY finish_tick ASC";

        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, uuid.toString());
            ps.setString(2, courseName);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                int tick = rs.getInt("finish_tick");
                result.add(tick);
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE, "getTimes()でエラーが発生しました。", e);
        }

        return result;
    }

    /**
     * 一位の記録を取得する
     *
     * @param courseName コース名
     * @return 記録保持者と記録
     */
    public Map.Entry<UUID, Integer> getTopRecord(String courseName){
        String sql = "SELECT uuid, finish_tick FROM record WHERE course_name = ? ORDER BY finish_tick ASC LIMIT 1";
        try(PreparedStatement ps = RecordDatabase.getConnection().prepareStatement(sql)){
            ps.setString(1, courseName);
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                String uuid = rs.getString(1);
                int finishTick = rs.getInt(2);
                return new AbstractMap.SimpleEntry<>(UUID.fromString(uuid),finishTick);
            }
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE, "getTopPlayer()でエラーが発生しました。", e);
        }

        return null;
    }
}
