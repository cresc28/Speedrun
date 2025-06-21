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
 * /cp コマンドの実装クラス。
 */
public class CheckpointCommand implements CommandExecutor, TabCompleter {
    /**
    * Tab補完の処理を行う。
    */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        UUID senderUuid = ((Player) sender).getUniqueId();

        if (!command.getName().equalsIgnoreCase("cp")) return null;

        if (args.length == 1) {
            List<String> options = Arrays.asList("tp", "remove", "list", "allowCrossWorldTp");
            for (String option : options) {
                if (option.startsWith(args[0].toLowerCase())) completions.add(option);
            }
        }

        else if(args.length == 2) {
            if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("tp")) {
                Utils.completionFromMap(CheckpointManager.getCheckpointNames((Player) sender), args[1].toLowerCase(), completions);
            }

            else if(args[0].equalsIgnoreCase("allowCrossWorldTp")){
                List<String> options = Arrays.asList("true", "false");
                for (String option : options) {
                    if (option.startsWith(args[0].toLowerCase())) completions.add(option);
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
        UUID senderUuid = player.getUniqueId();
        Location loc = player.getLocation();

        if (!command.getName().equalsIgnoreCase("cp")) return false;

        if(args.length == 0){
            CheckpointManager.registerCheckpoint(player);
            return true;
        }

        else if(args.length == 1){
            if(args[0].equalsIgnoreCase("list")){
                MessageUtils.displayMap(CheckpointManager.getCheckpointNames(player), sender, "チェックポイント");
            }

            else{
                CheckpointManager.registerCheckpoint(player, args[0]);
            }

            return true;
        }

        if(args.length == 2){
            if(args[0].equalsIgnoreCase("remove")){
                boolean removed = CheckpointManager.removeCheckpoint(player, args[1]);
                MessageUtils.sendRemoveMessage(sender, removed, args[1], "チェックポイント");
                return true;
            }
            else if(args[0].equalsIgnoreCase("tp")){
                if(!CheckpointManager.selectCheckpoint(player, args[1])) {
                    sender.sendMessage(ChatColor.RED + "指定された名前のチェックポイントは存在しません。");
                }

                else{
                    player.teleport(CheckpointManager.getCurrentGlobalCpLocation(senderUuid));
                }

                return true;
            }

            else if(args[0].equalsIgnoreCase("allowCrossWorldTp")){
                if(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false")){
                    CheckpointManager.setCrossWorldTpAllowed(Boolean.parseBoolean(args[1]));
                }

                else sender.sendMessage("/cp arrowCrossWorldTp true/false");
                return true;
            }
        }
        return false;
    }
}
