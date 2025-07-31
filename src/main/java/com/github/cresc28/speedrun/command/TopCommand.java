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
     * Tab補完の処理を行う。
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
     * コマンドの実行処理を行う。
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof ConsoleCommandSender){
            sender.sendMessage("このコマンドはサーバコンソールでは実行できません。");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            return false;
        }

        String courseName = args[0];
        String targetPlayerId = null; //プレイヤー名
        boolean allowDup = false; //同じプレイヤーの複数記録を表示するか
        boolean isDetail = false; //詳細を表示するか
        boolean showAbove = false; //自分より上の10位表示モード
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

        UUID targetUuid = targetPlayerId == null || player.getName().equals(targetPlayerId) ?
                player.getUniqueId() : Bukkit.getOfflinePlayer(targetPlayerId).getUniqueId();
        String targetName = targetPlayerId == null || player.getName().equals(targetPlayerId) ? "あなた" : targetPlayerId + "さん";

        //detailが指定されていても中継地点がない場合はこのコースではdetailは無意味です　と返す
        if(isDetail){
            int sumTick = showTargetRank(player, courseName, targetUuid, targetName, allowDup);
            if(sumTick < 0) return true;
            Map<String, Integer> viaPointRecord = recordDao.getViaPointRecord(targetUuid, courseName);

            if(viaPointRecord.isEmpty()) sender.sendMessage("このコースの詳細情報はありません。");
            else showDetail(player, viaPointRecord, sumTick);

            return true;
        }

        if(showAbove){
            showAboveRanking(player, courseName, targetUuid, targetName, allowDup, displayCount);
            return true;
        }

        if (targetPlayerId != null) {
            showTargetRank(player, courseName, targetUuid, targetName, allowDup);
            return true;
        }

        else {
            List<Map.Entry<String, String>> recordList = allowDup ?
                    recordDao.getTopRecordDup(courseName, 1, displayCount) : recordDao.getTopRecordNoDup(courseName, 1, displayCount);
            showRanking(recordList, player, 1);
            return true;
        }
    }

    private void showRanking(List<Map.Entry<String, String>> recordList, Player player, int startRank) {
        int rank = startRank;

        player.sendMessage(ChatColor.LIGHT_PURPLE + "----------ランキング----------");
        for (Map.Entry<String, String> entry : recordList) {
            int tick = Integer.parseInt(entry.getValue());
            String time = GameUtils.tickToTime(tick);
            String line = String.format(ChatColor.GREEN + "%2d. %-16s %10s %6d" + "ticks", rank, entry.getKey(), time, tick);
            player.sendMessage(line);
            rank++;
        }
    }

    private void showAboveRanking(Player player, String courseName, UUID targetUuid, String targetName, boolean allowDup, int count) {
        Map.Entry<Integer, Integer> rankAndTime = allowDup ?
                recordDao.getRankAndRecordDup(targetUuid, courseName, true) : recordDao.getRankAndRecordNoDup(targetUuid, courseName, true);

        if (rankAndTime == null) {
            player.sendMessage(targetName + "の記録は存在しません。");
            return;
        }

        int rank = rankAndTime.getKey();
        int startRank = Math.max(1, rank - count);
        int fetchCount = rank - startRank + 1;
        if(fetchCount == 1){
            player.sendMessage(targetName + "は1位です。");
            return;
        }

        List<Map.Entry<String, String>> recordList = allowDup ? recordDao.getTopRecordDup(courseName, startRank, fetchCount) : recordDao.getTopRecordNoDup(courseName, startRank, fetchCount);
        showRanking(recordList, player, startRank);
    }

    private int showTargetRank(Player player, String courseName, UUID targetUuid, String targetName, boolean allowDup) {
        Map.Entry<Integer, Integer> rankAndTime = allowDup ?
                recordDao.getRankAndRecordDup(targetUuid, courseName, false) : recordDao.getRankAndRecordNoDup(targetUuid, courseName, false);

        if (rankAndTime == null) {
            player.sendMessage(targetName + "の記録は存在しません。");
            return -1;
        }

        int rank = Objects.requireNonNull(rankAndTime).getKey();
        int timeRecord = rankAndTime.getValue();
        String formattedTime = GameUtils.tickToTime(timeRecord);
        player.sendMessage(ChatColor.GREEN +"プレイヤー:" + Bukkit.getPlayer(targetUuid).getName() +
                "     順位:" + rank + "位" + "     ベスト記録:" + formattedTime);
        return timeRecord;
    }

    private void showDetail(Player player, Map<String, Integer> viaPointRecord, int sumTick) {
        int prevTick = 0;
        int tick;
        int lapTime;

        player.sendMessage(ChatColor.LIGHT_PURPLE + "中継地点名:通過タイム  lap:ラップタイム");
        for(Map.Entry<String, Integer> entry : viaPointRecord.entrySet()){
            String viaPointName = entry.getKey();
            tick = entry.getValue();

            lapTime = tick - prevTick;
            prevTick = tick;

            player.sendMessage(ChatColor.GREEN + viaPointName + ":" + GameUtils.tickToTime(tick) + "(" + tick + "ticks)   lap:" + GameUtils.tickToTime(lapTime) + "(" + lapTime + "ticks)");
        }

        lapTime = sumTick - prevTick;
        player.sendMessage(ChatColor.GOLD + "ゴール:" + GameUtils.tickToTime(sumTick) + "(" + sumTick + "ticks)   lap:" + GameUtils.tickToTime(lapTime) + "(" + lapTime + "ticks)");
    }
}
