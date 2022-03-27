package me.elijuh.soup.tasks;

import me.elijuh.soup.Core;
import me.elijuh.soup.data.User;
import me.elijuh.soup.util.PlayerUtil;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnTask extends BukkitRunnable {
    private final User user;
    private int ticks = 50;

    public SpawnTask(User user) {
        this.user = user;
        user.msg("&eTeleporting to spawn in &d5 seconds&e.");
        runTaskTimer(Core.i(), 0L, 2L);
    }

    @Override
    public void run() {
        if (ticks > 0) {
            PlayerUtil.sendActionBar(user.p(), "&eTeleporting: &d" + ticks / 10.0 + "s");
            ticks--;
        } else {
            user.spawn();
            PlayerUtil.sendActionBar(user.p(), "&aTeleported!");
            user.msg("&aYou have been teleported to spawn.");
            cancel();
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        user.setSpawnTask(null);
    }
}
