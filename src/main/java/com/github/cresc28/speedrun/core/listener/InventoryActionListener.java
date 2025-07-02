package com.github.cresc28.speedrun.core.listener;

import com.github.cresc28.speedrun.core.manager.CheckpointManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Set;

public class InventoryActionListener implements Listener {
    private final CheckpointManager cpm;

    public InventoryActionListener(CheckpointManager cpm) {
        this.cpm = cpm;
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        Set<String> UserTags = event.getWhoClicked().getScoreboardTags();
        if(UserTags.contains("MenuOpen")){
            event.setCancelled(true);
        }

        if(event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()){
            return; // Nullチェックやアイテムメタの有無を確認したほうが安全
        }

        String displayName = event.getCurrentItem().getItemMeta().getDisplayName();

        if(!cpm.selectCheckpoint(player.getUniqueId(), player.getWorld(), displayName)) {
            player.sendMessage(ChatColor.RED + "指定された名前のチェックポイントは存在しません。");
        }

        else{
            player.teleport(cpm.getGlobalRecentCpLocation(player.getUniqueId()));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        Set<String> UserTags = event.getPlayer().getScoreboardTags();
        if(UserTags.contains("MenuOpen")){
            event.getPlayer().removeScoreboardTag("MenuOpen");
        }
    }
}
