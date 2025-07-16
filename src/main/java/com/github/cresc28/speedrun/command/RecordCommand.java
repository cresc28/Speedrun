package com.github.cresc28.speedrun.command;

import com.github.cresc28.speedrun.core.manager.CourseManager;
import com.github.cresc28.speedrun.db.course.RecordDao;
import com.github.cresc28.speedrun.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class RecordCommand implements CommandExecutor, TabCompleter {
    private final CourseManager courseManager;
    private final RecordDao recordDao;

    public RecordCommand(CourseManager courseManager, RecordDao recordDao){
        this.courseManager = courseManager;
        this.recordDao = recordDao;
    }
    /**
     * Tab補完の処理を行う。
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        Player player = (Player) sender;

        if (args.length == 1) {
            Utils.completionExcludeViaPoint(courseManager.getAllCourseName(), args[0].toLowerCase(), completions);
        }

        else {
            completions.addAll(Arrays.asList("dup", "detail", "above", player.getName()));
        }
        return completions;
    }

    /**
     * コマンドの実行処理を行う。
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
                    if (Utils.isNumeric(arg)) displayCount = Integer.parseInt(arg); //dup, detail, aboveというプレイヤーが存在する可能性は極めて低いため無視。
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

        if(allowDup && isDetail){
            player.sendMessage("dupオプションとdetailオプションの両立はできません。detailオプションを優先します。");
            allowDup = false;
        }

        if(displayCount > 100) {
            player.sendMessage("表示可能件数は最大100です。");
            displayCount = 100;
        }

        UUID targetUuid = targetPlayerId == null || player.getName().equals(targetPlayerId) ?
                player.getUniqueId() : Bukkit.getOfflinePlayer(targetPlayerId).getUniqueId();
        String targetName = targetPlayerId == null || player.getName().equals(targetPlayerId) ? "あなた" : targetPlayerId + "さん";

        //detailが指定されていても中継地点がない場合はこのコースではdetailは無意味です　と返す

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
            String time = Utils.tickToTime(tick);
            String line = String.format(ChatColor.GREEN + "%2d. %-16s %10s %6d" + "ticks", rank, entry.getKey(), time, tick);
            player.sendMessage(line);
            rank++;
        }
    }

    private void showAboveRanking(Player player, String courseName, UUID uuid, String targetName, boolean allowDup, int count) {
        Map.Entry<Integer, String> rankAndTime = allowDup ?
                recordDao.getRankAndRecordDup(uuid, courseName, true) : recordDao.getRankAndRecordNoDup(uuid, courseName, true);

        if (rankAndTime == null) {
            player.sendMessage(targetName + "の記録は存在しません。");
            return;
        }

        int rank = Objects.requireNonNull(rankAndTime).getKey();
        int startRank = Math.max(1, rank - count);
        int fetchCount = rank - startRank + 1;
        if(fetchCount == 1){
            player.sendMessage(targetName + "は1位です。");
            return;
        }

        List<Map.Entry<String, String>> recordList = allowDup ? recordDao.getTopRecordDup(courseName, startRank, fetchCount) : recordDao.getTopRecordNoDup(courseName, startRank, fetchCount);
        showRanking(recordList, player, startRank);
    }

    private void showTargetRank(Player player, String courseName, UUID uuid, String targetName, boolean allowDup) {
        Map.Entry<Integer, String> rankAndTime = allowDup ?
                recordDao.getRankAndRecordDup(uuid, courseName, false) : recordDao.getRankAndRecordNoDup(uuid, courseName, false);

        if (rankAndTime == null) {
            player.sendMessage(targetName + "の記録は存在しません。");
            return;
        }

        int rank = Objects.requireNonNull(rankAndTime).getKey();
        String timeRecord = rankAndTime.getValue();
        String formattedTime = Utils.tickToTime(Integer.parseInt(timeRecord));
        player.sendMessage(targetName + "のベスト記録は" + formattedTime + "です。(順位:" + rank + "位)");
    }
}
