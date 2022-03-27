package me.elijuh.soup.commands.impl.admin;

import com.google.common.collect.ImmutableList;
import me.elijuh.soup.Core;
import me.elijuh.soup.commands.SpigotCommand;
import me.elijuh.soup.util.ChatUtil;
import org.bukkit.entity.Player;

import java.util.List;

public class SetSpawnCommand extends SpigotCommand {

    public SetSpawnCommand() {
        super("setspawn", ImmutableList.of(), "soup.admin");
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return ImmutableList.of();
    }

    @Override
    public void onExecute(Player p, String[] args) {
        Core.i().setSpawn(p.getLocation());
        p.sendMessage(ChatUtil.color("&eSpawn has been updated to your location."));
    }
}
