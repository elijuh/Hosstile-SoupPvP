package me.elijuh.soup.events;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import me.elijuh.soup.Core;
import me.elijuh.soup.data.User;
import me.elijuh.soup.events.arena.DuelEventArena;
import me.elijuh.soup.events.arena.EventArena;
import me.elijuh.soup.events.arena.FFAEventArena;
import me.elijuh.soup.scoreboard.EventScoreboard;
import me.elijuh.soup.scoreboard.PreEventScoreboard;
import me.elijuh.soup.util.ChatUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class Event {
    private static Event current;
    private final List<User> users = new ArrayList<>();
    private final List<User> alive = new ArrayList<>();
    private final List<User> roundUsers = new ArrayList<>();
    private final User host;
    private final EventType type;
    private final EventArena arena;
    private EventState state = EventState.STARTING;
    private int duration, starting, round;

    public Event(User host, EventType type) {
        current = this;
        this.host = host;
        this.type = type;
        arena = type.getArenas().get(ThreadLocalRandom.current().nextInt(type.getArenas().size()));
        starting = 30;

        users.add(host);
        host.setSpawn(false);
        host.setScoreboard(new PreEventScoreboard(host.p()));

        BaseComponent component = new TextComponent(ChatUtil.color(host.coloredName() + " &eis hosting a &d" +
                type.getDisplay() + " &eevent &e(&aClick to join&e)!"));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatUtil.color("&aClick to join the event."))));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join"));
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.spigot().sendMessage(component);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (state == EventState.STARTING) {
                    if (starting == 0) {
                        start();
                    } else if (starting % 10 == 0 || starting < 6) {
                        BaseComponent component = new TextComponent(ChatUtil.color("&eA &d" + type.getDisplay() +
                                " &eevent is starting in &d" + starting + "s &e(&aClick to join&e)!"));
                        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatUtil.color("&aClick to join the event."))));
                        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join"));
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.spigot().sendMessage(component);
                        }
                    }
                    starting--;
                } else if (state == EventState.ENDED) {
                    cancel();
                } else {
                    if (type == EventType.FFA) {
                        int timer = 10 - duration;
                        if (timer > 0) {
                            for (User user : users) {
                                user.msg("&eYou can attack players in &d" + timer + "s");
                            }
                        } else if (timer == 0) {
                            for (User user : users) {
                                user.msg("&aYou can now attack other players");
                                user.p().playSound(user.loc(), Sound.NOTE_PLING, 2f, 1f);
                            }
                        }
                    }
                    duration++;
                }
            }
        }.runTaskTimer(Core.i(), 0L, 20L);
    }

    public void start() throws IllegalStateException {
        if (users.size() > 1) {
            if (state == EventState.STARTING) {
                Bukkit.broadcastMessage(ChatUtil.color("&aThe event has started!"));
                state = EventState.ACTIVE;
                for (User user : users) {
                    alive.add(user);
                    if (type == EventType.FFA) {
                        roundUsers.add(user);
                        Core.i().getKit("default").apply(user);
                    } else {
                        user.setContents(new ItemStack[40]);
                    }
                    user.setScoreboard(new EventScoreboard(user.p()));
                    user.p().teleport(arena.getSpawn());
                }
                if (arena instanceof DuelEventArena) {
                    nextRound();
                }
            } else {
                throw new IllegalStateException("Event already running.");
            }
        } else {
            Bukkit.broadcastMessage(ChatUtil.color("&cThere were not enough players to start the event."));
            end();
            if (host != null) {
                host.pay(type.getPrice());
                host.msg("&aYou have been refunded for the event.");
            }
        }
    }

    public void end() {
        duration = 0;
        state = EventState.ENDED;
        for (User user : ImmutableList.copyOf(users)) {
            remove(user);
        }
        current = null;
    }

    public void add(User user) {
        if (state == EventState.STARTING) {
            if (users.size() >= type.getMaxPlayers()) {
                user.msg("&cThe event is full.");
                return;
            } else {
                user.setScoreboard(new PreEventScoreboard(user.p()));
            }
        } else {
            user.setScoreboard(new EventScoreboard(user.p()));
        }
        users.add(user);
        user.setSpawn(false);
        if (state == EventState.STARTING) {
            BaseComponent component = new TextComponent(ChatUtil.color(String.format("&e(&d%s&7/&d%s&e) %s &ehas joined the event (&aClick to join&e)!",
                    users.size(), type.getMaxPlayers(), user.coloredName())));
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatUtil.color("&aClick to join the event."))));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join"));
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.spigot().sendMessage(component);
            }
        } else {
            if (arena instanceof FFAEventArena) {
                user.p().teleport(((FFAEventArena) arena).getSpectateArea());
            } else {
                user.p().teleport(arena.getSpawn());
            }
        }
    }

    public void remove(User user) {
        if (users.remove(user)) {
            if (state == EventState.STARTING) {
                BaseComponent component = new TextComponent(ChatUtil.color(String.format("&e(&d%s&7/&d%s&e) %s &ehas left the event &e(&aClick to join&e)!",
                        users.size(), type.getMaxPlayers(), user.coloredName())));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatUtil.color("&aClick to join the event."))));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join"));
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.spigot().sendMessage(component);
                }
            }
            if (state == EventState.ACTIVE) {
                eliminate(user);
            }

            user.p().removeMetadata("immobile", Core.i());
            user.spawn(true);
        }
    }

    public void eliminate(User user) {
        if (alive.remove(user)) {
            for (User all : Event.getCurrent().getUsers()) {
                all.msg("&d" + user.p().getName() + " &ehas been eliminated.");
            }
            if (arena instanceof FFAEventArena) {
                roundUsers.remove(user);
                user.p().teleport(((FFAEventArena) arena).getSpectateArea());
            } else {
                user.p().teleport(arena.getSpawn());
            }
            if (alive.size() < 2) {
                User last = alive.stream().findFirst().orElse(null);
                if (last != null) {
                    Bukkit.broadcastMessage(ChatUtil.color(last.coloredName() + " &ehas won the &d" + type.getDisplay() + " &eevent!"));
                    last.pay(type.getPrice());
                } else {
                    Bukkit.broadcastMessage(ChatUtil.color("&aThe event has ended."));
                }
                end();
            } else if (arena instanceof DuelEventArena && roundUsers.contains(user)) {
                nextRound();
            }
        }
    }

    public void nextRound() {
        for (User user : ImmutableList.copyOf(roundUsers)) {
            user.p().teleport(arena.getSpawn());
            user.setContents(new ItemStack[40]);
            roundUsers.remove(user);
        }

        round++;
        if (alive.size() < 2) {
            return;
        }
        User one = alive.get(ThreadLocalRandom.current().nextInt(alive.size()));
        User t = alive.get(ThreadLocalRandom.current().nextInt(alive.size()));
        while (one == t) {
            t = alive.get(ThreadLocalRandom.current().nextInt(alive.size()));
        }
        User two = t;

        roundUsers.add(one);
        roundUsers.add(two);

        if (type == EventType.DUELS) {
            Core.i().getKit("default").apply(one);
            Core.i().getKit("default").apply(two);
        }

        DuelEventArena duelEventArena = (DuelEventArena) arena;

        one.p().setMetadata("immobile", new FixedMetadataValue(Core.i(), true));
        two.p().setMetadata("immobile", new FixedMetadataValue(Core.i(), true));

        one.p().teleport(duelEventArena.getPlayerOneSpot());
        two.p().teleport(duelEventArena.getPlayerTwoSpot());

        for (User user : users) {
            user.msg(String.format("&eNext Round: %s &evs %s", one.coloredName(), two.coloredName()));
        }

        new BukkitRunnable() {
            private int seconds = 5;

            @Override
            public void run() {
                if (roundUsers.size() > 1) {
                    if (seconds > 0) {
                        for (User user : users) {
                            user.msg("&dRound #" + round + " &estarting in &d" + seconds + "s");
                            user.p().playSound(user.loc(), Sound.CLICK, 1f, 1f);
                        }
                    } else {
                        for (User user : users) {
                            user.msg("&aThe round has started!");
                            user.p().playSound(user.loc(), Sound.NOTE_PLING, 1f, 2f);
                        }
                        one.p().removeMetadata("immobile", Core.i());
                        two.p().removeMetadata("immobile", Core.i());
                        cancel();
                    }
                    seconds--;
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(Core.i(), 20L, 20L);
    }

    public static Event getCurrent() {
        return current;
    }
}
