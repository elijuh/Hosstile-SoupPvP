package me.elijuh.soup.events;

import lombok.Getter;
import me.elijuh.soup.Core;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

@Getter
public class EventConfiguration {
    private static EventConfiguration instance;
    private final File file = new File(Core.i().getDataFolder(), "events.yml");
    private final FileConfiguration config = YamlConfiguration.loadConfiguration(file);

    public EventConfiguration() {
        instance = this;
    }

    public void save() {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static EventConfiguration getInstance() {
        return instance;
    }
}
