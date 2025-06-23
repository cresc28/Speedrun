package com.github.cresc28.speedrun.listener;


import com.github.cresc28.speedrun.config.ConfigManager;
import com.github.cresc28.speedrun.manager.CheckpointManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;


/**
 * プレイヤーのアイテムクリック時の処理を行うクラス。
 * <p>
 * ネザースターの右クリックを検知するとチェックポイントへTPさせ、
 * 左クリックを検知するとメニューを開く。
 * </p>
 */

public class PlayerInteractListener implements Listener {
    private final CheckpointManager cpm;

    public PlayerInteractListener(CheckpointManager cpm){
        this.cpm = cpm;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        ItemStack item = event.getItem();

        if(item != null && item.getType() == Material.NETHER_STAR) {
            if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
                Location loc;
                if(ConfigManager.isCrossWorldTpAllowed()) loc = cpm.getGlobalRecentCpLocation(uuid);
                else loc = cpm.getLocalRecentCpLocation(uuid);

                if (loc != null) {
                    player.teleport(loc);
                }
                else {
                    player.sendMessage(ChatColor.RED + "有効なチェックポイントが存在しません。");
                }
            }
            else if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK){

            }
        }
    }
}
