package com.github.cresc28.speedrun.data;

import org.bukkit.World;

/**
 * メニューの状態管理クラス。
 */
public class MenuState {
    boolean deleteMode = false;
    int page = 0;
    World selectedWorld;

    public MenuState(World initialWorld) {
        this.selectedWorld = initialWorld;
    }
    public boolean isDeleteMode() {
        return deleteMode;
    }

    public void setDeleteMode(boolean isDeleteMode) {
        this.deleteMode = isDeleteMode;
    }

    public int getPage() {
        return page;
    }

    public void incrementPage() {
        this.page++;
    }

    public void decrementPage() {
        this.page--;
    }

    public World getWorld() {
        return selectedWorld;
    }

    public void setWorld(World world) {
        this.selectedWorld = world;
    }

    public void reset(){
        page = 0;
        this.deleteMode = false;
    }
}
