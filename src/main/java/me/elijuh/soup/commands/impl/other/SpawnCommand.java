package me.elijuh.soup.commands.impl.other;

import com.google.common.collect.ImmutableList;
import me.elijuh.soup.Core;
import me.elijuh.soup.commands.SpigotCommand;
import me.elijuh.soup.data.User;
import me.elijuh.soup.events.Event;
import me.elijuh.soup.tasks.SpawnTask;
import org.bukkit.entity.Player;

import java.util.List;

public class SpawnCommand extends SpigotCommand {

    public SpawnCommand() {
        super("spawn");
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return ImmutableList.of();
    }

    @Override
    public void onExecute(Player p, String[] args) {
        User user = Core.i().getUser(p.getName());
        if (user != null) {
            if (user.isSpawn() || Core.i().getSpawnRegion().contains(user.p().getLocation())) {
                user.spawn();
            } else if (Event.getCurrent() != null && Event.getCurrent().getUsers().contains(user)) {
                Event.getCurrent().remove(user);
            } else if (user.getCombat() > 0) {
                user.msg("&cYou cannot /spawn in combat.");
            } else {
                user.setSpawnTask(new SpawnTask(user));
            }
        }
    }
}
