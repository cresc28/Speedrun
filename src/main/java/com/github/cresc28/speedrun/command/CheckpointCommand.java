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

public class CheckpointCommand implements CommandExecutor, TabCompleter {
    //TAB補完
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        UUID senderUuid = ((Player) sender).getUniqueId();

        if (!command.getName().equalsIgnoreCase("cp")) return null;

        if (args.length == 1) {
            List<String> options = Arrays.asList("remove", "list");
            for (String option : options) {
                if (option.startsWith(args[0].toLowerCase())) completions.add(option);
            }
        }

        else if(args.length == 2) {
            if ("remove".equalsIgnoreCase(args[0])) {
                Utils.completionFromMap(CheckpointManager.getCheckpointNames(senderUuid), args[1].toLowerCase(), completions);
            }
        }

        return completions;
    }

    //コマンドの受け取り
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        UUID senderUuid = player.getUniqueId();
        Location loc = player.getLocation();

        if (!command.getName().equalsIgnoreCase("cp")) return false;

        if(args.length == 0){
            CheckpointManager.registerCheckpoint(senderUuid, loc);
            return true;
        }

        else if(args.length == 1){
            if(args[0].equals("list")){
                MessageUtils.displayMap(CheckpointManager.getCheckpointNames(senderUuid), sender, "チェックポイント");
            }

            else{
                CheckpointManager.registerCheckpoint(senderUuid, loc, args[0]);
            }

            return true;
        }

        if(args.length == 2){
            if(args[0].equals("remove")){
                boolean removed = CheckpointManager.removeCheckpoint(senderUuid, args[1]);
                MessageUtils.sendRemoveMessage(sender, removed, args[1], "チェックポイント");
                return true;
            }
            else if(args[0].equals("tp")){
                if(CheckpointManager.selectCp(senderUuid, args[1]) != null) {
                    player.teleport(CheckpointManager.getCurrentCpLocation(senderUuid));
                }

                else{
                    sender.sendMessage(ChatColor.RED + "指定された名前のチェックポイントは存在しません。");
                }

                return true;
            }
        }
        return false;
    }
}
