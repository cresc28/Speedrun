package com.github.cresc28.speedrun.command;

import com.github.cresc28.speedrun.manager.CheckpointManager;
import com.github.cresc28.speedrun.utils.MessageUtils;
import com.github.cresc28.speedrun.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * /cpコマンドの実装クラス。
 */
public class CheckpointCommand implements CommandExecutor, TabCompleter {
    /**
    * Tab補完の処理を行う。
    */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        Player player = (Player) sender;

        if (!command.getName().equalsIgnoreCase("cp")) return null;

        if (args.length == 1) {
            List<String> options = Arrays.asList("tp", "remove", "list", "allowCrossWorldTp");
            for (String option : options) {
                if (option.startsWith(args[0].toLowerCase())) completions.add(option);
            }
        }

        else if(args.length == 2) {
            if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("tp")) {
                Utils.completionFromMap(CheckpointManager.getCheckpointNames(player.getUniqueId(), player.getLocation()), args[1].toLowerCase(), completions);
            }

            else if(args[0].equalsIgnoreCase("allowCrossWorldTp")){
                List<String> options = Arrays.asList("true", "false");
                for (String option : options) {
                    if (option.startsWith(args[1].toLowerCase())) completions.add(option);
                }
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
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation();

        if (!command.getName().equalsIgnoreCase("cp")) return false;

        if(args.length == 0){
            CheckpointManager.registerCheckpoint(uuid, loc);
            sender.sendMessage("チェックポイントを設定しました。");
            return true;
        }

        else if(args.length == 1){
            if(args[0].equalsIgnoreCase("list")){
                MessageUtils.displayMap(CheckpointManager.getCheckpointNames(uuid, loc), sender, "チェックポイント");
            }

            else{
                CheckpointManager.registerCheckpoint(uuid, loc, args[0]);
                sender.sendMessage("チェックポイントを設定しました。");
            }

            return true;
        }

        if(args.length == 2){
            if(args[0].equalsIgnoreCase("remove")){
                boolean removed = CheckpointManager.removeCheckpoint(uuid, loc, args[1]);
                MessageUtils.sendRemoveMessage(sender, removed, args[1], "チェックポイント");
                return true;
            }
            else if(args[0].equalsIgnoreCase("tp")){
                if(!CheckpointManager.selectCheckpoint(uuid, loc, args[1])) {
                    sender.sendMessage(ChatColor.RED + "指定された名前のチェックポイントは存在しません。");
                }

                else{
                    player.teleport(CheckpointManager.getGlobalRecentCpLocation(uuid));
                }

                return true;
            }

            else if(args[0].equalsIgnoreCase("allowCrossWorldTp")){
                if(args[1].equalsIgnoreCase("true")){
                    CheckpointManager.setCrossWorldTpAllowed(Boolean.parseBoolean(args[1]));
                    sender.sendMessage("他ワールドへのCPでの移動を許可しました。");
                }

                else if(args[1].equalsIgnoreCase("false")){
                    CheckpointManager.setCrossWorldTpAllowed(Boolean.parseBoolean(args[1]));
                    sender.sendMessage("他ワールドへのCPでの移動を禁止しました。");
                }

                else sender.sendMessage("/cp arrowCrossWorldTp true/false");
                return true;
            }
        }
        return false;
    }
}
