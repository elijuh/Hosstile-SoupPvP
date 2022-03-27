package me.elijuh.soup.listeners;

import me.elijuh.soup.Core;
import me.elijuh.soup.data.HealType;
import me.elijuh.soup.data.perks.Perk;
import me.elijuh.soup.data.User;
import me.elijuh.soup.events.Event;
import me.elijuh.soup.events.EventType;
import me.elijuh.soup.events.arena.DuelEventArena;
import me.elijuh.soup.events.arena.FFAEventArena;
import me.elijuh.soup.data.kits.Kit;
import me.elijuh.soup.data.kits.KitType;
import me.elijuh.soup.util.ChatUtil;
import me.elijuh.soup.util.ItemBuilder;
import me.elijuh.soup.util.MathUtil;
import me.elijuh.soup.util.PlayerUtil;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class PlayerListener implements Listener {

    public PlayerListener() {
        Bukkit.getPluginManager().registerEvents(this, Core.i());
    }

    @EventHandler
    public void on(PlayerJoinEvent e) {
        User user = new User(e.getPlayer());
        Core.i().getUsers().add(user);
        user.p().teleport(Core.i().getSpawn().clone().subtract(0, 0, 1));
        Bukkit.getScheduler().runTaskLater(Core.i(), user::spawn, 5L);
    }

    @EventHandler
    public void on(PlayerQuitEvent e) {
        User user = Core.i().getUser(e.getPlayer().getName());
        if (user != null) {
            if (user.getCombat() > 0) {
                user.p().setHealth(0);
                user.spawn();
            }
            user.unload();
            Core.i().getUsers().remove(user);
        }
    }

    @EventHandler
    public void on(PlayerKickEvent e) {
        User user = Core.i().getUser(e.getPlayer().getName());
        if (user != null) {
            if (user.getCombat() > 0) {
                user.p().setHealth(0);
                user.spawn();
            }
            user.unload();
            Core.i().getUsers().remove(user);
        }
    }

    @EventHandler
    public void on(PlayerInteractEvent e) {
        User user = Core.i().getUser(e.getPlayer().getName());

        if (user == null) return;

        if (e.getClickedBlock() != null) {
            if (e.getClickedBlock().getState() instanceof Sign) {
                Sign sign = (Sign) e.getClickedBlock().getState();
                if (sign.getLine(0).equals(ChatUtil.color("&c[Refill]"))) {
                    Inventory inv = Bukkit.createInventory(null, 54);
                    ItemStack healing = user.getHealType().getItem();
                    for (int i = 0; i < inv.getSize(); i++) {
                        inv.setItem(i, healing);
                    }
                    user.p().openInventory(inv);
                    user.p().playSound(user.loc(), Sound.CLICK, 0.5f, 1f);
                }
            }
        }

        if (e.getItem() == null) return;

        if (e.getAction().toString().contains("RIGHT")) {
             if (e.getItem().getType() == Material.MUSHROOM_SOUP) {
                if (e.getItem().getItemMeta().getDisplayName() == null) {
                    if (e.getPlayer().getHealth() < e.getPlayer().getMaxHealth()) {
                        e.setCancelled(true);
                        e.getPlayer().setHealth(Math.min(e.getPlayer().getHealth() + 8, e.getPlayer().getMaxHealth()));
                        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.EAT, 0.5f, 1f);
                        e.getItem().setType(Material.BOWL);
                    }
                }
            }
            String name = e.getItem().getItemMeta().getDisplayName();
            if (name != null) {
                if (name.equals(ChatUtil.color("&eEvents"))) {
                    user.p().performCommand("event start");
                } else if (name.equals(ChatUtil.color("&eKit Selector"))) {
                    e.setCancelled(true);
                    e.getPlayer().openInventory(Kit.getGUI(user));
                } else if (name.startsWith(ChatUtil.color("&eCycle Healing"))){
                    e.setCancelled(true);
                    user.setHealType(user.getHealType() == HealType.SOUP ? HealType.POTS : HealType.SOUP);
                    user.msg("&eYou have changed your healing to &d" + ChatUtil.upperFirst(user.getHealType().name()) + "&e!");
                    ItemBuilder cycleHeal = new ItemBuilder(Material.MUSHROOM_SOUP).setName("&eCycle Healing: &d" + ChatUtil.upperFirst(user.getHealType().name()));
                    if (user.getHealType() == HealType.POTS) {
                        cycleHeal.setMaterial(Material.POTION).setDura(16421);
                    }
                    user.p().setItemInHand(cycleHeal.build());
                } else if (name.equals(ChatUtil.color("&ePerks"))) {
                    e.setCancelled(true);
                    user.p().openInventory(Perk.getGUI(user));
                } else if (name.equals(ChatUtil.color("&eLeaderboards &a&lNEW!"))) {
                    user.p().openInventory(Core.i().getLeaderboardGUI().getInv());
                    user.p().playSound(user.loc(), Sound.CLICK, 0.5f, 1f);
                }
            }
        }
    }

    @EventHandler
    public void on(PlayerDeathEvent e) {
        User killed = Core.i().getUser(e.getEntity().getName());
        if (killed == null) return;

        Bukkit.getScheduler().runTask(Core.i(), ()-> {
            e.getEntity().spigot().respawn();
            if (Event.getCurrent() == null || !Event.getCurrent().getUsers().contains(killed)) {
                killed.spawn();
            } else {
                if (Event.getCurrent().getArena() instanceof FFAEventArena) {
                    killed.p().teleport(((FFAEventArena) Event.getCurrent().getArena()).getSpectateArea());
                } else {
                    killed.p().teleport(Event.getCurrent().getArena().getSpawn());
                }
            }
        });
        e.getDrops().clear();
        e.setDeathMessage(null);

        User killer = e.getEntity().getKiller() == null ? null : Core.i().getUser(e.getEntity().getKiller().getName());

        if (killer != null) {
            if (!killer.isSpawn()) {
                int refiller = killer.getPerkLevel(Perk.REFILLER);
                if (refiller > 0) {
                    for (int i = 0; i < refiller; i++) {
                        killer.p().getInventory().addItem(killer.getHealType().getItem());
                    }
                    killer.msg("&6&lPerks &8⏐ &a+" + refiller + (killer.getHealType() == HealType.SOUP ? " Soup" : " Health Potion(s)") + " &7(Refiller Perk)");
                }
                if (MathUtil.chance(killer.getPerkLevel(Perk.VAMPIRE) * Perk.VAMPIRE.getChance())) {
                    killer.p().setHealth(killer.p().getMaxHealth());
                    killer.msg("&6&lPerks &8⏐ &aFull Heal &7(Vampire Perk)");
                }
            }
        }

        if (Event.getCurrent() != null) {
            Event.getCurrent().eliminate(killed);
        }

        if (Core.i().getConfig().getStringList("stat-effecting-worlds").contains(e.getEntity().getWorld().getName()) && killer != null) {
            if (killer == killed) {
                return;
            }
            killer.getData().put("kills", (int) killer.get("kills") + 1);
            killer.getData().put("streak", (int) killer.get("streak") + 1);

            int coins = 10 + ThreadLocalRandom.current().nextInt(20);
            if (killer.getPerkLevel(Perk.MONEY_MAKER) > 0) {
                int increase = killer.getPerkLevel(Perk.MONEY_MAKER) * Perk.MONEY_MAKER.getChance();
                coins += coins * (increase / 100);
                killer.msg("&6&lPerks &8⏐ &7You have recieved &a" + coins + " Coins &7for killing " + killed.coloredName() + " &7(Money Maker Perk: &a+" + increase + "%&7)");
            } else {
                killer.msg("&7You have recieved &a" + coins + " Coins &7for killing " + killed.coloredName() + "&7.");
            }
            if (killed.getBounty() > 0) {
                Bukkit.broadcastMessage(ChatUtil.color("&d" + killer.name() +
                        " &ehas claimed a bounty of &a$" + killed.getBounty() + " &efor killing &d" + killed.name()));
                killer.pay(killed.getBounty());
                killed.setBounty(0);
            }
            killer.pay(coins);
            killed.msg("&eYou were killed by &d" + killer.name() + " &ewith &c" + (int) (killer.p().getHealth() / 2) + "❤&e.");

            if (MathUtil.chance(killed.getPerkLevel(Perk.STREAK_SAVER) * Perk.STREAK_SAVER.getChance())) {
                killed.msg("&6&lPerks &8⏐ &aYou have kept your streak &7(Streak Saver Perk)");
            } else {
                int killedStreak = killed.get("streak");
                killed.getData().put("deaths", (int) killed.get("deaths") + 1);
                killed.getData().put("streak", 0);
                if (killedStreak > 20) {
                    Bukkit.broadcastMessage(ChatUtil.color("&d" + killer.name() +
                            " &ehas broken the killstreak of &d" + killed.name() + "! &8(&6&l" + killedStreak + "&8)"));
                }

                int streak = killer.get("streak");

                if (streak > 0 && streak % 10 == 0) {
                    Bukkit.broadcastMessage(ChatUtil.color("&d" + killer.name() +
                            " &eis on a high killstreak! &8(&6&l" + streak + "&8)"));
                }
            }
        }
    }

    @EventHandler
    public void on(EntityDamageEvent e) {
        if (e.getEntityType() == EntityType.PLAYER) {
            User user = Core.i().getUser(e.getEntity().getName());
            if (user != null) {
                if (user.isSpawn()) {
                    e.setCancelled(true);
                } else if (Event.getCurrent() != null && Event.getCurrent().getUsers().contains(user) && !Event.getCurrent().getRoundUsers().contains(user)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player) {
            if (Core.i().getSpawnRegion().contains(((Player) ((Projectile) e.getDamager()).getShooter()).getLocation())) {
                e.setCancelled(true);
            }
        }
        if (Core.i().getSpawnRegion().contains(e.getDamager().getLocation()) || Core.i().getSpawnRegion().contains(e.getEntity().getLocation())) {
            e.setCancelled(true);
        } else if (e.getEntity() instanceof Player) {
            User damager = null;
            if (e.getDamager() instanceof Player) {
                damager = Core.i().getUser(e.getDamager().getName());
            } else if (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player) {
                damager = Core.i().getUser(((Player) ((Projectile) e.getDamager()).getShooter()).getName());
            }
            User damaged = Core.i().getUser(e.getEntity().getName());

            if (damager == null || damaged == null) return;

            if (Event.getCurrent() != null && Event.getCurrent().getUsers().contains(damager)) {
                if (Event.getCurrent().getType() == EventType.FFA && Event.getCurrent().getDuration() <= 10) {
                    e.setCancelled(true);
                } else if (Event.getCurrent().getType() == EventType.SUMO) {
                    e.setDamage(0);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMonitor(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            User damager = null;
            if (e.getDamager() instanceof Player) {
                damager = Core.i().getUser(e.getDamager().getName());
            } else if (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player) {
                damager = Core.i().getUser(((Player) ((Projectile) e.getDamager()).getShooter()).getName());
            }
            User damaged = Core.i().getUser(e.getEntity().getName());

            if (damager == null || damaged == null) return;

            if (damager.getCombat() == 0) {
                damager.msg("&cYou are now in combat with &7" + damaged.p().getName() + "&c.");
            }
            if (damaged.getCombat() == 0) {
                damaged.msg("&cYou are now in combat with &7" + damager.p().getName() + "&c.");
            }
            damager.setCombat(200);
            damaged.setCombat(200);

            if (Event.getCurrent() == null || !Event.getCurrent().getUsers().contains(damager)) {
                if (MathUtil.chance(damager.getPerkLevel(Perk.THIEF) * Perk.THIEF.getChance())) {
                    for (int i = 0; i < 36; i++) {
                        ItemStack item = damaged.p().getInventory().getItem(i);
                        if (item != null && (item.getType() == Material.MUSHROOM_SOUP || (item.getType() == Material.POTION && item.getDurability() == 16421))) {
                            damaged.p().getInventory().setItem(i, null);
                            damaged.msg("&6&lPerks &8⏐ &c-1 " + (damaged.getHealType() == HealType.SOUP ? "Soup" : "Health Potion") + " &7(Opponent Thief Perk)");
                            break;
                        }
                    }
                    damager.p().getInventory().addItem(damager.getHealType().getItem());
                    damager.msg("&6&lPerks &8⏐ &a+1 " + (damager.getHealType() == HealType.SOUP ? "Soup" : "Health Potion") + " &7(Thief Perk)");
                }
                if (e.getDamager().getType() == EntityType.ARROW && damaged.getPerkLevel(Perk.ARROW_DEFENDER) > 0 && MathUtil.chance(50)) {
                    e.setDamage(e.getDamage() / 0.5);
                    damaged.msg("&6&lPerks &8⏐ &a-50% Damage Taken &7(Arrow Defender Perk)");
                    damager.msg("&6&lPerks &8⏐ &c-50% Damage Dealt &7(Opponent Arrow Defender Perk)");
                }
            }
        }
    }

    @EventHandler
    public void on(PlayerDropItemEvent e) {
        User user = Core.i().getUser(e.getPlayer().getName());
        if (e.getItemDrop().getItemStack().getType() == Material.BOWL) {
            e.getItemDrop().remove();
        } else if (user != null && (user.isSpawn() || Core.i().getSpawnRegion().contains(user.loc()))) {
            e.setCancelled(true);
        } else if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(ProjectileHitEvent e) {
        Bukkit.getScheduler().runTask(Core.i(), ()-> {
            if (!e.getEntity().isDead() && e.getEntity().getType() == EntityType.ARROW) {
                e.getEntity().remove();
            }
        });
    }

    @EventHandler
    public void on(ItemSpawnEvent e) {
        Bukkit.getScheduler().runTaskLater(Core.i(), ()-> {
            if (!e.getEntity().isDead()) {
                e.getEntity().remove();
            }
        }, 600L);
    }

    @EventHandler
    public void on(PlayerPickupItemEvent e) {
        User user = Core.i().getUser(e.getPlayer().getName());
        if (user != null && user.isSpawn()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(FoodLevelChangeEvent e) {
        e.setFoodLevel(40);
    }

    @EventHandler
    public void on(InventoryClickEvent e) {
        User user = Core.i().getUser(e.getWhoClicked().getName());
        if (user != null) {
            if (user.isSpawn() && user.p().getGameMode() != GameMode.CREATIVE && !user.p().hasMetadata("staffmode")) {
                e.setCancelled(true);
            }
            if (e.getView().getTitle().equals(ChatUtil.color("&6&lKit Selector"))) {
                e.setCancelled(true);
                if (e.getRawSlot() > 8 && e.getRawSlot() - 9 < KitType.values().length) {
                    Kit kit = Core.i().getKit(KitType.values()[e.getRawSlot() - 9].name().toLowerCase());
                    if (user.getKits().contains(kit.getId())) {
                        kit.apply(user);
                    } else if (user.charge(kit.getType().getPrice())) {
                        user.p().playSound(user.loc(), Sound.ORB_PICKUP, 0.5f, 1f);
                        user.msg(String.format("&eYou have purchased &d%s&e.", ChatUtil.upperFirst(kit.getId())));
                        user.getKits().add(kit.getId());
                    } else {
                        user.msg("&cYou cannot afford that kit.");
                    }
                    e.getView().close();
                }
            } else if (e.getView().getTitle().equals(ChatUtil.color("&6&lPick Event"))) {
                e.setCancelled(true);
                for (EventType type : EventType.values()) {
                    if (type.getIcon().equals(e.getCurrentItem())) {
                        if (Event.getCurrent() == null) {
                            if (user.charge(type.getPrice())) {
                                new Event(user, type);
                                user.p().playSound(user.loc(), Sound.CLICK, 1f, 1f);
                            } else {
                                user.msg("&cYou cannot afford that.");
                            }
                        } else {
                            user.msg("&cAn event is already active.");
                        }
                        e.getView().close();
                        break;
                    }
                }
            } else if (e.getView().getTitle().equals(ChatUtil.color("&6&lPerks"))) {
                e.setCancelled(true);
                if (e.getRawSlot() > 8 && e.getRawSlot() - 9 < Perk.values().length) {
                    Perk perk = Perk.values()[e.getRawSlot() - 9];
                    if (user.getPerkLevel(perk) < perk.getMaxLevel()) {
                        if (user.charge(perk.getPriceIncrease() * (user.getPerkLevel(perk) + 1))) {
                            user.p().playSound(user.loc(), Sound.ORB_PICKUP, 1f, 2f);
                            user.getPerks().put(perk.name(), user.getPerkLevel(perk) + 1);
                            user.p().openInventory(Perk.getGUI(user));
                        }
                    }
                }
            }
        }
        if (e.getView().getTitle().equals(ChatUtil.color("&6&lLeaderboards"))) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(PlayerMoveEvent e) {
        Location t = e.getTo();
        Location f = e.getFrom();
        User user = Core.i().getUser(e.getPlayer().getName());
        if (user != null) {
            if (user.getSpawnTask() != null && t.toVector().distance(f.toVector()) > 0.1) {
                user.getSpawnTask().cancel();
                PlayerUtil.sendActionBar(user.p(), "&cYou moved, teleport cancelled!");
                user.msg("&cTeleport cancelled due to movement.");
            }

            if (user.isSpawn() && user.p().getGameMode() != GameMode.CREATIVE && !user.p().hasMetadata("staffmode")
                    && e.getFrom().getWorld().getName().equals(Core.i().getConfig().getString("spawn-region.world"))) {
                if (Core.i().getSpawnRegion().contains(f) && !Core.i().getSpawnRegion().contains(t)) {
                    Kit kit = Core.i().getKit(user.get("last_kit"));
                    kit.apply(user);
                }
            }

            if (Event.getCurrent() != null && Event.getCurrent().getRoundUsers().contains(user) && Event.getCurrent().getType() == EventType.SUMO) {
                DuelEventArena arena = (DuelEventArena) Event.getCurrent().getArena();
                if (arena.getRegion().contains(f) && !arena.getRegion().contains(t)) {
                    Event.getCurrent().eliminate(user);
                }
            }

            if (user.p().hasMetadata("immobile") && f.toVector().distance(t.toVector()) > 0) {
                user.p().teleport(f);
            } else if (Core.i().getSpawnRegion().contains(t) && !Core.i().getSpawnRegion().contains(f)) {
                if (user.getCombat() > 0) {
                    user.p().teleport(f);
                } else {
                    user.spawn(false);
                }
            }
            if (t.getBlockX() != f.getBlockX() || t.getBlockY() != f.getBlockY() || t.getBlockZ() != f.getBlockZ()) {
                user.refreshCombatTagForceField(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(PlayerTeleportEvent e) {
        Location t = e.getTo();
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            if (Core.i().getSpawnRegion().contains(t)) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatUtil.color("&cYou cannot ender pearl into spawn."));
                e.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
            }
        }
    }

    @EventHandler
    public void on(SignChangeEvent e) {
        if (e.getPlayer().hasPermission("soup.refillsign")) {
            if (e.getLine(0).equalsIgnoreCase("[refill]")) {
                e.setLine(0, ChatUtil.color("&c[Refill]"));
                e.setLine(1, ChatUtil.color("(Right-Click)"));
                e.setLine(2, "");
                e.setLine(3, "");
            }
        }
    }
}
