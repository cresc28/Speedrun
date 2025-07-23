package com.github.cresc28.speedrun.gui;

import com.github.cresc28.speedrun.utils.GameUtils;
import com.github.cresc28.speedrun.utils.HeadUtils;
import com.github.cresc28.speedrun.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * ワールドをインベントリ形式で表示するクラス。
 */

public class RecordMenuForDelete {
    private final Player player;
    private final List<String> times;
    private int currentPage = 0;

    public RecordMenuForDelete(Player player, List<String> times, int currentPage){
        this.player = player;
        this.times = times;
        this.currentPage = currentPage;
    }

    /**
     * ワールドメニューを開く
     */
    public void openInventory(){
        final int ALL_SLOT = 54; //54枠
        final int USE_SLOT = 45; //紙で埋めるのは45枠まで
        Inventory inv = Bukkit.createInventory(null, ALL_SLOT,"削除する記録をクリックしてください。");

        int pages = (times.size() - 1) / USE_SLOT;

        if(currentPage < pages) {
            ItemStack arrowRight = HeadUtils.getHeadFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19", "次へ");
            inv.setItem(53, arrowRight);
        }

        if(currentPage > 0){
            ItemStack arrowLeft = HeadUtils.getHeadFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==", "前へ");
            inv.setItem(45, arrowLeft);
        }

        int startIndex = currentPage * USE_SLOT;
        int endIndex = Math.min(times.size(), (currentPage + 1) * USE_SLOT);

        for(int i = startIndex; i < endIndex; i++){
            ItemStack paper = new ItemStack(Material.PAPER,1);
            TextUtils.changeItemName(paper, times.get(i));
            inv.setItem(i - startIndex, paper);
        }

        player.openInventory(inv);
    }
}
