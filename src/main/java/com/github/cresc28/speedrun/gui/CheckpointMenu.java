package com.github.cresc28.speedrun.gui;

import com.github.cresc28.speedrun.core.manager.CheckpointManager;
import com.github.cresc28.speedrun.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * チェックポイントをインベントリ形式で表示するクラス。
 */

public class CheckpointMenu {
    private final Player player;
    private final CheckpointManager cpm;

    public CheckpointMenu(Player player, CheckpointManager cpm){
        this.player = player;
        this.cpm = cpm;
    }

    public void openInventory(){
        final int ALL_SLOT = 54; //54枠
        final int CHECKPOINT_SLOT = 44; //45番スロットまで(0-based)
        Inventory inv = Bukkit.createInventory(null, ALL_SLOT,"CheckpointMenu");
        Collection<String> cpCollection = cpm.getCheckpointNames(player.getUniqueId(), player.getWorld());
        Utils.sortCollection(cpCollection);
        ArrayList<String> cpNames = new ArrayList<>(cpCollection);

        for(int i = 0; i < cpNames.size(); i++){
            ItemStack netherStar = new ItemStack(Material.NETHER_STAR,1);
            ItemMeta itemMeta = netherStar.getItemMeta();
            itemMeta.setDisplayName(cpNames.get(i));
            netherStar.setItemMeta(itemMeta);
            if(i < CHECKPOINT_SLOT) inv.setItem(i,netherStar);
        }

        player.openInventory(inv);
        player.addScoreboardTag("MenuOpen");
    }
}
