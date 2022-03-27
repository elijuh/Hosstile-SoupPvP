package me.elijuh.soup.data.kits;

import lombok.Getter;
import me.elijuh.soup.data.Pair;
import me.elijuh.soup.util.ChatUtil;
import me.elijuh.soup.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

@Getter
public enum KitType {
    DEFAULT(0, new ItemBuilder(Material.IRON_SWORD).addLore("&7Default kit for PvP.").addFlag(ItemFlag.HIDE_ATTRIBUTES), new Pair[]{}),
    ARCHER(0, new ItemBuilder(Material.BOW).addLore("&7Infinity bow and Speed II."), new Pair[]{new Pair<>(PotionEffectType.SPEED, 2)}),
    BLAZE(3500, new ItemBuilder(Material.BLAZE_ROD).addLore("&7Fire sword with Fire Resistance."), new Pair[]{new Pair<>(PotionEffectType.FIRE_RESISTANCE, 1)}),
    CACTUS(3750, new ItemBuilder(Material.CACTUS).addLore("&7Prickly thorns."), new Pair[]{}),
    FISHERMAN(4000, new ItemBuilder(Material.FISHING_ROD).addLore("&7Gets a fishing rod."), new Pair[]{});

    private final int price;
    private final ItemStack icon;
    private final List<Pair<PotionEffectType, Integer>> effects;

    KitType(int price, ItemBuilder builder, Pair<PotionEffectType, Integer>[] effects) {
        this.price = price;
        this.icon = builder.setName("&a" + ChatUtil.upperFirst(name())).build();
        this.effects = Arrays.asList(effects);
    }
}
