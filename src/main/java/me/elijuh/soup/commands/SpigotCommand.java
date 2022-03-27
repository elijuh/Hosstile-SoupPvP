package me.elijuh.soup.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Getter;
import me.elijuh.soup.util.ChatUtil;
import me.elijuh.soup.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class SpigotCommand extends Command {
    private static final List<String> registeredCommands = new ArrayList<>();
    String name, permission;
    List<String> aliases;

    public SpigotCommand(String name) {
        this(name, Lists.newArrayList(), null);
    }

    public SpigotCommand(String name, List<String> aliases, String permission) {
        super(name);
        setAliases(aliases);

        this.name = name;
        this.aliases = aliases;
        this.permission = permission;

        try{
            CommandMap map = (CommandMap) ReflectionUtil.getField(Bukkit.getServer().getClass(), "commandMap").get(Bukkit.getServer());
            ReflectionUtil.unregisterCommands(map, getName());
            ReflectionUtil.unregisterCommands(map, getAliases());
            map.register(getName(), "soup", this);
            registeredCommands.add(name);
            registeredCommands.addAll(aliases);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            onConsole(sender, args);
            return false;
        }

        Player p = (Player) sender;
        if (permission != null && !p.hasPermission(permission)) {
            p.sendMessage(ChatUtil.color("&cNo permission."));
            return false;
        }

        try {
            onExecute(p, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (sender instanceof Player) {
            if (getPermission() != null) {
                if (!sender.hasPermission(getPermission())) {
                    return ImmutableList.of();
                }
            }

            Player p = (Player) sender;

            List<String> tabCompletion = onTabComplete(p, args);
            if (tabCompletion == null) {
                List<String> list = Lists.newArrayList();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (StringUtil.startsWithIgnoreCase(player.getName(), args[0]) && p.canSee(player)) {
                        list.add(player.getName());
                    }
                }
                return list;
            }
            return tabCompletion;

        } else {
            return ImmutableList.of();
        }
    }

    public void onConsole(CommandSender sender, String[] args) {
        sender.sendMessage(ChatUtil.color("&cYou must be a player to execute this command."));
    }

    public static List<String> getRegisteredCommands() {
        return registeredCommands;
    }

    public abstract List<String> onTabComplete(Player p, String[] args);

    public abstract void onExecute(Player p, String[] args);

}
