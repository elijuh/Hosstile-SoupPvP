package me.elijuh.soup.scoreboard;

import me.elijuh.soup.data.Pair;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

public interface IScoreboard {
    Player getPlayer();

    void refresh();

    List<Pair<String, String>> getLines();

    String getTitle();

    Scoreboard getScoreboard();

    Objective getObjective();
}
