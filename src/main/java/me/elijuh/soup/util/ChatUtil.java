package me.elijuh.soup.util;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

@UtilityClass
public class ChatUtil {

    public String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public String upperFirst(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public String formatSeconds(int seconds) {
        int minutes = 0;
        while (seconds > 60) {
            minutes++;
            seconds -= 60;
        }
        return minutes + ":" + (seconds > 9 ? seconds : "0" + seconds);
    }
}
