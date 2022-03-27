package me.elijuh.soup.events;

import lombok.Getter;
import me.elijuh.soup.data.Cuboid;
import me.elijuh.soup.events.arena.DuelEventArena;
import me.elijuh.soup.events.arena.EventArena;
import me.elijuh.soup.events.arena.FFAEventArena;
import me.elijuh.soup.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum EventType {
    FFA(150, "FFA", new ItemBuilder(Material.DIAMOND_SWORD).addFlag(ItemFlag.HIDE_ATTRIBUTES)),
    SUMO(100, "Sumo", new ItemBuilder(Material.STICK)),
    DUELS(125, "1v1s", new ItemBuilder(Material.SIGN));

    private final FileConfiguration config = EventConfiguration.getInstance() == null ? new EventConfiguration().getConfig()
            : EventConfiguration.getInstance().getConfig();
    private final List<EventArena> arenas = new ArrayList<>();
    private final int price;
    private final String display;
    private final ItemStack icon;

    EventType(int price, String display, ItemBuilder icon) {
        this.price = price;
        this.display = display;
        this.icon = icon.setName("&a" + display).addLore(" ").addLore("&8(&aClick to start: &c$" + price + "&8)").build();
        config.options().copyDefaults(true);
        config.addDefault("events." + id() + ".max-players", 24);
        config.addDefault("events." + id() + ".arenas.default.world", "eventworld");
        config.addDefault("events." + id() + ".arenas.default.spawn.x", 0.0);
        config.addDefault("events." + id() + ".arenas.default.spawn.y", 0.0);
        config.addDefault("events." + id() + ".arenas.default.spawn.z", 0.0);
        config.addDefault("events." + id() + ".arenas.default.spawn.yaw", 0.0);
        config.addDefault("events." + id() + ".arenas.default.spawn.pitch", 0.0);
        config.addDefault("events." + id() + ".arenas.default.region.minX", 0);
        config.addDefault("events." + id() + ".arenas.default.region.minY", 0);
        config.addDefault("events." + id() + ".arenas.default.region.minZ", 0);
        config.addDefault("events." + id() + ".arenas.default.region.maxX", 0);
        config.addDefault("events." + id() + ".arenas.default.region.maxY", 0);
        config.addDefault("events." + id() + ".arenas.default.region.maxZ", 0);
        if (id().equals("sumo") || id().equals("duels")) {
            config.addDefault("events." + id() + ".arenas.default.player-one.x", 0.0);
            config.addDefault("events." + id() + ".arenas.default.player-one.y", 0.0);
            config.addDefault("events." + id() + ".arenas.default.player-one.z", 0.0);
            config.addDefault("events." + id() + ".arenas.default.player-one.yaw", 0.0);
            config.addDefault("events." + id() + ".arenas.default.player-one.pitch", 0.0);
            config.addDefault("events." + id() + ".arenas.default.player-two.x", 0.0);
            config.addDefault("events." + id() + ".arenas.default.player-two.y", 0.0);
            config.addDefault("events." + id() + ".arenas.default.player-two.z", 0.0);
            config.addDefault("events." + id() + ".arenas.default.player-two.yaw", 0.0);
            config.addDefault("events." + id() + ".arenas.default.player-two.pitch", 0.0);
        } else {
            config.addDefault("events." + id() + ".arenas.default.spectate.x", 0.0);
            config.addDefault("events." + id() + ".arenas.default.spectate.y", 0.0);
            config.addDefault("events." + id() + ".arenas.default.spectate.z", 0.0);
            config.addDefault("events." + id() + ".arenas.default.spectate.yaw", 0.0);
            config.addDefault("events." + id() + ".arenas.default.spectate.pitch", 0.0);
        }
        for (String name : config.getConfigurationSection("events." + id() + ".arenas").getKeys(false)) {
            EventArena arena;
            World world = Bukkit.getWorld(config.getString("events." + id() + ".arenas." + name + ".world"));
            if (config.contains("events." + id() + ".arenas.default.player-one.x")) {
                arena = new DuelEventArena(name, new Cuboid(
                                config.getInt("events." + id() + ".arenas." + name + ".region.minX"),
                                config.getInt("events." + id() + ".arenas." + name + ".region.minY"),
                                config.getInt("events." + id() + ".arenas." + name + ".region.minZ"),
                                config.getInt("events." + id() + ".arenas." + name + ".region.maxX"),
                                config.getInt("events." + id() + ".arenas." + name + ".region.maxY"),
                                config.getInt("events." + id() + ".arenas." + name + ".region.maxZ")
                        ), new Location(world,
                                config.getDouble("events." + id() + ".arenas." + name + ".spawn.x"),
                                config.getDouble("events." + id() + ".arenas." + name + ".spawn.y"),
                                config.getDouble("events." + id() + ".arenas." + name + ".spawn.z"),
                                (float) config.getDouble("events." + id() + ".arenas." + name + ".spawn.yaw"),
                                (float) config.getDouble("events." + id() + ".arenas." + name + ".spawn.pitch")
                        ), new Location(world,
                                config.getDouble("events." + id() + ".arenas." + name + ".player-one.x"),
                                config.getDouble("events." + id() + ".arenas." + name + ".player-one.y"),
                                config.getDouble("events." + id() + ".arenas." + name + ".player-one.z"),
                                (float) config.getDouble("events." + id() + ".arenas." + name + ".player-one.yaw"),
                                (float) config.getDouble("events." + id() + ".arenas." + name + ".player-one.pitch")
                        ), new Location(world,
                                config.getDouble("events." + id() + ".arenas." + name + ".player-two.x"),
                                config.getDouble("events." + id() + ".arenas." + name + ".player-two.y"),
                                config.getDouble("events." + id() + ".arenas." + name + ".player-two.z"),
                                (float) config.getDouble("events." + id() + ".arenas." + name + ".player-two.yaw"),
                                (float) config.getDouble("events." + id() + ".arenas." + name + ".player-two.pitch")
                        )
                );
            } else {
                arena = new FFAEventArena(name, new Cuboid(
                                config.getInt("events." + id() + ".arenas." + name + ".region.minX"),
                                config.getInt("events." + id() + ".arenas." + name + ".region.minY"),
                                config.getInt("events." + id() + ".arenas." + name + ".region.minZ"),
                                config.getInt("events." + id() + ".arenas." + name + ".region.maxX"),
                                config.getInt("events." + id() + ".arenas." + name + ".region.maxY"),
                                config.getInt("events." + id() + ".arenas." + name + ".region.maxZ")
                        ), new Location(world,
                                config.getDouble("events." + id() + ".arenas." + name + ".spawn.x"),
                                config.getDouble("events." + id() + ".arenas." + name + ".spawn.y"),
                                config.getDouble("events." + id() + ".arenas." + name + ".spawn.z"),
                                (float) config.getDouble("events." + id() + ".arenas." + name + ".spawn.yaw"),
                                (float) config.getDouble("events." + id() + ".arenas." + name + ".spawn.pitch")
                        ), new Location(world,
                        config.getDouble("events." + id() + ".arenas." + name + ".spectate.x"),
                        config.getDouble("events." + id() + ".arenas." + name + ".spectate.y"),
                        config.getDouble("events." + id() + ".arenas." + name + ".spectate.z"),
                        (float) config.getDouble("events." + id() + ".arenas." + name + ".spectate.yaw"),
                        (float) config.getDouble("events." + id() + ".arenas." + name + ".spectate.pitch")
                ));
            }
            arenas.add(arena);
        }
        EventConfiguration.getInstance().save();
    }

    public int getMaxPlayers() {
        return config.getInt("events." + id() + ".max-players");
    }

    public String id() {
        return name().toLowerCase();
    }
}
