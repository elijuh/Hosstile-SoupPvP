package me.elijuh.soup.commands.impl.other;

import com.google.common.collect.ImmutableList;
import me.elijuh.soup.Core;
import me.elijuh.soup.commands.SpigotCommand;
import me.elijuh.soup.data.User;
import me.elijuh.soup.util.ChatUtil;
import me.elijuh.soup.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BountyCommand extends SpigotCommand {
    private final Inventory bounties = Bukkit.createInventory(null, 54, ChatUtil.color("&6&lBounties"));
    private final ItemStack filler = new ItemBuilder(Material.STAINED_GLASS_PANE).setDura(15).setName(" ").build();

    public BountyCommand() {
        super("bounty");

        new SpigotCommand("bounties") {

            @Override
            public List<String> onTabComplete(Player p, String[] args) {
                return ImmutableList.of();
            }

            @Override
            public void onExecute(Player p, String[] args) {
                bounties.clear();
                for (int i = 0; i < 9; i++) {
                    bounties.setItem(i, filler);
                    bounties.setItem(i + 45, filler);
                }
                int index = 9;
                for (User user : Core.i().getUsers()) {
                    if (user.getBounty() > 0 && !user.p().hasMetadata("vanish")) {
                        bounties.setItem(index++, new ItemBuilder(Material.GOLD_INGOT).setName(user.coloredName()).addLore("&7Bounty: &a$" + user.getBounty()).build());
                    }
                }
                p.openInventory(bounties);
            }
        };
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return args.length == 1 ? null : ImmutableList.of();
    }

    @Override
    public void onExecute(Player p, String[] args) {
        User user = Core.i().getUser(p.getName());
        if (user == null) return;

        if (args.length == 2) {
            int coins;
            try {
                coins = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                user.msg("&cInvalid integer: " + args[1]);
                return;
            }
            User target = Core.i().getUser(args[0]);
            if (target != null) {
                if (user == target) {
                    user.msg("&cYou cannot bounty yourself.");
                }
                if (coins >= target.getBounty() + 100) {
                    if (user.charge(coins)) {
                        target.setBounty(coins);
                        Bukkit.broadcastMessage(ChatUtil.color("&d" + user.coloredName() + " &ehas placed a bounty of &a$" + coins +
                                " &eon " + target.coloredName() + "&e!"));
                    } else {
                        user.msg("&cYou can't afford that.");
                    }
                } else {
                    user.msg("&7Bounty must be atleast &c$100 &7more than current: &a$" + target.getBounty());
                }
            } else {
                user.msg("&cThat player is not online.");
            }
        } else {
            user.msg("&cUsage: /bounty <player> <coins>");
        }
    }
}
