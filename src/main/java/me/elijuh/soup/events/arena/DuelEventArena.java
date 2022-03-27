package me.elijuh.soup.events.arena;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.elijuh.soup.data.Cuboid;
import org.bukkit.Location;

@Getter
@AllArgsConstructor
public class DuelEventArena implements EventArena {
    private final String id;
    private final Cuboid region;
    private final Location spawn;
    private final Location playerOneSpot;
    private final Location playerTwoSpot;
}
