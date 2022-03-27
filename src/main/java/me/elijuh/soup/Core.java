package me.elijuh.soup;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.elijuh.soup.commands.SpigotCommand;
import me.elijuh.soup.commands.impl.admin.SetCoinsCommand;
import me.elijuh.soup.commands.impl.admin.SetSpawnCommand;
import me.elijuh.soup.commands.impl.other.*;
import me.elijuh.soup.data.Cuboid;
import me.elijuh.soup.data.User;
import me.elijuh.soup.data.kits.Kit;
import me.elijuh.soup.data.kits.KitType;
import me.elijuh.soup.db.MongoManager;
import me.elijuh.soup.gui.LeaderboardGUI;
import me.elijuh.soup.listeners.PlayerListener;
import me.elijuh.soup.scoreboard.DefaultScoreboard;
import me.elijuh.soup.scoreboard.StaffScoreboard;
import me.elijuh.soup.util.ItemBuilder;
import me.elijuh.soup.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Core extends JavaPlugin {
    private final List<User> users = new ArrayList<>();
    private final List<Kit> kits = new ArrayList<>();
    private static Core instance;
    private MongoManager mongoManager;
    private LeaderboardGUI leaderboardGUI;
    private Location spawn;
    private Cuboid spawnRegion;

    public void onEnable() {
        instance = this;
        getConfig().options().copyDefaults(true);
        getConfig().addDefault("mongodb.connection-string", "");
        World world = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == World.Environment.NORMAL).findFirst().orElse(Bukkit.getWorld("world"));
        spawn = world.getSpawnLocation();
        getConfig().addDefault("spawn", spawn.getWorld().getName() + ";" + spawn.getX() + ";" + spawn.getY()
            + ";" + spawn.getZ() + ";" + spawn.getYaw() + ";" + spawn.getPitch());
        getConfig().addDefault("spawn-region.world", "world");
        getConfig().addDefault("spawn-region.min.x", 0);
        getConfig().addDefault("spawn-region.min.y", 0);
        getConfig().addDefault("spawn-region.min.z", 0);
        getConfig().addDefault("spawn-region.max.x", 0);
        getConfig().addDefault("spawn-region.max.y", 0);
        getConfig().addDefault("spawn-region.max.z", 0);
        Core.i().getConfig().addDefault("stat-effecting-worlds", Lists.newArrayList("worlds_go_here"));
        saveConfig();

        mongoManager = new MongoManager();
        leaderboardGUI = new LeaderboardGUI();

        String[] spawnString = getConfig().getString("spawn").split(";");
        spawn = new Location(
                Bukkit.getWorld(spawnString[0]),
                Double.parseDouble(spawnString[1]),
                Double.parseDouble(spawnString[2]),
                Double.parseDouble(spawnString[3]),
                Float.parseFloat(spawnString[4]),
                Float.parseFloat(spawnString[5])
        );

        spawnRegion = new Cuboid(
                getConfig().getInt("spawn-region.min.x"),
                getConfig().getInt("spawn-region.min.y"),
                getConfig().getInt("spawn-region.min.z"),
                getConfig().getInt("spawn-region.max.x"),
                getConfig().getInt("spawn-region.max.y"),
                getConfig().getInt("spawn-region.max.z")
        );

        for (KitType type : KitType.values()) {
            kits.add(new Kit(type));
        }

        setKitItems();

        new SoupExpansion().register();

        new PlayerListener();

        new SetSpawnCommand();
        new EventCommand();
        new SpawnCommand();
        new StatsCommand();
        new SetCoinsCommand();
        new LeaderboardCommand();
        new BountyCommand();

        for (Player p : Bukkit.getOnlinePlayers()) {
            users.add(new User(p));
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(Core.i(), ()-> {
            for (User user : users) {
                if (user.getCombat() > 0) {
                    user.setCombat(user.getCombat() - 1);

                    if (user.getCombat() == 0) {
                        user.msg("&aYou are no longer in combat.");
                        user.refreshCombatTagForceField(true);
                    }
                }
                user.getScoreboard().refresh();
            }
        }, 2L, 2L);

        Bukkit.getScheduler().runTaskTimer(this, ()-> {
            for (User user : users) {
                if (user.p().hasMetadata("staffmode")) {
                    if ((user.getScoreboard() instanceof DefaultScoreboard)) {
                        user.setScoreboard(new StaffScoreboard(user.p()));
                    }
                } else if (user.getScoreboard() instanceof StaffScoreboard) {
                    user.setScoreboard(new DefaultScoreboard(user.p()));
                }
            }
        }, 2L, 2L);
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
        getConfig().set("spawn", spawn.getWorld().getName() + ";" + spawn.getX() + ";" + spawn.getY()
                + ";" + spawn.getZ() + ";" + spawn.getYaw() + ";" + spawn.getPitch());
        saveConfig();
    }

    public void onDisable() {
        SoupExpansion.i().unregister();
        for (User user : users) {
            user.unload();
        }

        try {
            CommandMap map = (CommandMap) ReflectionUtil.getField(Bukkit.getServer().getClass(), "commandMap").get(Bukkit.getServer());
            for (String command : SpigotCommand.getRegisteredCommands()) {
                ReflectionUtil.unregisterCommands(map, command);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        users.clear();
        mongoManager.getClient().close();
    }

    public static Core i() {
        return instance;
    }

    public Kit getKit(String name) {
        for (Kit kit : kits) {
            if (kit.getId().equalsIgnoreCase(name)) {
                return kit;
            }
        }
        return null;
    }

    public User getUser(String name) {
        for (User user : users) {
            if (user.p().getName().equalsIgnoreCase(name)) {
                return user;
            }
        }
        return null;
    }

    private void setKitItems() {
        getKit("default").getItems().put(39, new ItemBuilder(Material.IRON_HELMET).build());
        getKit("default").getItems().put(38, new ItemBuilder(Material.IRON_CHESTPLATE).build());
        getKit("default").getItems().put(37, new ItemBuilder(Material.IRON_LEGGINGS).build());
        getKit("default").getItems().put(36, new ItemBuilder(Material.IRON_BOOTS).build());
        getKit("default").getItems().put(0, new ItemBuilder(Material.DIAMOND_SWORD).addEnchant(Enchantment.DURABILITY, 1).build());

        getKit("archer").getItems().put(39, new ItemBuilder(Material.LEATHER_HELMET).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).addEnchant(Enchantment.DURABILITY, 1).build());
        getKit("archer").getItems().put(38, new ItemBuilder(Material.LEATHER_CHESTPLATE).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).addEnchant(Enchantment.DURABILITY, 1).build());
        getKit("archer").getItems().put(37, new ItemBuilder(Material.LEATHER_LEGGINGS).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).addEnchant(Enchantment.DURABILITY, 1).build());
        getKit("archer").getItems().put(36, new ItemBuilder(Material.LEATHER_BOOTS).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).addEnchant(Enchantment.DURABILITY, 1).build());
        getKit("archer").getItems().put(17, new ItemBuilder(Material.ARROW).build());
        getKit("archer").getItems().put(8, new ItemBuilder(Material.BOW).addEnchant(Enchantment.ARROW_INFINITE, 1).build());
        getKit("archer").getItems().put(0, new ItemBuilder(Material.STONE_SWORD).addEnchant(Enchantment.DAMAGE_ALL, 1).addEnchant(Enchantment.DURABILITY, 1).build());

        getKit("blaze").getItems().put(39, new ItemBuilder(Material.LEATHER_HELMET).addEnchant(Enchantment.DURABILITY, 2).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).setColor(0xFFA500).build());
        getKit("blaze").getItems().put(38, new ItemBuilder(Material.IRON_CHESTPLATE).build());
        getKit("blaze").getItems().put(37, new ItemBuilder(Material.IRON_LEGGINGS).build());
        getKit("blaze").getItems().put(36, new ItemBuilder(Material.IRON_BOOTS).build());
        getKit("blaze").getItems().put(0, new ItemBuilder(Material.IRON_SWORD).addEnchant(Enchantment.FIRE_ASPECT, 1).build());

        getKit("cactus").getItems().put(39, new ItemBuilder(Material.LEATHER_HELMET).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).addEnchant(Enchantment.THORNS, 1).setColor(0x006400).build());
        getKit("cactus").getItems().put(38, new ItemBuilder(Material.IRON_CHESTPLATE).build());
        getKit("cactus").getItems().put(37, new ItemBuilder(Material.LEATHER_LEGGINGS).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).addEnchant(Enchantment.THORNS, 1).setColor(0x006400).build());
        getKit("cactus").getItems().put(36, new ItemBuilder(Material.LEATHER_BOOTS).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).addEnchant(Enchantment.THORNS, 1).setColor(0x006400).build());
        getKit("cactus").getItems().put(0, new ItemBuilder(Material.STONE_SWORD).addEnchant(Enchantment.DAMAGE_ALL, 2).build());

        getKit("fisherman").getItems().put(39, new ItemBuilder(Material.GOLD_HELMET).build());
        getKit("fisherman").getItems().put(38, new ItemBuilder(Material.IRON_CHESTPLATE).build());
        getKit("fisherman").getItems().put(37, new ItemBuilder(Material.IRON_LEGGINGS).build());
        getKit("fisherman").getItems().put(36, new ItemBuilder(Material.GOLD_BOOTS).build());
        getKit("fisherman").getItems().put(1, new ItemBuilder(Material.FISHING_ROD).setName("&cFisherman's Rod").build());
        getKit("fisherman").getItems().put(0, new ItemBuilder(Material.DIAMOND_SWORD).addEnchant(Enchantment.DURABILITY, 1).build());
    }
}
