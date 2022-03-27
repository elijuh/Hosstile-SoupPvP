package me.elijuh.soup.commands.impl.other;

import com.google.common.collect.ImmutableList;
import me.elijuh.soup.Core;
import me.elijuh.soup.commands.SpigotCommand;
import me.elijuh.soup.data.User;
import me.elijuh.soup.events.Event;
import me.elijuh.soup.events.EventConfiguration;
import me.elijuh.soup.events.EventState;
import me.elijuh.soup.events.EventType;
import me.elijuh.soup.util.ChatUtil;
import me.elijuh.soup.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EventCommand extends SpigotCommand {
    private final Inventory selector = Bukkit.createInventory(null, 27, ChatUtil.color("&6&lPick Event"));

    public EventCommand() {
        super("event");
        ItemStack filler = new ItemBuilder(Material.STAINED_GLASS_PANE).setDura(15).setName(" ").build();
        for (int i = 0; i < 27; i++) {
            selector.setItem(i, filler);
        }
        selector.setItem(12, EventType.FFA.getIcon());
        selector.setItem(13, EventType.SUMO.getIcon());
        selector.setItem(14, EventType.DUELS.getIcon());
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return ImmutableList.of();
    }

    @Override
    public void onExecute(Player p, String[] args) {
        User user = Core.i().getUser(p.getName());
        if (user == null) return;
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "join": {
                    Event event = Event.getCurrent();
                    if (event == null) {
                        user.msg("&cThere is no current event.");
                    } else if (event.getState() != EventState.STARTING) {
                        user.msg("&cThe event is already active, use /event spectate to watch.");
                    } else if (event.getUsers().contains(user)) {
                        user.msg("&cYou are already in the event.");
                    } else if (!user.isSpawn()) {
                        user.msg("&cYou must be in spawn to join the event.");
                    } else {
                        event.add(user);
                    }
                    break;
                }
                case "leave": {
                    Event event = Event.getCurrent();
                    if (event == null || !event.getUsers().contains(user)) {
                        user.msg("&cYou are not in an event.");
                    } else {
                        event.remove(user);
                    }
                    break;
                }
                case "start": {
                    if (!user.isSpawn()) {
                        user.msg("&cYou must be in spawn to start an event.");
                    } else {
                        user.p().openInventory(selector);
                    }
                    break;
                }
                case "spectate": {
                    Event event = Event.getCurrent();
                    if (event == null || event.getState() == EventState.STARTING) {
                        user.msg("&cThere is no event to spectate.");
                    } else if (event.getUsers().contains(user)) {
                        user.msg("&cYou are already spectating.");
                    } else if (!user.isSpawn()) {
                        user.msg("&cYou must be in spawn to spectate the event.");
                    } else {
                        event.add(user);
                    }
                    break;
                }
                case "forcestart": {
                    if (p.hasPermission("events.admin")) {
                        if (Event.getCurrent() != null) {
                            try {
                                Event.getCurrent().start();
                                user.msg("&a&lSuccess!");
                            } catch (IllegalStateException e) {
                                user.msg("&cEvent already active.");
                            }
                        }
                        break;
                    }
                }
                case "setspawn": {
                    if (p.hasPermission("events.admin") && args.length == 3) {
                        try {
                            EventType type = EventType.valueOf(args[1].toUpperCase());
                            String name = args[2];
                            Location loc = p.getLocation();
                            FileConfiguration config = EventConfiguration.getInstance().getConfig();
                            config.set("events." + type.id() + ".arenas." + name + ".world", loc.getWorld().getName());
                            config.set("events." + type.id() + ".arenas." + name + ".spawn.x", loc.getX());
                            config.set("events." + type.id() + ".arenas." + name + ".spawn.y", loc.getY());
                            config.set("events." + type.id() + ".arenas." + name + ".spawn.z", loc.getZ());
                            config.set("events." + type.id() + ".arenas." + name + ".spawn.yaw", loc.getYaw());
                            config.set("events." + type.id() + ".arenas." + name + ".spawn.pitch", loc.getPitch());
                            EventConfiguration.getInstance().save();
                            user.msg("&a&lSuccess!");
                        } catch (IllegalArgumentException e) {
                            user.msg("&cNo such event type.");
                        }
                        break;
                    }
                }
                case "setregion": {
                    if (p.hasPermission("events.admin") && args.length == 4) {
                        try {
                            if (!args[3].equals("min") && !args[3].equals("max")) {
                                throw new IllegalArgumentException("min or max needs to be specified");
                            }
                            EventType type = EventType.valueOf(args[1].toUpperCase());
                            String name = args[2];
                            Location loc = p.getLocation();
                            FileConfiguration config = EventConfiguration.getInstance().getConfig();
                            config.set("events." + type.id() + ".arenas." + name + ".region." + args[3] + "X", loc.getBlockX());
                            config.set("events." + type.id() + ".arenas." + name + ".region." + args[3] + "Y", loc.getBlockY());
                            config.set("events." + type.id() + ".arenas." + name + ".region." + args[3] + "Z", loc.getBlockZ());
                            EventConfiguration.getInstance().save();
                            user.msg("&a&lSuccess!");
                        } catch (IllegalArgumentException e) {
                            user.msg("&cInvalid syntax.");
                        }
                        break;
                    }
                }
                case "setplayer": {
                    if (p.hasPermission("events.admin") && args.length == 4) {
                        try {
                            if (!args[3].equals("one") && !args[3].equals("two")) {
                                throw new IllegalArgumentException("one or two needs to be specified");
                            }
                            EventType type = EventType.valueOf(args[1].toUpperCase());
                            String name = args[2];
                            Location loc = p.getLocation();
                            FileConfiguration config = EventConfiguration.getInstance().getConfig();
                            config.set("events." + type.id() + ".arenas." + name + ".player-" + args[3] + ".x", loc.getX());
                            config.set("events." + type.id() + ".arenas." + name + ".player-" + args[3] + ".y", loc.getY());
                            config.set("events." + type.id() + ".arenas." + name + ".player-" + args[3] + ".z", loc.getZ());
                            config.set("events." + type.id() + ".arenas." + name + ".player-" + args[3] + ".yaw", loc.getYaw());
                            config.set("events." + type.id() + ".arenas." + name + ".player-" + args[3] + ".pitch", loc.getPitch());
                            EventConfiguration.getInstance().save();
                            user.msg("&a&lSuccess!");
                        } catch (IllegalArgumentException e) {
                            user.msg("&cInvalid syntax.");
                        }
                        break;
                    }
                }
                case "setspectate": {
                    if (p.hasPermission("events.admin") && args.length == 3) {
                        try {
                            EventType type = EventType.valueOf(args[1].toUpperCase());
                            String name = args[2];
                            Location loc = p.getLocation();
                            FileConfiguration config = EventConfiguration.getInstance().getConfig();
                            config.set("events." + type.id() + ".arenas." + name + ".spectate.x", loc.getX());
                            config.set("events." + type.id() + ".arenas." + name + ".spectate.y", loc.getY());
                            config.set("events." + type.id() + ".arenas." + name + ".spectate.z", loc.getZ());
                            config.set("events." + type.id() + ".arenas." + name + ".spectate.yaw", loc.getYaw());
                            config.set("events." + type.id() + ".arenas." + name + ".spectate.pitch", loc.getPitch());
                            EventConfiguration.getInstance().save();
                            user.msg("&a&lSuccess!");
                        } catch (IllegalArgumentException e) {
                            user.msg("&cInvalid syntax.");
                        }
                        break;
                    }
                }
                default: {
                    user.msg("&7&m--------------------");
                    user.msg("&6&lEvent Help:");
                    user.msg(" &e/event join");
                    user.msg(" &e/event leave");
                    user.msg(" &e/event start");
                    user.msg(" &e/event spectate");
                    user.msg("&7&m--------------------");
                }
            }
        } else {
            user.msg("&7&m--------------------");
            user.msg("&6&lEvent Help:");
            user.msg(" &e/event join");
            user.msg(" &e/event leave");
            user.msg(" &e/event start");
            user.msg(" &e/event spectate");
            user.msg("&7&m--------------------");
        }
    }
}
