package com.github.cresc28.speedrun.command;

import com.github.cresc28.speedrun.core.manager.CourseManager;
import com.github.cresc28.speedrun.db.course.RecordDao;
import com.github.cresc28.speedrun.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
            completions.addAll(Arrays.asList("dup", "detail", "above", player.getDisplayName()));
        }
        return completions;
    }

    /**
     * コマンドの実行処理を行う。
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation();

        if (args.length < 1) {
            return false;
        }

        String courseName = args[0];
        String targetPlayer = null; //プレイヤー名
        boolean allowDup = false; //同じプレイヤーの複数記録を表示するか
        boolean isDetail = false; //詳細を表示するか
        boolean showAbove = false; //自分より上の10位表示モード
        int displayCount = 10; //何位まで表示するか


        for (int i = 1; i < args.length; i++) {
            String arg = args[i].toLowerCase();

            if ("dup".equalsIgnoreCase(arg)) {
                allowDup = true;
                continue;
            }

            else if ("detail".equalsIgnoreCase(arg)) {
                if(targetPlayer == null) targetPlayer = player.getDisplayName();
                isDetail = true;
                continue;
            }

            else if ("above".equalsIgnoreCase(arg)) {
                showAbove = true;
                continue;
            }

            if(Utils.isNumeric(arg)) {
                displayCount = Integer.parseInt(arg);
                continue;
            }

            else { //dup, detail, aboveというプレイヤーが存在する状況は可能性として極めて低いため無視。
                targetPlayer = arg;
            }
        }

        if(showAbove && isDetail) {
            player.sendMessage("aboveオプションとdetailオプションの両立はできません。aboveを優先します。");
            isDetail = false; //aboveが入力されているときはdetailが入力されていないものとする。
        }

        if(allowDup && isDetail){
            player.sendMessage("dupオプションとdetailオプションの両立はできません。detailオプションを優先します。");
            allowDup = false;
        }

        if(displayCount > 30) {
            player.sendMessage("表示可能件数は最大30です。");
            displayCount = 30;
        }

        //detailが指定されていても中継地点がない場合はこのコースではdetailは無意味です　と返す
        //

        if(targetPlayer != null){
            int rank;
            String timeRecord;
            UUID targetUuid;
            String targetName;

            if (targetPlayer.equals(player.getName())) {
                targetUuid = player.getUniqueId();
                targetName = "あなた";
            }

            else {
                targetUuid = Bukkit.getOfflinePlayer(targetPlayer).getUniqueId();;
                targetName = targetPlayer + "さん";
            }

            Map.Entry<Integer, String> rankAndTime = allowDup ? recordDao.getRankAndRecordDup(targetUuid, courseName) : recordDao.getRankAndRecordNoDup(targetUuid, courseName);


            if(rankAndTime == null){
                player.sendMessage(targetName + "の記録は存在しません。");
                return true;
            }

            else {
                rank = rankAndTime.getKey();
                timeRecord = rankAndTime.getValue();
                String formatedTime = Utils.tickToTime(Integer.parseInt(timeRecord));
                player.sendMessage(targetName + "の記録は" + formatedTime + "です。" + "(順位:" + rank + "位)");
            }

            return true;
        }

        if(!showAbove){
            if(allowDup){
                List<Map.Entry<String, String>> recordList = recordDao.getTopRecordDup(courseName, displayCount);
                showRanking(recordList, player, 1);
            }

            else {
                List<Map.Entry<String, String>> recordList = recordDao.getTopRecordNoDup(courseName, displayCount);
                showRanking(recordList, player, 1);
            }
            return true;
        }

        return false;
    }

    private void showRanking(List<Map.Entry<String, String>> recordList, Player sender, int startRank) {
        int rank = startRank;

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "----------ランキング----------");
        for (Map.Entry<String, String> entry : recordList) {
            int tick = Integer.parseInt(entry.getValue());
            String time = Utils.tickToTime(tick);
            String line = String.format(ChatColor.GREEN + "%2d. %-16s %10s %6d" + "ticks", rank, entry.getKey(), time, tick);
            sender.sendMessage(line);
            rank++;
        }
    }
}
