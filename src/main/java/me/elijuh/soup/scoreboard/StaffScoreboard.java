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
public class StaffScoreboard implements IScoreboard {
    private final ImmutableList<Pair<String, String>> lines = new ImmutableList.Builder<Pair<String, String>>()
            .add(new Pair<>("&7&m---------", "-----------"))
            .add(new Pair<>("&8» &eKills:", " &f%soup_kills%"))
            .add(new Pair<>("&8» &eDeaths:", " &f%soup_deaths%"))
            .add(new Pair<>("&8» &eStreak:", " &f%soup_streak%"))
            .add(new Pair<>("", ""))
            .add(new Pair<>("&8» &eCombat:", " &f%soup_combat%"))
            .add(new Pair<>("&8» &eBalance:", " &a$%soup_coins%"))
            .add(new Pair<>(" ", ""))
            .add(new Pair<>("&6Staff:", ""))
            .add(new Pair<>("&8» &eMod Mode:", " &f%staff_modmode%"))
            .add(new Pair<>("&8» &eVanish:", " &f%staff_vanish%"))
            .add(new Pair<>("&7&m-----------", "---------&r")).build();

    private final String title = ChatUtil.color("&6Hosstile &7⏐ &fSoup");
    private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    private final Objective objective = scoreboard.registerNewObjective("dummy", "sb");
    private final Player player;

    public StaffScoreboard(Player player) {
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
