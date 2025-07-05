package com.github.cresc28.speedrun.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * プレイヤーヘッドに関するユーティリティクラス。
 */

public class HeadUtils {
    private static final Logger LOGGER = Logger.getLogger("HeadUtils");

    /**
     * プレイヤーの頭(マインクラフト1.13以降ではこの関数は動作しない)
     *
     * @param name 取得したい頭を持つプレイヤーの名前
     * @return プレイヤーの頭
     */
    public static ItemStack getPlayerHead(String name){
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(name);
        head.setItemMeta(meta);

        return head;
    }

    /**
     * スキンを高速で読み込む。上の関数は低速のためこちらを用いるべきである。
     * (setOwnerなどで読み込むとサーバー接続により初回のみ遅延が生じてしまうため)
     * 下記のサイトのValueの部分がBASE64形式のスキンテクスチャに当たる。
     * <a href="https://minecraft-heads.com/custom-heads/head/7827-oak-wood-arrow-left">...</a>
     * アイテムはプレイヤー名で区別できないため、名前変更も同時に行い表示名で区別する。
     *
     * @param base64Texture base64形式のスキンテクスチャ
     * @param displayName 表示名
     * @return スキン反映されたhead
     */
    public static ItemStack getHeadFromBase64(String base64Texture, String displayName) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        if (base64Texture == null || base64Texture.isEmpty()) return head;

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", base64Texture));

        try{
            Field field = meta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(meta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "headの読み込みで例外が発生しました。");
        }

        meta.setDisplayName(displayName);
        head.setItemMeta(meta);
        return head;
    }

    /**
     * ItemStackが特定プレイヤーの頭かどうかを判定する(マインクラフト1.13以降ではこの関数は動作しない)
     *
     * @param item 判定対象のアイテム
     * @param name プレイヤー名
     * @return 該当プレイヤーの頭であればtrue
     */
    public static boolean isPlayerHeadOf(ItemStack item, String name) {
        if (item == null || item.getType() != Material.SKULL_ITEM) return false;

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof SkullMeta)) return false;

        SkullMeta skullMeta = (SkullMeta) meta;
        return name.equalsIgnoreCase(skullMeta.getOwner());
    }
}
