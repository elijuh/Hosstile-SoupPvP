package me.elijuh.soup.commands.impl.other;

import com.google.common.collect.ImmutableList;
import me.elijuh.soup.Core;
import me.elijuh.soup.commands.SpigotCommand;
import me.elijuh.soup.data.User;
import me.elijuh.soup.util.ChatUtil;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.util.List;

public class StatsCommand extends SpigotCommand {

    public StatsCommand() {
        super("stats", ImmutableList.of("coins", "kills", "deaths", "streak"), null);
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return null;
    }

    @Override
    public void onExecute(Player p, String[] args) {
        User target = Core.i().getUser(args.length > 0 ? args[0] : p.getName());
        String message = ChatUtil.color("&cThat player does not exist.");
        if (target != null) {
            message = ChatUtil.color("&7&m-----------------------------"
                    + "\n&eShowing stats for &f" + target.p().getName()
                    + "\n&eKills: &f" + target.get("kills")
                    + "\n&eDeaths: &f" + target.get("deaths")
                    + "\n&eStreak: &f" + target.get("streak")
                    + "\n&eCoins: &f" + target.get("coins")
                    + "\n&eBounty: &f" + target.getBounty()
                    + "\n&eLast Kit: &f" + ChatUtil.upperFirst(target.get("last_kit"))
                    + "\n&eSelected Healing: &f" + ChatUtil.upperFirst(target.getHealType().name())
                    + "\n&7&m-----------------------------");
        } else if (args.length > 0) {
            Document data = Core.i().getMongoManager().getData(args[0]);
            if (data != null) {
                message = ChatUtil.color("&7&m-----------------------------"
                        + "\n&eShowing stats for &f" + data.getString("name")
                        + "\n&eKills: &f" + data.getInteger("kills")
                        + "\n&eDeaths: &f" + data.getInteger("deaths")
                        + "\n&eStreak: &f" + data.getInteger("streak")
                        + "\n&eCoins: &f" + data.getInteger("coins")
                        + "\n&eBounty: &f" +data.getInteger("bounty")
                        + "\n&eLast Kit: &f" + ChatUtil.upperFirst(data.getString("last_kit"))
                        + "\n&eSelected Healing: &f" + ChatUtil.upperFirst(data.getString("heal_type"))
                        + "\n&7&m-----------------------------");
            }
        }
        p.sendMessage(message);
    }
}
