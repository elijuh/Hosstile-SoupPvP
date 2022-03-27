package me.elijuh.soup;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.elijuh.soup.data.User;
import me.elijuh.soup.events.Event;
import me.elijuh.soup.util.ChatUtil;
import me.elijuh.soup.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SoupExpansion extends PlaceholderExpansion {
    private static SoupExpansion instance;

    public SoupExpansion() {
        instance = this;
    }

    @Override
    public String getIdentifier() {
        return "soup";
    }

    @Override
    public String getAuthor() {
        return "elijuh";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        User user = Core.i().getUser(player.getName());
        if (user != null) {
            switch (params.toLowerCase()) {
                case "kills": {
                    return Integer.toString(user.get("kills"));
                }
                case "deaths": {
                    return Integer.toString(user.get("deaths"));
                }
                case "streak": {
                    return Integer.toString(user.get("streak"));
                }
                case "coins": {
                    return Integer.toString(user.get("coins"));
                }
                case "health": {
                    return Long.toString(PlayerUtil.getHealth(player));
                }
                case "visibleplayers": {
                    int players = 0;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (player.canSee(p)) {
                            players++;
                        }
                    }
                    return Integer.toString(players);
                }
                case "combat": {
                    if (user.getCombat() > 0) {
                        return ChatColor.RED.toString() + user.getCombat() / 10.0 + "s";
                    } else {
                        return ChatColor.GREEN + "None";
                    }
                }
                case "event_display": {
                    return Event.getCurrent() != null ? Event.getCurrent().getType().getDisplay() : "";
                }
                case "event_users": {
                    return Event.getCurrent() != null ? Integer.toString(Event.getCurrent().getUsers().size()) : "";
                }
                case "event_alive": {
                    return Event.getCurrent() != null ? Integer.toString(Event.getCurrent().getAlive().size()) : "";
                }
                case "event_maxplayers": {
                    return Event.getCurrent() != null ? Integer.toString(Event.getCurrent().getType().getMaxPlayers()) : "";
                }
                case "event_spectators": {
                    return Event.getCurrent() != null ? Integer.toString(Event.getCurrent().getUsers().size() - Event.getCurrent().getAlive().size()) : "";
                }
                case "event_round": {
                    return Event.getCurrent() != null ? Integer.toString(Event.getCurrent().getRound()) : "";
                }
                case "event_duration": {
                    return Event.getCurrent() != null ? ChatUtil.formatSeconds(Event.getCurrent().getDuration()) : "";
                }
                case "event_countdown": {
                    return Event.getCurrent() != null ? ChatUtil.formatSeconds(Event.getCurrent().getStarting()) : "";
                }
            }
        }
        return "";
    }

    public static SoupExpansion i() {
        return instance;
    }
}
