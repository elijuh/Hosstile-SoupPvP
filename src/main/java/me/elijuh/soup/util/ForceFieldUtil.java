package me.elijuh.soup.util;

import lombok.experimental.UtilityClass;
import me.elijuh.soup.Core;
import me.elijuh.soup.data.User;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class ForceFieldUtil {
    public Set<Location> getChangedBlocks(Player p) {
        User user = Core.i().getUser(p.getName());
        Set<Location> locations = new HashSet<>();

        if (user.getCombat() < 1) return locations;

        int r = 5;
        Location l = p.getLocation();
        Location loc1 = l.clone().add(r, r, r);
        Location loc2 = l.clone().subtract(r, r, r);
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location location = new Location(l.getWorld(), x, y, z);

                    if (!Core.i().getSpawnRegion().contains(location)) continue;

                    if (!isPvpSurrounding(location)) continue;

                    if (!location.getBlock().getType().isSolid()) {
                        locations.add(location);
                    }
                }
            }
        }

        return locations;
    }

    public boolean isPvpSurrounding(Location loc) {
        for (BlockFace direction : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            if (!Core.i().getSpawnRegion().contains(loc.getBlock().getRelative(direction).getLocation())) {
                return true;
            }
        }

        return false;
    }
}
