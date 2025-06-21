package com.github.cresc28.speedrun.utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MessageUtils {
    //登録されているmap要素の一覧表示
    public static void displayMap(Collection<String> names, CommandSender sender, String subject){
        if (names.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "登録済みの" + subject + "が存在しません。");
            return;
        }

        sender.sendMessage(names.toString());
    }

    public static void sendRemoveMessage(CommandSender sender, boolean removed, String name, String subject) {
        if (removed) sender.sendMessage(name + "を削除しました。");
        else sender.sendMessage(ChatColor.RED + "その名前の" + subject + "は登録されていません。");
    }
}
