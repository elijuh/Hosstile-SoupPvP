package me.elijuh.soup.util;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@UtilityClass
public class PlayerUtil {
    private static final Class<?> craftPlayer = ReflectionUtil.getCBClass("entity.CraftPlayer");
    private static final Class<?> entityHuman = ReflectionUtil.getNMSClass("EntityHuman");

    public long getHealth(Player p) {
        try {
            Object ep = craftPlayer.getMethod("getHandle").invoke(craftPlayer.cast(p));
            float absorption = (float) ReflectionUtil.getMethod(entityHuman, "getAbsorptionHearts").invoke(ep);
            return Math.round(p.getHealth() + absorption);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void sendActionBar(Player p, String message) {
        Method icbcMethod;
        Object icbc;
        Class<?> icbcClass = ReflectionUtil.getNMSClass("IChatBaseComponent");
        Class<?> chatSerializerClass = null;
        for(Class<?> clazz : icbcClass.getClasses()) {
            if(clazz.getName().contains("ChatSerializer"))
                chatSerializerClass = clazz;
        }
        if(chatSerializerClass == null) return;

        try {
            icbcMethod = chatSerializerClass.getDeclaredMethod("a", String.class);
            icbc = icbcMethod.invoke(null, "{\"text\": \"" + ChatUtil.color(message) + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        Object packet = null;

        if(Integer.parseInt(ReflectionUtil.getVersion().split("_")[1]) < 11) {
            Class<?> ppocClass = ReflectionUtil.getNMSClass("PacketPlayOutChat");
            Constructor<?> ppocConstructor;
            try {
                ppocConstructor = ppocClass.getConstructor(ReflectionUtil.getNMSClass("IChatBaseComponent"), byte.class);
                packet = ppocConstructor.newInstance(icbc, (byte)2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Class<?> ppocClass = ReflectionUtil.getNMSClass("PacketPlayOutChat");
            Constructor<?> ppocConstructor;
            try {
                Class<?> chatMessageTypeClass = ReflectionUtil.getNMSClass("ChatMessageType");
                ppocConstructor = ppocClass.getConstructor(ReflectionUtil.getNMSClass("IChatBaseComponent"), chatMessageTypeClass);

                Method chatMessageTypeMethod = chatMessageTypeClass.getMethod("valueOf", String.class);
                Object chatMessageTypeValue = chatMessageTypeMethod.invoke(null, "GAME_INFO");

                packet = ppocConstructor.newInstance(icbc, chatMessageTypeValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (packet == null) return;
        Class<?> player = ReflectionUtil.getCBClass("entity.CraftPlayer");

        Object craftPlayer;
        try {
            craftPlayer = player.getMethod("getHandle").invoke(p);
            Field playerConnectionField = craftPlayer.getClass().getField("playerConnection");
            Object playerConnection = playerConnectionField.get(craftPlayer);

            Method sendPacketsMethod = playerConnection.getClass().getMethod("sendPacket", ReflectionUtil.getNMSClass("Packet"));
            sendPacketsMethod.invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
