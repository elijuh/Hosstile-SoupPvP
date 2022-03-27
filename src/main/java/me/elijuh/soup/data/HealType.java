package me.elijuh.soup.data;

import lombok.Getter;
import me.elijuh.soup.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
public enum HealType {
    SOUP(new ItemBuilder(Material.MUSHROOM_SOUP).build()),
    POTS(new ItemBuilder(Material.POTION).setDura(16421).build());

    private final ItemStack item;

    HealType(ItemStack item) {
        this.item = item;
    }
}
