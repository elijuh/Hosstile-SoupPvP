package me.elijuh.soup.scoreboard;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.elijuh.soup.data.Pair;
import me.elijuh.soup.events.Event;
import me.elijuh.soup.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

@Getter
public class PreEventScoreboard implements IScoreboard {
    private final ImmutableList<Pair<String, String>> lines = ImmutableList.of(
            new Pair<>("&7&m---------", "-----------"),
            new Pair<>("&8» &eStarting", " in: &7%soup_event_countdown%"),
            new Pair<>("", ""),
            new Pair<>("&8» &ePlayers:", " &d%soup_event_users%&7/&d%soup_event_maxplayers%"),
            new Pair<>("&7&m----------", "----------&r")
    );
    private final String title = ChatUtil.color("&6Event &7⏐ &f" + Event.getCurrent().getType().getDisplay());
    private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    private final Objective objective = scoreboard.registerNewObjective("dummy", "sb");
    private final Player player;

    public PreEventScoreboard(Player player) {
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
