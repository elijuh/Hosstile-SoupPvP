package me.elijuh.soup.data.perks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.elijuh.soup.data.User;
import me.elijuh.soup.util.ChatUtil;
import me.elijuh.soup.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public enum Perk {
    VAMPIRE(PerkType.CHANCE, Material.REDSTONE, "Vampire", "Fully heal on kills.", 125, 20),
    THIEF(PerkType.CHANCE, Material.HOPPER, "Thief", "Steal healing on attack.", 100, 3),
    REFILLER(PerkType.LEVEL, Material.MUSHROOM_SOUP, "Refiller", "Gain healing on kills.", 200, 5),
    MONEY_MAKER(PerkType.INCREASE, Material.GOLD_INGOT, "Money Maker", "Receive more coins on kills.", 50, 10),
    ARROW_DEFENDER(PerkType.UNLOCK, Material.ARROW, "Arrow Defender", "Take 50% less damage from arrows.", 250, 1),
    STREAK_SAVER(PerkType.CHANCE, Material.BLAZE_POWDER, "Streak Saver", "Chance to save your streak on dying.", 1000, 5);

    private final PerkType type;
    private final Material iconType;
    private final String display, description;
    private final int priceIncrease;
    private final int maxLevel;

    public int getChance() {
        switch (this) {
            case VAMPIRE: {
                return 5;
            }
            case THIEF:
            case STREAK_SAVER: {
                return 1;
            }
            case MONEY_MAKER: {
                return 10;
            }
            default: {
                return 0;
            }
        }
    }

    private String getDisplayStat(int level) {
        int chance = getChance();
        String percentage = level < maxLevel ? "&f" + level * chance + "% &7» &a" + (level + 1) * chance + "%" : "&a" + level * chance + "% &8(&a&lMaxed&8)";
        switch (type) {
            case CHANCE: {
                return "&eChance: " + percentage;
            }
            case LEVEL: {
                return "&eLevel: " + (level < maxLevel ? "&f" + level + " &7» &a" + (level + 1) : "&a" + level + " &8(&a&lMaxed&8)");
            }
            case INCREASE: {
                return "&eIncrease: " + percentage;
            }
            case UNLOCK: {
                return "&eState: " + (level < maxLevel ? "&cLocked &7» &aUnlocked" : "&aUnlocked");
            }
            default: {
                return "";
            }
        }
    }

    private ItemStack getIcon(User user) {
        int level = user.getPerkLevel(this);
        return new ItemBuilder(iconType).setName("&a" + display)
                .addLore("&7&m----------------------------")
                .addLore("&7" + description)
                .addLore(" ")
                .addLore(getDisplayStat(level))
                .addLore(level < maxLevel ? " " : "")
                .addLore(level < maxLevel ? "&8(&aClick to upgrade: &c$" + priceIncrease * (level + 1) + "&8)" : "")
                .addLore("&7&m----------------------------").build();
    }

    public static Inventory getGUI(User user) {
        Inventory inv = Bukkit.createInventory(null, 36, ChatUtil.color("&6&lPerks"));
        ItemStack filler = new ItemBuilder(Material.STAINED_GLASS_PANE).setDura(15).setName(" ").build();
        for (int i = 0; i < 36; i++) {
            if (i < 9) {
                inv.setItem(i, filler);
                inv.setItem(i + 27, filler);
            } else if (i - 9 < Perk.values().length) {
                Perk perk = Perk.values()[i - 9];
                inv.setItem(i, perk.getIcon(user));
            } else {
                break;
            }
        }
        return inv;
    }
}
