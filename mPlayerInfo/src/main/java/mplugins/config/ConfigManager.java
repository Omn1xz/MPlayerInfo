package mplugins.config;

import mplugins.MPlayerInfo;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final MPlayerInfo plugin;

    private String menuTitle = "&8Информация о &c{target}";
    private int menuSize = 54;

    private boolean fillerEnabled = true;
    private Material fillerMaterial = Material.PINK_STAINED_GLASS_PANE;
    private String fillerName = " ";
    private String fillerMode = "BORDER";

    private final Map<String, MenuItem> items = new LinkedHashMap<>();

    private String invseeTitle = "&8Инвентарь: &c{target}";
    private String enderTitle = "&8Эндер-сундук: &c{target}";

    public ConfigManager(MPlayerInfo plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration c = plugin.getConfig();
        items.clear();

        menuTitle = c.getString("menu.title", "&8Информация о &c{target}");
        menuSize = c.getInt("menu.size", 54);
        if (menuSize % 9 != 0 || menuSize < 9 || menuSize > 54) menuSize = 54;

        fillerEnabled = c.getBoolean("menu.filler.enabled", true);
        fillerMode = c.getString("menu.filler.mode", "BORDER");
        fillerName = c.getString("menu.filler.display-name", " ");
        try {
            fillerMaterial = Material.valueOf(c.getString("menu.filler.material", "PINK_STAINED_GLASS_PANE"));
        } catch (Exception e) {
            fillerMaterial = Material.PINK_STAINED_GLASS_PANE;
        }

        invseeTitle = c.getString("invsee.title", "&8Инвентарь: &c{target}");
        enderTitle = c.getString("enderchest.title", "&8Эндер-сундук: &c{target}");

        ConfigurationSection sec = c.getConfigurationSection("menu.items");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                ConfigurationSection is = sec.getConfigurationSection(key);
                if (is == null) continue;
                MenuItem item = new MenuItem();
                item.key = key;
                item.enabled = is.getBoolean("enabled", true);

                List<Integer> slots = new ArrayList<>();
                if (is.isList("slots")) {
                    for (Object o : is.getList("slots", new ArrayList<>())) {
                        try {
                            slots.add(Integer.parseInt(String.valueOf(o)));
                        } catch (NumberFormatException ignored) {}
                    }
                } else if (is.isInt("slot")) {
                    slots.add(is.getInt("slot"));
                } else if (is.isString("slot")) {
                    try {
                        slots.add(Integer.parseInt(is.getString("slot", "-1")));
                    } catch (NumberFormatException ignored) {}
                }
                item.slots = slots;

                String matName = is.getString("material", "STONE");
                try {
                    item.material = Material.valueOf(matName.toUpperCase());
                } catch (Exception e) {
                    plugin.getLogger().warning("Unknown material '" + matName + "' for item '" + key + "', using STONE.");
                    item.material = Material.STONE;
                }
                item.displayName = is.getString("display-name", "&f" + key);
                item.lore = is.getStringList("lore");
                item.glow = is.getBoolean("glow", false);
                item.action = is.getString("action", "NONE").toUpperCase();
                item.clickCommands = is.getStringList("click-commands");
                item.closeOnClick = is.getBoolean("close-on-click", false);
                item.skullOwnerMode = is.getString("skull-owner", "TARGET");
                items.put(key, item);
            }
        }
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public int getMenuSize() {
        return menuSize;
    }

    public boolean isFillerEnabled() {
        return fillerEnabled;
    }

    public Material getFillerMaterial() {
        return fillerMaterial;
    }

    public String getFillerName() {
        return fillerName;
    }

    public String getFillerMode() {
        return fillerMode;
    }

    public Map<String, MenuItem> getItems() {
        return items;
    }

    public String getInvseeTitle() {
        return invseeTitle;
    }

    public String getEnderTitle() {
        return enderTitle;
    }

    public String msg(String path, String def) {
        return plugin.getConfig().getString("messages." + path, def);
    }

    public String prefix() {
        return msg("prefix", "&8[&dmPlayerInfo&8] ");
    }

    public static class MenuItem {
        public String key = "";
        public boolean enabled = true;
        public List<Integer> slots = new ArrayList<>();
        public Material material = Material.STONE;
        public String displayName = "";
        public List<String> lore = new ArrayList<>();
        public boolean glow = false;
        public String action = "NONE";
        public List<String> clickCommands = new ArrayList<>();
        public boolean closeOnClick = false;
        public String skullOwnerMode = "TARGET";
    }
}
