package com.github.cresc28.speedrun.gui;

import com.github.cresc28.speedrun.core.manager.CheckpointManager;
import com.github.cresc28.speedrun.utils.HeadUtils;
import com.github.cresc28.speedrun.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;

/**
 * チェックポイントをインベントリ形式で表示するクラス。
 */

public class CheckpointMenu {
    private final Player player;
    private final CheckpointManager cpm;
    private int currentPage = 0;

    public CheckpointMenu(Player player, CheckpointManager cpm, int currentPage){
        this.player = player;
        this.cpm = cpm;
        this.currentPage = currentPage;
    }

    /**
     * チェックポイントメニューを開く
     */
    public void openInventory(){
        final int ALL_SLOT = 54; //54枠
        final int CHECKPOINT_SLOT = 45; //ネザースターで埋めるのは45枠まで
        Inventory inv = Bukkit.createInventory(null, ALL_SLOT,"CheckpointMenu(page " + (currentPage + 1) + ")");
        Collection<String> cpCollection = cpm.getCheckpointNames(player.getUniqueId(), player.getWorld());
        Utils.sortCollection(cpCollection);
        ArrayList<String> cpNames = new ArrayList<>(cpCollection);
        int pages = (cpNames.size() - 1) / CHECKPOINT_SLOT;

        if(currentPage < pages) {
            ItemStack arrowRight = HeadUtils.getHeadFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19", "次へ");
            inv.setItem(53, arrowRight);
        }

        if(currentPage > 0){
            ItemStack arrowLeft = HeadUtils.getHeadFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==", "前へ");
            inv.setItem(45, arrowLeft);
        }

        int startIndex = currentPage * CHECKPOINT_SLOT;
        int endIndex = Math.min(cpNames.size(), (currentPage + 1) * CHECKPOINT_SLOT);

        for(int i = startIndex; i < endIndex; i++){
            ItemStack netherStar = new ItemStack(Material.NETHER_STAR,1);
            Utils.changeItemName(netherStar, cpNames.get(i));
            inv.setItem(i - startIndex, netherStar);
        }

        player.openInventory(inv);
        player.addScoreboardTag("MenuOpen");
    }
}
