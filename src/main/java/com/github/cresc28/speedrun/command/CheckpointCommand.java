package com.github.cresc28.speedrun.command;

import com.github.cresc28.speedrun.config.ConfigManager;
import com.github.cresc28.speedrun.data.SpeedrunParameters;
import com.github.cresc28.speedrun.manager.CheckpointManager;
import com.github.cresc28.speedrun.utils.MessageUtils;
import com.github.cresc28.speedrun.utils.TextUtils;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * /cpコマンドの実装クラス。
 */
public class CheckpointCommand implements CommandExecutor, TabCompleter {
    private final CheckpointManager cpManager;

    public CheckpointCommand(SpeedrunParameters p) {
        cpManager = p.getCpManager();
    }

    /**
    * Tab補完の処理を行う。
    */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        Player player = (Player) sender;

        if (args.length == 1) {
            List<String> options = Arrays.asList("tp", "remove", "list", "allowCrossWorldTp", "deleteCpOnStart");
            for (String option : options) {
                if (option.startsWith(args[0].toLowerCase())) completions.add(option);
            }
        }

        else if(args.length == 2) {
            if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("tp")) {
                TextUtils.completionFromCollection(cpManager.getCheckpointNames(player.getUniqueId(), player.getLocation().getWorld()), args[1].toLowerCase(), completions);
            }

            else if (args[0].equalsIgnoreCase("allowCrossWorldTp") || args[0].equalsIgnoreCase("deleteCpOnStart")){
                List<String> options = Arrays.asList("true", "false");
                for (String option : options) {
                    if (option.startsWith(args[1].toLowerCase())) completions.add(option);
                }
            }

            else if (args[0].equalsIgnoreCase("list")){
                List<String> worldNames = new ArrayList<>();
                for (World world : Bukkit.getWorlds()) {
                    worldNames.add(world.getName());
                }
                TextUtils.sortCollection(worldNames);
                TextUtils.completionFromCollection(worldNames, args[1].toLowerCase(), completions);
            }
        }

        return completions;
    }

    /**
    * コマンドの実行処理を行う。
    */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if(sender instanceof ConsoleCommandSender){
            sender.sendMessage("このコマンドはサーバコンソールでは実行できません。");
            return true;
        }

        if(player.getGameMode() != GameMode.CREATIVE && !player.isOp()){
            sender.sendMessage("OP権限不所持の場合、このコマンドはクリエイティブモードでのみ実行可能です。");
            return true;
        }

        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation();

        if(args.length == 0){
            cpManager.registerCheckpoint(uuid, loc);
            sender.sendMessage("チェックポイントを設定しました。");
            return true;
        }

        else if(args.length == 1){
            if(args[0].equalsIgnoreCase("list")){
                MessageUtils.displayMap(cpManager.getCheckpointNames(uuid, loc.getWorld()), sender, "チェックポイント");
            }

            else{
                cpManager.registerCheckpoint(uuid, loc, args[0]);
                sender.sendMessage("チェックポイントを設定しました。");
            }

            return true;
        }

        if(args.length == 2){
            if(args[0].equalsIgnoreCase("remove")){
                boolean removed = cpManager.removeCheckpoint(uuid, loc.getWorld(), args[1]);
                MessageUtils.sendRemoveMessage(sender, removed, args[1], "チェックポイント");
                return true;
            }
            else if(args[0].equalsIgnoreCase("tp")){
                if(!cpManager.selectCheckpoint(uuid, loc.getWorld(), args[1])) {
                    sender.sendMessage(ChatColor.RED + "指定された名前のチェックポイントは存在しません。");
                }

                else{
                    player.teleport(cpManager.getGlobalRecentCpLocation(uuid));
                }

                return true;
            }

            else if(args[0].equalsIgnoreCase("list")) {
                World world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    sender.sendMessage("§指定されたワールドは存在しません。");
                    return true;
                }
                MessageUtils.displayMap(cpManager.getCheckpointNames(uuid, world), sender, "チェックポイント");
            }

            else if(args[0].equalsIgnoreCase("allowCrossWorldTp")){
                if(args[1].equalsIgnoreCase("true")){
                    ConfigManager.setCrossWorldTpAllowed(Boolean.parseBoolean(args[1]));
                    sender.sendMessage("他ワールドへのCPでの移動を許可しました。");
                }

                else if(args[1].equalsIgnoreCase("false")){
                    ConfigManager.setCrossWorldTpAllowed(Boolean.parseBoolean(args[1]));
                    sender.sendMessage("他ワールドへのCPでの移動を禁止しました。");
                }

                else sender.sendMessage("/cp arrowCrossWorldTp <true|false>");
                return true;
            }

            else if(args[0].equalsIgnoreCase("deleteCpOnStart")){
                if(args[1].equalsIgnoreCase("true")){
                    ConfigManager.setDeleteCpOnStart(Boolean.parseBoolean(args[1]));
                    sender.sendMessage("計測開始時にそのコースのCPが削除されるように変更しました。");
                }

                else if(args[1].equalsIgnoreCase("false")){
                    ConfigManager.setDeleteCpOnStart(Boolean.parseBoolean(args[1]));
                    sender.sendMessage("計測開始時にそのコースのCPが削除されないように変更しました。");
                }

                else sender.sendMessage("/cp deleteCpOnStart <true|false>");
                return true;
            }
        }
        return false;
    }
}
