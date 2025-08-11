package com.github.cresc28.speedrun.command;

import com.github.cresc28.speedrun.data.RecordInfo;
import com.github.cresc28.speedrun.data.RecordSession;
import com.github.cresc28.speedrun.data.SpeedrunParameters;
import com.github.cresc28.speedrun.db.record.RecordDao;
import com.github.cresc28.speedrun.gui.RecordMenuForDelete;
import com.github.cresc28.speedrun.manager.CourseManager;
import com.github.cresc28.speedrun.utils.GameUtils;
import com.github.cresc28.speedrun.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class RecordCommand implements CommandExecutor, TabCompleter {
    private final CourseManager courseManager;
    private final RecordDao recordDao;
    private final RecordSession recordSession;

    public RecordCommand(SpeedrunParameters p){
        courseManager = p.getCourseManager();
        recordDao = p.getRecordDao();
        recordSession = p.getRecordSession();
    }
    /**
     * TAB補完の実装。
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
            if("add".equalsIgnoreCase(args[0]) || "remove".equalsIgnoreCase(args[0]) || "removeAll".equalsIgnoreCase(args[0])){
                TextUtils.completionExcludeViaPoint(courseManager.getAllCourseName(), args[1].toLowerCase(), completions);
            }
        }

        if(args.length == 3){
            if("remove".equalsIgnoreCase(args[0]) || "removeAll".equalsIgnoreCase(args[0])) {
                List<String> playerNames = Arrays.stream(Bukkit.getOfflinePlayers())
                        .map(OfflinePlayer::getName)
                        .collect(Collectors.toList());

                TextUtils.completionFromCollection(playerNames, args[1].toLowerCase(), completions);
            }
        }

        return completions;
    }

    /**
     * コマンドの実装。
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) return false;

        String sub = args[0].toLowerCase();
        UUID deleteUuid;

        if(sender instanceof ConsoleCommandSender){ //コンソールからのコマンド処理
            switch(sub){
                case "removecourseall":
                    if(args.length != 2) return false;

                    if(recordDao.delete(args[1])) sender.sendMessage("削除に成功しました。");
                    else sender.sendMessage("その名前のコースデータは存在しません。");

                    return true;

                case "removeplayerall":
                    if(args.length != 2) return false;
                    deleteUuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();

                    if(recordDao.delete(deleteUuid)) sender.sendMessage("削除に成功しました。");
                    else sender.sendMessage("そのプレイヤーの記録は存在しません。");

                    return true;

                default: return false;
            }
        }

        switch(sub){ //ゲーム内からのコマンド処理
            case "add":
                if(args.length != 4){
                    sender.sendMessage("/record add <コース名> <プレイヤー名> <タイム(tick表記)>");
                    return true;
                }
                if(!TextUtils.isPositiveInteger(args[3])){
                    sender.sendMessage("タイムはtick表記の非負の整数で入力してください。");
                    return true;
                }

                UUID uuidToAdd = Bukkit.getOfflinePlayer(args[2]).getUniqueId();
                int tick = Integer.parseInt(args[3]);
                recordDao.insert(uuidToAdd, args[1], tick);
                sender.sendMessage(String.format("登録完了(コース名:%s  プレイヤー:%s   タイム:%s)", args[1], args[2], GameUtils.tickToTime(tick)));
                return true;

            case "remove":
                if(args.length != 3) {
                    sender.sendMessage("/record remove <コース名> <プレイヤー名>");
                    return true;
                }

                deleteUuid = Bukkit.getOfflinePlayer(args[2]).getUniqueId();
                UUID senderUuid = ((Player) sender).getUniqueId();

                List<Integer> times = recordDao.getFinishTick(deleteUuid, args[1]);
                if(times.isEmpty()){
                    sender.sendMessage("コース、プレイヤー、記録のいずれかが存在しません。");
                    return true;
                }
                List<String> timesString = times.stream().map(GameUtils::tickToTime).collect(Collectors.toList());
                recordSession.setRecord(senderUuid, new RecordInfo(args[1], deleteUuid, timesString));
                ((Player)sender).addScoreboardTag("MenuOpen");
                new RecordMenuForDelete((Player) sender, timesString,0).openInventory();
                return true;

            case "removeall":
                if(args.length != 3) {
                    sender.sendMessage("/record removeAll <コース名> <プレイヤー名>");
                    return true;
                }
                deleteUuid = Bukkit.getOfflinePlayer(args[2]).getUniqueId();

                if(recordDao.delete(deleteUuid, args[1])) sender.sendMessage("削除に成功しました。");
                else sender.sendMessage("コース、プレイヤー、記録のいずれかが存在しません。");

                return true;

            case "removecourseall":

            case "removeplayerall":
                sender.sendMessage("/record <removePlayerAll|removeCourseAll>はサーバコンソールでのみ実行可能です。");
                return true;

            default: return false;
        }
    }
}
