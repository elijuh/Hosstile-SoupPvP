package me.elijuh.soup.commands.impl.other;

import com.google.common.collect.ImmutableList;
import me.elijuh.soup.Core;
import me.elijuh.soup.commands.SpigotCommand;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class LeaderboardCommand extends SpigotCommand {
    public LeaderboardCommand() {
        super("leaderboard", ImmutableList.of("lb"), null);
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return ImmutableList.of();
    }

    @Override
    public void onExecute(Player p, String[] args) {
        p.openInventory(Core.i().getLeaderboardGUI().getInv());
        p.playSound(p.getLocation(), Sound.CLICK, 0.5f, 1f);
    }
}
