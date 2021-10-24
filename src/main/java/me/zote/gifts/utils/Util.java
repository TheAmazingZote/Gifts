package me.zote.gifts.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

@UtilityClass
public class Util {

    public String color(String color) {
        return ChatColor.translateAlternateColorCodes('&', color);
    }

    public String title(String title) {
        title = uncolor(title);
        title = title.replaceAll("\\[", "").replaceAll("]", "");
        return color("&8[&e" + title + "&8]");
    }

    public String uncolor(String message) {
        return message.replaceAll("(?i)(&|" + ChatColor.COLOR_CHAR + ")[0-9A-FK-OR]", "");
    }

}
