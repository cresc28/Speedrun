package com.github.cresc28.speedrun.event;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/**
 * 看板への文字入力時の処理
 */

public class SignChangeListener implements Listener {

    /**
     * 看板の編集確定時の処理。
     *
     * @param e SignChangeEvent
     */
    @EventHandler
    public void onSignChange(SignChangeEvent e){
        Player player = e.getPlayer();

        String firstLine = e.getLine(0);
        String secondLine = e.getLine(1);
        String thirdLine = e.getLine(2);
        if(!"CP".equalsIgnoreCase(firstLine)) return;

        e.setLine(0, ChatColor.AQUA + "" + ChatColor.BOLD +  "☆☆☆ Checkpoint ☆☆☆");
        if(secondLine.isEmpty()) e.setLine(1, ChatColor.BOLD + "tmp");
        else e.setLine(1, ChatColor.BOLD + secondLine);

        if("fixed".equalsIgnoreCase(thirdLine)) e.setLine(2, "fixed");
        else if("fly".equalsIgnoreCase(thirdLine)) e.setLine(2, "fly");
        else e.setLine(2, "player");

        e.setLine(3, ChatColor.RED + "" + ChatColor.BOLD + "☆☆☆Right Click!☆☆☆");
    }
}
