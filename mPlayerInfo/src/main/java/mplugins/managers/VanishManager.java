package mplugins.managers;

import mplugins.MPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VanishManager {

    private final MPlayerInfo plugin;
    private final Set<UUID> vanished = ConcurrentHashMap.newKeySet();

    public VanishManager(MPlayerInfo plugin) {
        this.plugin = plugin;
    }

    public boolean isVanished(UUID uuid) {
        return vanished.contains(uuid);
    }

    public void setVanishedQuiet(UUID uuid, boolean value) {
        if (value) vanished.add(uuid);
        else vanished.remove(uuid);
    }

    public boolean toggle(Player player) {
        boolean to = !isVanished(player.getUniqueId());
        setVanished(player, to);
        return to;
    }

    public void setVanished(Player player, boolean vanish) {
        if (vanish) {
            vanished.add(player.getUniqueId());
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.equals(player)) continue;
                if (other.hasPermission("mplayerinfo.use")) {
                    other.showPlayer(plugin, player);
                } else {
                    other.hidePlayer(plugin, player);
                }
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
        } else {
            vanished.remove(player.getUniqueId());
            for (Player other : Bukkit.getOnlinePlayers()) {
                other.showPlayer(plugin, player);
            }
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    public void hideAllVanishedFrom(Player newcomer) {
        for (UUID uuid : vanished) {
            Player v = Bukkit.getPlayer(uuid);
            if (v != null && !newcomer.hasPermission("mplayerinfo.use")) {
                newcomer.hidePlayer(plugin, v);
            }
        }
    }

    public void reapplyVanish(Player player) {
        if (isVanished(player.getUniqueId())) {
            setVanished(player, true);
        }
    }
}
