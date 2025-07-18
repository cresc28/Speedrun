package com.github.cresc28.speedrun.command;

import com.github.cresc28.speedrun.db.course.RecordDao;
import com.github.cresc28.speedrun.manager.CourseManager;
import com.github.cresc28.speedrun.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

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

        if (args.length == 1) {
            List<String> options = Arrays.asList("add", "remove", "removeAll", "removePlayerAll", "removeCourseAll");
            for (String option : options) {
                if (option.startsWith(args[0].toLowerCase())) completions.add(option);
            }
        }

        if(args.length == 2){
            if("remove".equalsIgnoreCase(args[1]) || "removeAll".equalsIgnoreCase(args[1])){
                Utils.completionExcludeViaPoint(courseManager.getAllCourseName(), args[1].toLowerCase(), completions);
            }
        }

        if(args.length == 3){
            if("remove".equalsIgnoreCase(args[1]) || "removeAll".equalsIgnoreCase(args[1])) {
                List<String> playerNames = Arrays.stream(Bukkit.getOfflinePlayers())
                        .map(OfflinePlayer::getName)
                        .collect(Collectors.toList());

                Utils.completionExcludeViaPoint(playerNames, args[1].toLowerCase(), completions);
            }
        }

        return completions;
    }

    /**
     * コマンドの実行処理を行う。
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) return false;

        if("removeCourseAll".equalsIgnoreCase(args[0]) || "removePlayerAll".equalsIgnoreCase(args[0])){
            if(!(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage("/record <removePlayerAll|removeCourseAll>はサーバコンソールでのみ実行可能です。");
                return true;
            }
        }

        if(args.length == 2){
            if("removeCourseAll".equalsIgnoreCase(args[0])){
                if(recordDao.delete(args[1])){
                    sender.sendMessage("削除に成功しました。");
                }
                else {
                    sender.sendMessage("その名前のコースデータは存在しません。");
                }
                return true;
            }

            else if("removePlayerAll".equalsIgnoreCase(args[0])){
                UUID deleteUuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                if(recordDao.delete(deleteUuid)){
                    sender.sendMessage("削除に成功しました。");
                }
                else {
                    sender.sendMessage("そのプレイヤーの記録は存在しません。");
                }
                return true;
            }
        }

        if (args.length == 3) {
            if("add".equalsIgnoreCase(args[0])){
                sender.sendMessage("/record add <コース名> <プレイヤー名> <タイム(tick表記)>");
                return true;
            }

            if("remove".equalsIgnoreCase(args[0])){

            }

            else if("removeAll".equalsIgnoreCase(args[0])){
                UUID deleteUuid = Bukkit.getOfflinePlayer(args[2]).getUniqueId();
                if(!recordDao.delete(deleteUuid, args[1])){
                    sender.sendMessage("コース、プレイヤー、記録のいずれかが存在しません。");
                }
                else {
                    sender.sendMessage("削除に成功しました。");
                }
                return true;
            }
        }

        if (args.length == 4) {
            if("remove".equalsIgnoreCase(args[0])){
                sender.sendMessage("/record remove <コース名> <プレイヤー名>");
                return true;
            }

            else if("removeAll".equalsIgnoreCase(args[0])){
                sender.sendMessage("/record removeAll [コース名] [プレイヤー名]");
                return true;
            }

            if(!"add".equalsIgnoreCase(args[0])) return false;

            if(!Utils.isPositiveInteger(args[3])){
                sender.sendMessage("タイムはtick表記の非負の整数で入力してください。");
                return true;
            }

            UUID uuidToAdd = Bukkit.getOfflinePlayer(args[2]).getUniqueId();
            int tick = Integer.parseInt(args[3]);
            recordDao.insert(uuidToAdd, args[1], tick);
            sender.sendMessage(String.format("登録完了(コース名:%s  プレイヤー:%s   タイム:%s)", args[1], args[2], Utils.tickToTime(tick)));
            return true;
        }

        return false;
    }
}
