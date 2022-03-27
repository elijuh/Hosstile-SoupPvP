package me.elijuh.soup.scoreboard;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.elijuh.soup.data.Pair;
import me.elijuh.soup.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

@Getter
public class DefaultScoreboard implements IScoreboard {
    private final ImmutableList<Pair<String, String>> lines = ImmutableList.of(
            new Pair<>("&7&m---------", "-----------"),
            new Pair<>("&8» &eKills:", " &f%soup_kills%"),
            new Pair<>("&8» &eDeaths:", " &f%soup_deaths%"),
            new Pair<>("&8» &eStreak:", " &f%soup_streak%"),
            new Pair<>("", ""),
            new Pair<>("&8» &eCombat:", " &f%soup_combat%"),
            new Pair<>("&8» &eBalance:", " &a$%soup_coins%"),
            new Pair<>("&7&m----------", "----------&r")
    );
    private final String title = ChatUtil.color("&6Hosstile &7⏐ &fSoup");
    private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    private final Objective objective = scoreboard.registerNewObjective("dummy", "sb");
    private final Player player;

    public DefaultScoreboard(Player player) {
        this.player = player;

        objective.setDisplayName(title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (int i = 0; i < lines.size(); i++) {
            Team team = scoreboard.registerNewTeam(Integer.toString(i));
            String text = ChatUtil.color(lines.get(i).getX());
            team.addEntry(text);
            objective.getScore(text).setScore(lines.size() - i);
        }
        refresh();
        player.setScoreboard(scoreboard);
    }

    @Override
    public void refresh() {
        for (int i = 0; i < lines.size(); i++) {
            Team team = scoreboard.getTeam(Integer.toString(i));
            team.setSuffix(PlaceholderAPI.setPlaceholders(player, lines.get(i).getY()));
        }
    }
}
