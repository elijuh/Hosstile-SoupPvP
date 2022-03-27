package me.elijuh.soup.commands.impl.admin;

import com.google.common.collect.ImmutableList;
import com.mongodb.client.model.Filters;
import me.elijuh.soup.Core;
import me.elijuh.soup.commands.SpigotCommand;
import me.elijuh.soup.data.User;
import me.elijuh.soup.util.ChatUtil;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.util.List;

public class SetCoinsCommand extends SpigotCommand {

    public SetCoinsCommand() {
        super("setcoins", ImmutableList.of(), "soup.admin");
    }

    @Override
    public List<String> onTabComplete(Player p, String[] args) {
        return ImmutableList.of();
    }

    @Override
    public void onExecute(Player p, String[] args) {
        if (args.length == 2) {
            int coins;
            try {
                coins = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                p.sendMessage(ChatUtil.color("&cInvalid Integer: " + args[1]));
                return;
            }
            User user = Core.i().getUser(args[0]);
            if (user != null) {
                p.sendMessage(ChatUtil.color(String.format("&eUpdated coins for &d%s&e:\n&a$%s &eto &a$%s", user.name(),
                        user.get("coins"), coins)));
                user.getData().put("coins", coins);
            } else {
                Document data = Core.i().getMongoManager().getData(args[0]);
                if (data != null) {
                    Document update = new Document("coins", coins);
                    Core.i().getMongoManager().getUserdata().updateOne(Filters.eq("uuid", data.getString("uuid")), new Document("$set", update));
                    p.sendMessage(ChatUtil.color(String.format("&eUpdated coins for &d%s&e:\n&a$%s &eto &a$%s", data.getString("display"),
                            data.getInteger("coins"), coins)));
                } else {
                    p.sendMessage(ChatUtil.color("&cThat player does not exist."));
                }
            }
        } else {
            p.sendMessage(ChatUtil.color("&cUsage: /setcoins <player> <amount>"));
        }
    }
}
