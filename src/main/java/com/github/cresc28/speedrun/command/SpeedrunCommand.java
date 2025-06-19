package com.github.cresc28.speedrun.command;

import com.github.cresc28.speedrun.data.CourseDataManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class SpeedrunCommand implements CommandExecutor, TabCompleter {

    private Location getBlockLocation(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public void registarCourse(Location loc, String courseName, String type){
        //重複が起きないよう現在座標をキーとする値を一旦削除
        CourseDataManager.getStartMap().remove(loc);
        CourseDataManager.getEndMap().remove(loc);

        if (type.equalsIgnoreCase("start")) {
            CourseDataManager.getStartMap().put(loc, courseName);
        }
        else if (type.equalsIgnoreCase("end")) {
            CourseDataManager.getEndMap().put(loc, courseName);
        }

        CourseDataManager.save();
    }

    //登録されているparkourの削除
    public boolean removeCourse(Map<Location, String> map, String parkourName) {
        List<Location> keysToRemove = new ArrayList<>();
        for (Map.Entry<Location, String> entry : map.entrySet()) {
            if (entry.getValue().equals(parkourName)) {
                keysToRemove.add(entry.getKey());
            }
        }

        keysToRemove.forEach(map::remove);
        CourseDataManager.save();
        return true;
    }

    public void sendRemoveMessage(CommandSender sender, boolean removed, String courseName) {
        if (removed) sender.sendMessage(courseName + "を削除しました。");
        else sender.sendMessage(ChatColor.RED + "その名前のコースは登録されていません。");
    }

    //登録されているparkourの一覧表示
    public void displayParkour(Map<Location, String> map, CommandSender sender) {
        if (map.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "コースは登録されていません。");
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

        if (!command.getName().equalsIgnoreCase("course")) return null;

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
                    completionFromMap(CourseDataManager.getEndMap().values(), args[1].toLowerCase(), completions);
                }
            }
        }

        else if (args.length == 3) {
            if (args[0].equals("remove")) {
                if (args[1].equals("start")) {
                    //startMapに登録されているアスレを表示
                    completionFromMap(CourseDataManager.getStartMap().values(), args[2].toLowerCase(), completions);
                } else if (args[1].equals("end")) {
                    completionFromMap(CourseDataManager.getEndMap().values(), args[2].toLowerCase(), completions);
                }
            }
        }
        return completions;
    }



    //コマンドの受け取り
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("course")) return false;

        if(args.length == 1 && args[0].equals("list")) {
            displayParkour(CourseDataManager.getStartMap(), sender);
            return true;
        }

        else if (args.length == 2 && args[0].equals("remove")) {
            boolean removedStart = removeCourse(CourseDataManager.getStartMap(), args[1]);
            boolean removedEnd = removeCourse(CourseDataManager.getEndMap(), args[1]);
            sendRemoveMessage(sender, (removedStart || removedEnd), args[1]);
            return true;
        }

        if (args.length == 3) {
            if (args[0].equals("add")) {
                Player player = (Player) sender;
                Location loc = getBlockLocation(player.getLocation());

                if (args[1].equals("start")) {
                    registarCourse(loc,args[2],"start");
                    return true;
                }

                else if (args[1].equals("end")) {
                    registarCourse(loc,args[2],"end");
                    return true;
                }

            }

            else if (args[0].equals("remove")) {
                if (args[1].equals("start")) {
                    boolean removed = removeCourse(CourseDataManager.getStartMap(), args[2]);
                    sendRemoveMessage(sender, removed, args[2]);
                    return true;
                }

                else if (args[1].equals("end")) {
                    boolean removed = removeCourse(CourseDataManager.getStartMap(), args[2]);
                    sendRemoveMessage(sender, removed, args[2]);
                    return true;
                }

            }

        }

        return false;
    }
}