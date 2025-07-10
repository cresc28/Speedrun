package com.github.cresc28.speedrun.gui;

import com.github.cresc28.speedrun.core.manager.CheckpointManager;
import com.github.cresc28.speedrun.utils.HeadUtils;
import com.github.cresc28.speedrun.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * ワールドをインベントリ形式で表示するクラス。
 */

public class WorldMenu {
    private final Player player;
    private int currentPage = 0;

    public WorldMenu(Player player, CheckpointManager cpm, int currentPage){
        this.player = player;
        this.currentPage = currentPage;
    }

    /**
     * ワールドメニューを開く
     */
    public void openInventory(){
        final int ALL_SLOT = 54; //54枠
        final int WORLD_SLOT = 45; //ネザースターで埋めるのは45枠まで
        Inventory inv = Bukkit.createInventory(null, ALL_SLOT,"WorldMenu");
        List<World> worlds = Bukkit.getWorlds();
        worlds.sort(Comparator.comparing(World::getName)); // 名前でソート

        List<String> worldNames = new ArrayList<>();
        for (World world : worlds) {
            worldNames.add(world.getName()); // World → String
        }
        int pages = (worldNames.size() - 1) / WORLD_SLOT;

        if(currentPage < pages) {
            ItemStack arrowRight = HeadUtils.getHeadFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19", "次へ");
            inv.setItem(53, arrowRight);
        }

        if(currentPage > 0){
            ItemStack arrowLeft = HeadUtils.getHeadFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==", "前へ");
            inv.setItem(45, arrowLeft);
        }

        ItemStack netherStar = new ItemStack(Material.NETHER_STAR,1);
        Utils.changeItemName(netherStar, "チェックポイント選択へ切り替え");
        inv.setItem(52, netherStar);

        int startIndex = currentPage * WORLD_SLOT;
        int endIndex = Math.min(worldNames.size(), (currentPage + 1) * WORLD_SLOT);

        for(int i = startIndex; i < endIndex; i++){
            ItemStack paper = new ItemStack(Material.PAPER,1);
            Utils.changeItemName(paper, worldNames.get(i));
            inv.setItem(i - startIndex, paper);
        }

        player.openInventory(inv);
    }
}
