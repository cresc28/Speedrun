package com.github.cresc28.speedrun.command;

import com.github.cresc28.speedrun.data.SpeedrunParameters;
import com.github.cresc28.speedrun.manager.CourseManager;
import com.github.cresc28.speedrun.db.course.RecordDao;
import com.github.cresc28.speedrun.utils.GameUtils;
import com.github.cresc28.speedrun.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class TopCommand implements CommandExecutor, TabCompleter {
    private final CourseManager courseManager;
    private final RecordDao recordDao;

    public TopCommand(SpeedrunParameters p){
        courseManager = p.getCourseManager();
        recordDao = p.getRecordDao();
    }
    /**
     * TAB補完の実装。
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        Player player = (Player) sender;

        if (args.length == 1) {
            TextUtils.completionExcludeViaPoint(courseManager.getAllCourseName(), args[0].toLowerCase(), completions);
        }

        else {
            List<String> options = Arrays.asList("dup", "detail", "above", player.getName());
            for (String option : options) {
                if (option.startsWith(args[args.length - 1].toLowerCase())) completions.add(option);
            }
        }
        return completions;
    }

    /**
     * コマンドの実装。
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof ConsoleCommandSender){ //コンソールからの実行を禁止
            sender.sendMessage("このコマンドはサーバコンソールでは実行できません。");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            return false;
        }

        String courseName = args[0];
        String targetPlayerId = null; //プレイヤー名
        boolean allowDup = false; //同一プレイヤーの複数記録を表示するか
        boolean isDetail = false; //詳細を表示するか
        boolean showAbove = false; //自分基準に自分より上位のプレイヤーを表示するモード
        int displayCount = 10; //何位まで表示するか

        for (int i = 1; i < args.length; i++) {
            String arg = args[i].toLowerCase();

            switch (arg) {
                case "dup": allowDup = true; break;
                case "above": showAbove = true; break;
                case "detail":
                    if (targetPlayerId == null) targetPlayerId = player.getName();
                    isDetail = true;
                    break;
                default:
                    if (TextUtils.isPositiveInteger(arg)) displayCount = Integer.parseInt(arg); //dup, detail, aboveというプレイヤーが存在する可能性は極めて低いため無視。
                    else targetPlayerId = arg;
            }
        }

        if(!recordDao.contains(courseName)){
            player.sendMessage("そのコース又はそのコースの記録は存在しません。");
            return true;
        }

        if(showAbove && isDetail) {
            player.sendMessage("aboveオプションとdetailオプションの両立はできません。aboveを優先します。");
            isDetail = false; //aboveが入力されているときはdetailが入力されていないものとする。
        }

        if(displayCount > 100) {
            player.sendMessage("表示可能件数は最大100です。");
            displayCount = 100;
        }

        if(displayCount == 0) {
            player.sendMessage("表示件数は1以上100以下の半角数字で指定してください。");
            displayCount = 10;
        }

        UUID targetUuid = targetPlayerId == null || player.getName().equals(targetPlayerId) ?
                player.getUniqueId() : Bukkit.getOfflinePlayer(targetPlayerId).getUniqueId();
        String targetName = targetPlayerId == null || player.getName().equals(targetPlayerId) ? "あなた" : targetPlayerId + "さん";

        if(isDetail){
            int sumTick = showTargetRank(player, courseName, targetUuid, targetName, allowDup);
            if(sumTick < 0) return true;
            else showDetail(player, courseName, targetUuid, sumTick);

            return true;
        }

        if(showAbove){
            showAboveRanks(player, courseName, targetUuid, targetName, allowDup, displayCount);
            return true;
        }

        if (targetPlayerId != null) {
            showTargetRank(player, courseName, targetUuid, targetName, allowDup);
            return true;
        }

        else {
            showRanks(player, courseName, 1, allowDup, displayCount);
            return true;
        }
    }

    /**
     * ランキングを表示
     *
     * @param sender 表示先のプレイヤー
     * @param courseName コース名
     * @param startRank 何位から表示するか
     * @param allowDup 同一プレイヤーの複数記録を含めるか
     * @param count 表示件数
     */
    private void showRanks(Player sender, String courseName, int startRank, boolean allowDup, int count) {
        List<Map.Entry<String, String>> recordList = allowDup ?
                recordDao.getTopRecordDup(courseName, startRank, count) : recordDao.getTopRecordNoDup(courseName, startRank, count);

        int rank = startRank;
        int displayRank = rank;
        int previousTick = -1;

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "----------ランキング----------");
        for (Map.Entry<String, String> entry : recordList) {
            int tick = Integer.parseInt(entry.getValue());
            if(tick != previousTick) displayRank = rank;

            String time = GameUtils.tickToTime(tick);
            String line = String.format(ChatColor.GREEN + "%2d. %-16s %10s %6d" + "ticks", displayRank, entry.getKey(), time, tick);
            sender.sendMessage(line);

            previousTick = tick;
            rank++;
        }
    }

    /**
     * ある基準プレイヤーより上位数件のランキングを表示。
     *
     * @param sender 表示先のプレイヤー
     * @param courseName コース名
     * @param targetUuid 基準とするプレイヤーのUUID
     * @param targetName 基準とするプレイヤーの表示名(String) 〇〇さん、あなた等
     * @param allowDup 同一プレイヤーの複数記録を含めるか
     * @param count 表示件数
     */
    private void showAboveRanks(Player sender, String courseName, UUID targetUuid, String targetName, boolean allowDup, int count) {
        Map.Entry<Integer, Integer> rankAndTime = allowDup ?
                recordDao.getRankAndRecordDup(targetUuid, courseName) : recordDao.getRankAndRecordNoDup(targetUuid, courseName);

        if (rankAndTime == null) {
            sender.sendMessage(targetName + "の記録は存在しません。");
            return;
        }

        int rank = rankAndTime.getKey();
        int startRank = Math.max(1, rank - count);
        int fetchCount = rank - startRank + 1;
        if(fetchCount == 1){
            sender.sendMessage(targetName + "は1位です。");
            return;
        }

        showRanks(sender, courseName, startRank, allowDup, fetchCount);
    }

    /**
     * あるプレイヤーの順位を表示する。
     *
     * @param sender 表示先のプレイヤー
     * @param courseName コース名
     * @param targetUuid 基準とするプレイヤーのUUID
     * @param targetName 基準とするプレイヤーの表示名(String) 〇〇さん、あなた等
     * @param allowDup 同一プレイヤーの複数記録を含めるか
     * @return タイム
     */
    private int showTargetRank(Player sender, String courseName, UUID targetUuid, String targetName, boolean allowDup) {
        Map.Entry<Integer, Integer> rankAndTime = allowDup ?
                recordDao.getRankAndRecordDup(targetUuid, courseName) : recordDao.getRankAndRecordNoDup(targetUuid, courseName);

        if (rankAndTime == null) {
            sender.sendMessage(targetName + "の記録は存在しません。");
            return -1;
        }

        int rank = Objects.requireNonNull(rankAndTime).getKey();
        int timeRecord = rankAndTime.getValue();
        String formattedTime = GameUtils.tickToTime(timeRecord);
        sender.sendMessage(ChatColor.GREEN +"プレイヤー:" + Bukkit.getPlayer(targetUuid).getName() +
                "     順位:" + rank + "位" + "     ベスト記録:" + formattedTime);
        return timeRecord;
    }

    /**
     * 中継地点の通過タイムを表示する
     *
     * @param sender 表示先のプレイヤー
     * @param courseName コース名
     * @param targetUuid 詳細を表示するプレイヤーのUUID
     * @param sumTick 最終クリアタイム
     */
    private void showDetail(Player sender, String courseName, UUID targetUuid, int sumTick) {
        Map<String, Integer> viaPointRecord = recordDao.getViaPointRecord(targetUuid, courseName);
        if(viaPointRecord.isEmpty()) sender.sendMessage("このコースの詳細情報はありません。"); //中継地点がないとき。

        int prevTick = 0;
        int tick;
        int lapTime;

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "中継地点名:通過タイム  lap:ラップタイム");
        for(Map.Entry<String, Integer> entry : viaPointRecord.entrySet()){
            String viaPointName = entry.getKey();
            tick = entry.getValue();

            lapTime = tick - prevTick;
            prevTick = tick;

            sender.sendMessage(ChatColor.GREEN + viaPointName + ":" + GameUtils.tickToTime(tick) + "(" + tick + "ticks)   lap:" + GameUtils.tickToTime(lapTime) + "(" + lapTime + "ticks)");
        }

        lapTime = sumTick - prevTick;
        sender.sendMessage(ChatColor.GOLD + "ゴール:" + GameUtils.tickToTime(sumTick) + "(" + sumTick + "ticks)   lap:" + GameUtils.tickToTime(lapTime) + "(" + lapTime + "ticks)");
    }
}
