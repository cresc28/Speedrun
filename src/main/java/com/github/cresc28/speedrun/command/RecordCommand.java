package com.github.cresc28.speedrun.command;

import com.github.cresc28.speedrun.core.manager.CourseManager;
import com.github.cresc28.speedrun.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class RecordCommand implements CommandExecutor, TabCompleter {
    private final CourseManager courseManager;


    public RecordCommand(CourseManager courseManager){
        this.courseManager = courseManager;
    }
    /**
     * Tab補完の処理を行う。
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        Player player = (Player) sender;

        if (args.length == 1) {
            Utils.completionFromCollection(courseManager.getAllCourseName(), args[0].toLowerCase(), completions);
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
        String playerName = null; //プレイヤー名
        boolean dup = false; //同じプレイヤーの複数記録を表示するか
        boolean detailInputed = false; //詳細を表示するか
        boolean above = false; //自分より上の10位表示モード
        int displayCount = 10; //何位まで表示するか


        for (int i = 1; i < args.length; i++) {
            String arg = args[i].toLowerCase();

            if ("dup".equalsIgnoreCase(arg)) {
                dup = true;
                continue;
            }

            else if ("detail".equalsIgnoreCase(arg)) {
                if(playerName == null) playerName = player.getDisplayName();
                detailInputed = true;
                continue;
            }

            else if ("above".equalsIgnoreCase(arg)) {
                above = true;
                continue;
            }

            if(Utils.isNumeric(arg)) {
                displayCount = Integer.parseInt(arg);
                continue;
            }

            else { //dup, detail, aboveというプレイヤーが存在する状況は可能性として極めて低いため無視。
                playerName = arg;
            }
        }

        if(above) {
            player.sendMessage("aboveとdetailの両立はできません。aboveを優先します。");
            detailInputed = false; //aboveが入力されているときはdetailが入力されていないものとする。
        }

        //detailが指定されていても中継地点がない場合はこのコースではdetailは無意味です　と返す
        //

        return false;
    }
}
