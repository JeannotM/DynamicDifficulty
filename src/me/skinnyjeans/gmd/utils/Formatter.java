package me.skinnyjeans.gmd.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Formatter {

    /**
     * Function to format strings
     * @param original The String to be formatted
     * @return Formatted String with applied ColorCodes
     */
    public static String format(String original) { return ChatColor.translateAlternateColorCodes('&', original); }

    /**
     * Function to format strings
     * @param original The String to be formatted
     * @param replaceWith To replace in a string
     * @return Formatted String with applied ColorCodes and replaced item
     */
    public static String format(String original, String replaceWith) {
        return ChatColor.translateAlternateColorCodes('&', original).replace("%replace%", replaceWith);
    }

    /**
     * Function to format strings
     * @param original The String to be formatted
     * @param replaceWith To replace in a string
     * @return Formatted String with applied ColorCodes and replaced item
     */
    public static String format(String original, HashMap<String, String> replaceWith) {
        String formatted = ChatColor.translateAlternateColorCodes('&', original);
        for(String key : replaceWith.keySet()) formatted = formatted.replace(key, replaceWith.get(key));
        return formatted;
    }

    public static List<String> list(Object ...objects) {
        List<String> list = new ArrayList<>();
        for(Object object : objects) list.add(object.toString());
        return list;
    }


}
