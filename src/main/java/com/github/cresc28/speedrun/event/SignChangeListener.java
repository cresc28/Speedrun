package com.github.cresc28.speedrun.event;

import com.github.cresc28.speedrun.utils.TextUtils;
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
        String firstLine = e.getLine(0);

        if("CP".equalsIgnoreCase(firstLine)){
            createCheckpointSign(e);
        }

        else if("top".equalsIgnoreCase(firstLine)){
            createRankingSign(e);
        }
    }

    private void createCheckpointSign(SignChangeEvent e){
        String secondLine = e.getLine(1);
        String thirdLine = e.getLine(2);

        e.setLine(0, ChatColor.AQUA + "" + ChatColor.BOLD +  "☆☆☆ Checkpoint ☆☆☆");
        if(secondLine.isEmpty()) e.setLine(1, ChatColor.BOLD + "tmp");
        else e.setLine(1, ChatColor.BOLD + secondLine);

        if("fixed".equalsIgnoreCase(thirdLine)) e.setLine(2, "fixed");
        else if("fly".equalsIgnoreCase(thirdLine)) e.setLine(2, "fly");
        else e.setLine(2, "player");

        e.setLine(3, ChatColor.DARK_RED + "" + ChatColor.BOLD + "☆☆☆Right Click!☆☆☆");
    }

    private void createRankingSign(SignChangeEvent e){
        Player player = e.getPlayer();

        String secondLine = e.getLine(1);
        String thirdLine = e.getLine(2);
        String forthLine = e.getLine(3);
        String title;
        if(secondLine.isEmpty()) {
            player.sendMessage("2行目にはコース名を必ず指定してください。");
            return;
        }

        if(thirdLine.isEmpty()){
            thirdLine = "10";
        }

        if(!TextUtils.isPositiveInteger(thirdLine)){
            thirdLine = "10";
            player.sendMessage("3行目には表示する記録数を半角数字で入力してください。");
        }

        if("dup".equalsIgnoreCase(forthLine)) title = "☆☆☆Ranking(All)☆☆☆";
        else if("detail".equalsIgnoreCase(forthLine)) {
            title = "☆☆☆Best Record☆☆☆";
            thirdLine = "";
        }
        else title = "☆☆☆ Ranking ☆☆☆";


        e.setLine(0, ChatColor.AQUA + "" + ChatColor.BOLD + title);
        e.setLine(1, ChatColor.BOLD + secondLine);
        e.setLine(2, ChatColor.GRAY + "" + ChatColor.BOLD + thirdLine);
        e.setLine(3, ChatColor.DARK_RED + "" + ChatColor.BOLD + "☆☆☆Right Click!☆☆☆");
    }
}
