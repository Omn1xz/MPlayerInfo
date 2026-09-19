package mplugins.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public final class Text {

    private Text() {}

    public static String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static List<String> color(List<String> list) {
        List<String> out = new ArrayList<>(list.size());
        for (String s : list) out.add(color(s));
        return out;
    }

    public static String formatDuration(long millis) {
        if (millis < 0) millis = 0;
        long sec = millis / 1000;
        long d = sec / 86400;
        long h = (sec % 86400) / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append("д ");
        if (h > 0 || d > 0) sb.append(h).append("ч ");
        if (m > 0 || h > 0 || d > 0) sb.append(m).append("м ");
        sb.append(s).append("с");
        return sb.toString().trim();
    }

    public static String formatLocation(double v) {
        return String.format("%.1f", v);
    }
}
