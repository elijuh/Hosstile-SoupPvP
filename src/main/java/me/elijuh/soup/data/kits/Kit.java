package me.elijuh.soup.data.kits;

import lombok.Getter;
import me.elijuh.soup.data.User;
import me.elijuh.soup.util.ChatUtil;
import me.elijuh.soup.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Kit {
    private final Map<Integer, ItemStack> items = new HashMap<>();
    private final KitType type;
    private final String id;

    public Kit(KitType type) {
        this.type = type;
        this.id = type.name().toLowerCase();
    }

    public void apply(User user) {
        ItemStack heal = user.getHealType().getItem();
        for (int i = 0; i < 40; i++) {
            if (items.containsKey(i)) {
                user.p().getInventory().setItem(i, items.get(i));
            } else if (i < 36) {
                user.p().getInventory().setItem(i, heal);
            }
        }
        user.clearEffects();
        type.getEffects().forEach(pair -> user.p().addPotionEffect(new PotionEffect(pair.getX(), Integer.MAX_VALUE, pair.getY() - 1, false, false)));
        user.getData().put("last_kit", id);
        user.setSpawn(false);
        user.msg("&eApplied kit &d" + ChatUtil.upperFirst(id) + "&e.");
    }

    public static Inventory getGUI(User user) {
        Inventory inv = Bukkit.createInventory(null, 36, ChatUtil.color("&6&lKit Selector"));
        ItemStack filler = new ItemBuilder(Material.STAINED_GLASS_PANE).setDura(15).setName(" ").build();
        for (int i = 0; i < 36; i++) {
            if (i < 9) {
                inv.setItem(i, filler);
                inv.setItem(i + 27, filler);
            } else if (i - 9 < KitType.values().length) {
                KitType type = KitType.values()[i - 9];
                ItemStack icon = type.getIcon().clone();
                ItemMeta meta = icon.getItemMeta();
                List<String> lore = meta.getLore();
                String desc = lore.get(0);
                lore.set(0, ChatUtil.color("&7&m----------------------------"));
                lore.add(desc);
                lore.add(" ");
                if (user.getKits().contains(type.name().toLowerCase())) {
                    lore.add(ChatUtil.color("&8(&aClick to select&8)"));
                } else {
                    lore.add(ChatUtil.color(String.format("&8(&aClick to purchase: &c$%s&8)", type.getPrice())));
                }
                lore.add(ChatUtil.color("&7&m----------------------------"));
                meta.setLore(lore);
                icon.setItemMeta(meta);
                inv.setItem(i, icon);
            } else {
                break;
            }
        }
        return inv;
    }
}
