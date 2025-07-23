package com.github.cresc28.speedrun.event;


import com.github.cresc28.speedrun.config.ConfigManager;
import com.github.cresc28.speedrun.data.SpeedrunFacade;
import com.github.cresc28.speedrun.manager.CheckpointManager;
import com.github.cresc28.speedrun.gui.CheckpointMenu;
import com.github.cresc28.speedrun.utils.GameUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;


/**
 * プレイヤーのアイテムクリック時の処理を行うクラス。
 * ネザースターの右クリックを検知するとチェックポイントへTPさせ、
 * 左クリックを検知するとメニューを開く。
 *
 */

public class PlayerInteractListener implements Listener {
    private final CheckpointManager cpManager;

    public PlayerInteractListener(SpeedrunFacade facade){
        cpManager = facade.getCpManager();
    }

    /**
     * プレイヤーが何かをクリックしたときの処理。
     *
     * @param e PlayerInteractEvent
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        if (e.getHand() != EquipmentSlot.HAND) return;
        ItemStack item = e.getItem();
        Block clickedBlock = e.getClickedBlock();

        //看板クリックよりこちらの処理を優先する。
        if(item != null && item.getType() == Material.NETHER_STAR) {
            if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
                handNetherStarRightClick(e);
            }

            else if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK){
                handNetherStarLeftClick(e);
            }

            return; //CPでの移動と同時に看板をクリックできないようにする。(不本意なCPセットを防止)
        }

        if (clickedBlock != null && clickedBlock.getState() instanceof Sign && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            signClick(e, (Sign) clickedBlock.getState());
        }
    }

    /**
     * プレイヤーがネザークリックをもって右クリックしたときの処理。
     *
     * @param e PlayerInteractEvent
     */
    private void handNetherStarRightClick(PlayerInteractEvent e){
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        Location loc;

        if(ConfigManager.isCrossWorldTpAllowed()) loc = cpManager.getGlobalRecentCpLocation(uuid);
        else loc = cpManager.getLocalRecentCpLocation(uuid);

        if (loc != null) {
            player.teleport(loc);
        }
        else {
            player.sendMessage(ChatColor.RED + "有効なチェックポイントが存在しません。");
        }
    }

    /**
     * プレイヤーがネザークリックをもって左クリックしたときの処理。
     *
     * @param e PlayerInteractEvent
     */
    private void handNetherStarLeftClick(PlayerInteractEvent e){
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        e.setCancelled(true); // ブロック破壊を起こさない
        player.addScoreboardTag("MenuOpen");
        new CheckpointMenu(player, cpManager, 0, player.getWorld(),false).openInventory();
    }

    /**
     * 設置されている看板をクリックしたときの処理
     *
     * @param e PlayerInteractEvent
     * @param sign 看板
     */
    private void signClick(PlayerInteractEvent e, Sign sign){
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        String firstLine = ChatColor.stripColor(sign.getLine(0));
        String courseName = ChatColor.stripColor(sign.getLine(1));

        if(!"☆☆☆ Checkpoint ☆☆☆".equals(firstLine)) {
            return;
        }

        String type = ChatColor.stripColor(sign.getLine(2));
        if(!"player".equals(type) && !"fixed".equals(type) && !"fly".equals(type)) {
            return;
        }

        Location loc;

        if (type.equals("fixed")) {
            Location blockLoc = sign.getLocation(); //これはブロックの角の座標を返す。
            loc = blockLoc.add(0.5,0,0.5); //ブロックの中心に座標を持ってくる。
            loc.setPitch(0);
            byte signData = sign.getBlock().getData();
            loc.setYaw(GameUtils.getYaw(signData));
        }

        else {
            if ("player".equals(type) && !player.isOnGround()) {
                player.sendMessage("空中でのCP設定は禁止されています");
                return;
            }
            loc = player.getLocation();
        }

        cpManager.registerCheckpoint(uuid, loc, courseName);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HARP, 1.0f, 1.4f);
    }
}
