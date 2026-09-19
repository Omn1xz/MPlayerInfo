package mplugins.menu;

import mplugins.MPlayerInfo;
import mplugins.config.ConfigManager;
import mplugins.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InfoMenu {

    private final MPlayerInfo plugin;

    public InfoMenu(MPlayerInfo plugin) {
        this.plugin = plugin;
    }

    public static class Holder implements InventoryHolder {
        private final UUID targetUuid;
        private Inventory inventory;

        public Holder(UUID targetUuid) {
            this.targetUuid = targetUuid;
        }

        public UUID getTargetUuid() {
            return targetUuid;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public void open(Player viewer, OfflinePlayer target) {
        ConfigManager cfg = plugin.getConfigManager();
        String title = Text.color(applyPlaceholders(cfg.getMenuTitle(), target, viewer));
        int size = cfg.getMenuSize();

        Holder holder = new Holder(target.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inv);

        if (cfg.isFillerEnabled()) {
            ItemStack filler = buildFiller(cfg);
            String mode = cfg.getFillerMode().toUpperCase();
            for (int i = 0; i < size; i++) {
                boolean border = isBorderSlot(i, size);
                if (mode.equals("EMPTY") || (mode.equals("BORDER") && border)) {
                    inv.setItem(i, filler);
                }
            }
        }

        for (ConfigManager.MenuItem mi : cfg.getItems().values()) {
            if (!mi.enabled) continue;
            ItemStack stack = buildItem(mi, target, viewer);
            for (int slot : mi.slots) {
                if (slot >= 0 && slot < size) inv.setItem(slot, stack);
            }
        }

        viewer.openInventory(inv);
        try {
            viewer.playSound(viewer.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.2f);
        } catch (Exception ignored) {
        }
    }

    private boolean isBorderSlot(int slot, int size) {
        int row = slot / 9;
        int col = slot % 9;
        return row == 0 || row == size / 9 - 1 || col == 0 || col == 8;
    }

    private ItemStack buildFiller(ConfigManager cfg) {
        ItemStack item = new ItemStack(cfg.getFillerMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(cfg.getFillerName()));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildItem(ConfigManager.MenuItem mi, OfflinePlayer target, Player viewer) {
        ItemStack item = new ItemStack(mi.material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(Text.color(applyPlaceholders(mi.displayName, target, viewer)));

        List<String> lore = new ArrayList<>();
        for (String line : mi.lore) {
            lore.add(Text.color(applyPlaceholders(line, target, viewer)));
        }
        meta.setLore(lore);

        if (mi.material == Material.PLAYER_HEAD && meta instanceof SkullMeta skull) {
            if (!mi.skullOwnerMode.equalsIgnoreCase("NONE")) {
                try {
                    skull.setOwningPlayer(target);
                } catch (Exception ignored) {
                }
            }
        }

        if (mi.material == Material.POTION && meta instanceof PotionMeta potion) {
            try {
                potion.setBasePotionType(PotionType.WATER);
                potion.setColor(Color.GRAY);
            } catch (Exception ignored) {
            }
        }

        if (mi.glow) {
            try {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } catch (Exception ignored) {
            }
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        item.setItemMeta(meta);
        return item;
    }

    public void handleClick(Player viewer, int slot, Holder holder) {
        ConfigManager cfg = plugin.getConfigManager();
        ConfigManager.MenuItem found = null;
        for (ConfigManager.MenuItem mi : cfg.getItems().values()) {
            if (!mi.enabled) continue;
            if (mi.slots.contains(slot)) {
                found = mi;
                break;
            }
        }
        if (found == null) return;

        OfflinePlayer target = Bukkit.getOfflinePlayer(holder.getTargetUuid());
        Player onlineTarget = Bukkit.getPlayer(holder.getTargetUuid());

        switch (found.action.toUpperCase()) {
            case "INVSEE", "OPEN_INV", "INVENTORY" -> {
                if (onlineTarget == null) {
                    sendPrefixed(viewer, "offline-action");
                    return;
                }
                plugin.getInvseeMenu().open(viewer, onlineTarget);
                return;
            }
            case "ENDERSEE", "ENDER", "OPEN_ENDER" -> {
                if (onlineTarget == null) {
                    sendPrefixed(viewer, "offline-action");
                    return;
                }
                viewer.openInventory(onlineTarget.getEnderChest());
                return;
            }
            case "VANISH", "VANISH_TP" -> {
                if (onlineTarget == null) {
                    sendPrefixed(viewer, "offline-action");
                    return;
                }
                if (viewer.getUniqueId().equals(onlineTarget.getUniqueId())) {
                    sendPrefixed(viewer, "cannot-self");
                    return;
                }
                if (!plugin.getVanishManager().isVanished(viewer.getUniqueId())) {
                    plugin.getVanishManager().setVanished(viewer, true);
                    sendPrefixed(viewer, "vanished");
                }
                viewer.teleport(onlineTarget.getLocation());
                sendReplaced(viewer, "vanish-tp", "{target}", onlineTarget.getName());
                return;
            }
            case "FREEZE", "FREEZE_TOGGLE" -> {
                if (onlineTarget == null) {
                    sendPrefixed(viewer, "offline-action");
                    return;
                }
                boolean frozen = plugin.getFreezeManager().toggle(onlineTarget.getUniqueId());
                if (frozen) {
                    sendReplaced(viewer, "target-frozen", "{target}", onlineTarget.getName());
                    onlineTarget.sendMessage(Text.color(plugin.getConfigManager().prefix()
                            + plugin.getConfigManager().msg("you-frozen", "&cВы были заморожены администрацией.")));
                } else {
                    sendReplaced(viewer, "target-unfrozen", "{target}", onlineTarget.getName());
                    onlineTarget.sendMessage(Text.color(plugin.getConfigManager().prefix()
                            + plugin.getConfigManager().msg("you-unfrozen", "&aВы были разморожены.")));
                }
                open(viewer, target);
                return;
            }
            case "CLOSE" -> {
                viewer.closeInventory();
                return;
            }
            case "INFO", "STATE", "NONE", "DISPLAY", "COMMAND", "CONSOLE", "PLAYER_COMMAND" -> {
            }
            default -> {
            }
        }

        if (found.clickCommands != null) {
            for (String raw : found.clickCommands) {
                String cmd = applyPlaceholders(raw, target, viewer).replace("{viewer}", viewer.getName());
                if (cmd.startsWith("[console]")) {
                    String c = cmd.substring("[console]".length()).trim();
                    if (c.startsWith("/")) c = c.substring(1);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c);
                } else if (cmd.startsWith("[player]")) {
                    String c = cmd.substring("[player]".length()).trim();
                    if (c.startsWith("/")) c = c.substring(1);
                    viewer.performCommand(c);
                } else {
                    String c = cmd.trim();
                    if (c.startsWith("/")) c = c.substring(1);
                    viewer.performCommand(c);
                }
            }
        }

        if (found.closeOnClick) viewer.closeInventory();
    }

    public String applyPlaceholders(String text, OfflinePlayer target, Player viewer) {
        if (text == null) return "";
        Player online = target.isOnline() ? Bukkit.getPlayer(target.getUniqueId()) : null;
        if (online == null && target instanceof Player p) online = p;

        Map<String, String> ph = new HashMap<>();
        String name = target.getName() != null ? target.getName() : (online != null ? online.getName() : "???");
        ph.put("{target}", name);
        ph.put("{viewer}", viewer != null ? viewer.getName() : "");
        ph.put("{uuid}", target.getUniqueId().toString());
        boolean isOnline = online != null;
        ph.put("{online}", isOnline
                ? plugin.getConfigManager().msg("word-online", "&aОнлайн")
                : plugin.getConfigManager().msg("word-offline", "&cОффлайн"));
        ph.put("{world}", isOnline ? online.getWorld().getName() : "-");
        if (isOnline) {
            Location loc = online.getLocation();
            ph.put("{x}", Text.formatLocation(loc.getX()));
            ph.put("{y}", Text.formatLocation(loc.getY()));
            ph.put("{z}", Text.formatLocation(loc.getZ()));
            ph.put("{ping}", String.valueOf(getPing(online)));
            ph.put("{health}", String.format("%.1f/%.0f", online.getHealth(), online.getMaxHealth()));
            ph.put("{food}", online.getFoodLevel() + "/20");
            ph.put("{gamemode}", online.getGameMode().name());
            ph.put("{level}", String.valueOf(online.getLevel()));
            ph.put("{effects}", formatEffects(online.getActivePotionEffects()));
            ph.put("{frozen}", plugin.getFreezeManager().isFrozen(online.getUniqueId())
                    ? plugin.getConfigManager().msg("word-yes", "&cДа")
                    : plugin.getConfigManager().msg("word-no", "&aНет"));
        } else {
            ph.put("{x}", "-");
            ph.put("{y}", "-");
            ph.put("{z}", "-");
            ph.put("{ping}", "-");
            ph.put("{health}", "-");
            ph.put("{food}", "-");
            ph.put("{gamemode}", "-");
            ph.put("{level}", "-");
            ph.put("{effects}", "-");
            ph.put("{frozen}", "-");
        }
        long session = isOnline ? plugin.getSessionManager().getSessionMillis(target.getUniqueId()) : 0L;
        ph.put("{session}", isOnline ? Text.formatDuration(session)
                : plugin.getConfigManager().msg("word-none", "-"));
        ph.put("{vanished}", viewer != null && plugin.getVanishManager().isVanished(viewer.getUniqueId())
                ? plugin.getConfigManager().msg("word-yes", "&cДа")
                : plugin.getConfigManager().msg("word-no", "&aНет"));

        String out = text;
        for (Map.Entry<String, String> e : ph.entrySet()) {
            out = out.replace(e.getKey(), e.getValue());
        }
        return out;
    }

    private String formatEffects(Collection<PotionEffect> effects) {
        if (effects == null || effects.isEmpty()) {
            return plugin.getConfigManager().msg("word-no-effects", "&7Нет");
        }
        List<String> parts = new ArrayList<>();
        for (PotionEffect e : effects) {
            parts.add(e.getType().getName().toLowerCase().replace("_", " ") + " " + (e.getAmplifier() + 1));
        }
        return String.join(", ", parts);
    }

    public static int getPing(Player player) {
        try {
            Method m = player.getClass().getMethod("getPing");
            Object r = m.invoke(player);
            if (r instanceof Number n) return n.intValue();
        } catch (Exception ignored) {
        }
        try {
            Method m = Player.class.getMethod("getPing");
            Object r = m.invoke(player);
            if (r instanceof Number n) return n.intValue();
        } catch (Exception ignored) {
        }
        return -1;
    }

    private void sendPrefixed(Player p, String key) {
        String m = plugin.getConfigManager().msg(key, "");
        if (!m.isEmpty()) p.sendMessage(Text.color(plugin.getConfigManager().prefix() + m));
    }

    private void sendReplaced(Player p, String key, String placeholder, String value) {
        String m = plugin.getConfigManager().msg(key, "");
        if (!m.isEmpty()) p.sendMessage(Text.color(plugin.getConfigManager().prefix() + m.replace(placeholder, value)));
    }
}
