package com.github.cresc28.speedrun.listener;


import com.github.cresc28.speedrun.manager.CheckpointManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        ItemStack item = event.getItem();

        if(item != null && item.getType() == Material.NETHER_STAR) {
            if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
                Location loc = CheckpointManager.getCurrentCpLocation(event.getPlayer().getUniqueId());
                if (loc != null) {
                    event.getPlayer().teleport(loc);
                }
                else {
                    event.getPlayer().sendMessage(ChatColor.RED + "有効なチェックポイントが存在しません。");
                }
            }
            else if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK){

            }
        }
    }
}
