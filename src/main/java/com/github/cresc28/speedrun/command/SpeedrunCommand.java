package com.github.cresc28.speedrun.command;

import com.github.cresc28.speedrun.data.ParkourDataManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SpeedrunCommand implements CommandExecutor, TabCompleter {

    private Location getBlockLocation(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    //登録されているparkourの削除
    public boolean removeParkour(Map<Location, String> map, String parkourName) {
        List<Location> keysToRemove = new ArrayList<>();
        for (Map.Entry<Location, String> entry : map.entrySet()) {
            if (entry.getValue().equals(parkourName)) {
                keysToRemove.add(entry.getKey());
            }
        }

        keysToRemove.forEach(map::remove);
        ParkourDataManager.save();
        return true;
    }

    public void sendRemoveMessage(CommandSender sender, boolean removed, String parkourName) {
        if (removed) sender.sendMessage(parkourName + "を削除しました。");
        else sender.sendMessage(ChatColor.RED + "その名前のパルクールは登録されていません。");
    }

    //登録されているparkourの一覧表示
    public void displayParkour(Map<Location, String> map, CommandSender sender) {
        if (map.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "パルクールは登録されていません。");
            return;
        }

        Set<String> names = new HashSet<>(map.values());
        sender.sendMessage(names.toString());
    }

    //Collection内の全ての要素をTAB補完として表示する。
    private void completionFromMap(Collection<String> source, String prefix, List<String> completions) {
        for (String value : new HashSet<>(source)) {
            if (value.toLowerCase().startsWith(prefix)) {
                completions.add(value);
            }
        }
    }

    //TAB補完
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!command.getName().equalsIgnoreCase("parkour")) return null;

        if (args.length == 1) {
            List<String> options = Arrays.asList("add", "remove", "list");
            for (String option : options) {
                if (option.startsWith(args[0].toLowerCase())) completions.add(option);
            }
        }

        else if(args.length == 2) {
            if ("add".equalsIgnoreCase(args[0]) || "remove".equalsIgnoreCase(args[0])) {
                List<String> options = Arrays.asList("start", "end");
                for (String option : options) {
                    if (option.startsWith(args[1].toLowerCase())) completions.add(option);
                }

                if ("remove".equalsIgnoreCase(args[0])) {
                    completionFromMap(ParkourDataManager.getEndMap().values(), args[1].toLowerCase(), completions);
                }
            }
        }

        else if (args.length == 3) {
            if (args[0].equals("remove")) {
                if (args[1].equals("start")) {
                    //startMapに登録されているアスレを表示
                    completionFromMap(ParkourDataManager.getStartMap().values(), args[2].toLowerCase(), completions);
                } else if (args[1].equals("end")) {
                    completionFromMap(ParkourDataManager.getEndMap().values(), args[2].toLowerCase(), completions);
                }
            }
        }
        return completions;
    }

    //コマンドの受け取り
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("parkour")) return false;

        if(args.length == 1 && args[0].equals("list")) {
            displayParkour(ParkourDataManager.getStartMap(), sender);
            return true;
        }

        else if (args.length == 2 && args[0].equals("remove")) {
            boolean removedStart = removeParkour(ParkourDataManager.getStartMap(), args[1]);
            boolean removedEnd = removeParkour(ParkourDataManager.getEndMap(), args[1]);
            sendRemoveMessage(sender, (removedStart || removedEnd), args[2]);
            return true;
        }

        if (args.length == 3) {
            if (args[0].equals("add")) {
                Player player = (Player) sender;
                Location loc = getBlockLocation(player.getLocation());

                if (args[1].equals("start")) {
                    ParkourDataManager.getStartMap().put(loc, args[2]);
                    ParkourDataManager.save();
                    return true;
                }

                else if (args[1].equals("end")) {
                    ParkourDataManager.getEndMap().put(loc, args[2]);
                    ParkourDataManager.save();
                    return true;
                }

            }

            else if (args[0].equals("remove")) {
                if (args[1].equals("start")) {
                    boolean removed = removeParkour(ParkourDataManager.getStartMap(), args[2]);
                    sendRemoveMessage(sender, removed, args[2]);
                    return true;
                }

                else if (args[1].equals("end")) {
                    boolean removed = removeParkour(ParkourDataManager.getStartMap(), args[2]);
                    sendRemoveMessage(sender, removed, args[2]);
                    return true;
                }

            }

        }

        return false;
    }
}