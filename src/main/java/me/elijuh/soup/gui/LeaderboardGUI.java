package me.elijuh.soup.gui;

import lombok.Getter;
import me.elijuh.soup.Core;
import me.elijuh.soup.util.ChatUtil;
import me.elijuh.soup.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class LeaderboardGUI {
    private final ItemStack filler = new ItemBuilder(Material.STAINED_GLASS_PANE).setDura(15).setName(" ").build();
    private final Inventory inv = Bukkit.createInventory(null, 36, ChatUtil.color("&6&lLeaderboards"));

    public LeaderboardGUI() {
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, filler);
        }
    }

    public void setItems() {
        inv.setItem(12, new ItemBuilder(Material.IRON_SWORD).addFlag(ItemFlag.HIDE_ATTRIBUTES).setName("&6&lTop Kills").build());
        inv.setItem(13, new ItemBuilder(Material.SKULL_ITEM).setName("&6&lTop Deaths").build());
        inv.setItem(14, new ItemBuilder(Material.BLAZE_POWDER).setName("&6&lTop Streak").build());
        inv.setItem(22, new ItemBuilder(Material.GOLD_INGOT).setName("&6&lTop Coins").build());
        int[] slots = {12, 13, 14, 22};
        String[] types = {"kills", "deaths", "streak", "coins"};
        for (int i = 0; i < 4; i++) {
            String type = types[i];
            ItemStack item = inv.getItem(slots[i]);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            LinkedHashMap<String, Integer> leaderboard = Core.i().getMongoManager().getLeaderboards().get(type);
            lore.add(ChatUtil.color("&7&m-----------------------"));
            lore.add(ChatUtil.color("&7Leaderboard for " + ChatUtil.upperFirst(type)));
            lore.add(" ");
            int pos = 1;
            for (Map.Entry<String, Integer> entry : leaderboard.entrySet()) {
                lore.add(ChatUtil.color("&3&l#" + pos++ + " &e" + entry.getKey() + " (&d" + entry.getValue() + " " + ChatUtil.upperFirst(type) + "&e)"));
            }
            lore.add(ChatUtil.color("&7&m-----------------------"));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
    }
}
