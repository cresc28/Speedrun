package com.github.cresc28.speedrun.command;

import com.github.cresc28.speedrun.manager.CourseDataManager;
import com.github.cresc28.speedrun.utils.MessageUtils;
import com.github.cresc28.speedrun.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * /courseコマンドの実装クラス。
 */
public class SpeedrunCommand implements CommandExecutor, TabCompleter {
    /**
     * Tab補完の処理を行う。
     */
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
                    Utils.completionFromMap(CourseDataManager.getEndMap().values(), args[1].toLowerCase(), completions);
                }
            }
        }

        else if (args.length == 3) {
            if (args[0].equals("remove")) {
                if (args[1].equals("start")) {
                    //startMapに登録されているアスレを表示
                    Utils.completionFromMap(CourseDataManager.getStartMap().values(), args[2].toLowerCase(), completions);
                } else if (args[1].equals("end")) {
                    Utils.completionFromMap(CourseDataManager.getEndMap().values(), args[2].toLowerCase(), completions);
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
        if (!command.getName().equalsIgnoreCase("course")) return false;

        if(args.length == 1 && args[0].equals("list")) {
            MessageUtils.displayMap(CourseDataManager.getStartMap().values(), sender, "コース");
            return true;
        }

        else if (args.length == 2 && args[0].equals("remove")) {
            boolean removedStart = CourseDataManager.removeCourse(args[1], "start");
            boolean removedEnd = CourseDataManager.removeCourse(args[1], "end");
            boolean removed = removedStart || removedEnd;
            MessageUtils.sendRemoveMessage(sender, removed, args[1], "コース");
            return true;
        }

        if (args.length == 3) {
            if (args[0].equals("add")) {
                Player player = (Player) sender;
                Location loc = Utils.getBlockLocation(player.getLocation());

                if (args[1].equals("start")) {
                    CourseDataManager.registerCourse(loc,args[2], "start");
                    sender.sendMessage(String.format("スタート地点(%s) : %d %d %d", args[2], loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                    return true;
                }

                else if (args[1].equals("end")) {
                    CourseDataManager.registerCourse(loc,args[2], "end");
                    sender.sendMessage(String.format("ゴール地点(%s) : %d %d %d", args[2], loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                    return true;
                }

            }

            else if (args[0].equals("remove")) {
                if (args[1].equals("start")) {
                    boolean removed = CourseDataManager.removeCourse(args[2], "start");
                    MessageUtils.sendRemoveMessage(sender, removed, args[2], "コース");
                    return true;
                }

                else if (args[1].equals("end")) {
                    boolean removed = CourseDataManager.removeCourse(args[2], "end");
                    MessageUtils.sendRemoveMessage(sender, removed, args[2], "コース");
                    return true;
                }
            }
        }

        return false;
    }
}