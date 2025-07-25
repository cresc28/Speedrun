package com.github.cresc28.speedrun.event;

import com.github.cresc28.speedrun.manager.TimerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;


/**
 * プレイヤーの移動イベントを検知し処理を行うクラス。
 */
public class PlayerMoveListener implements Listener {
    private final TimerManager timerManager;

    public PlayerMoveListener(TimerManager timerManager) {
        this.timerManager = timerManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        timerManager.checkRunState(e.getPlayer());
    }
}