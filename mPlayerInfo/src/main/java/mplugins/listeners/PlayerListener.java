package mplugins.listeners;

import mplugins.MPlayerInfo;
import mplugins.menu.InfoMenu;
import mplugins.menu.InvseeMenu;
import mplugins.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final MPlayerInfo plugin;

    public PlayerListener(MPlayerInfo plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        plugin.getSessionManager().markJoin(p.getUniqueId());
        plugin.getVanishManager().hideAllVanishedFrom(p);
        if (plugin.getVanishManager().isVanished(p.getUniqueId())) {
            plugin.getVanishManager().setVanished(p, true);
            e.setJoinMessage(null);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getSessionManager().markQuit(e.getPlayer().getUniqueId());
        if (plugin.getVanishManager().isVanished(e.getPlayer().getUniqueId())) {
            e.setQuitMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player viewer)) return;

        if (e.getInventory().getHolder() instanceof InfoMenu.Holder holder) {
            e.setCancelled(true);
            if (e.getClickedInventory() == null) return;
            if (!e.getClickedInventory().equals(e.getInventory())) return;
            int slot = e.getRawSlot();
            if (slot < 0 || slot >= e.getInventory().getSize()) return;
            plugin.getInfoMenu().handleClick(viewer, slot, holder);
            return;
        }

        if (e.getInventory().getHolder() instanceof InvseeMenu.Holder holder) {
            if (e.getClick().isShiftClick()) {
                e.setCancelled(true);
                return;
            }

            int slot = e.getRawSlot();
            int size = e.getInventory().getSize();
            boolean top = slot >= 0 && slot < size;

            if (top && plugin.getInvseeMenu().isBlockedSlot(slot)) {
                e.setCancelled(true);
                return;
            }

            Player target = Bukkit.getPlayer(holder.getTargetUuid());
            if (target == null) {
                e.setCancelled(true);
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () ->
                    plugin.getInvseeMenu().saveToTarget(e.getInventory(), target));
        }
    }

    @EventHandler
    public void onInvseeDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof InvseeMenu.Holder holder) {
            for (int slot : e.getRawSlots()) {
                if (slot < e.getInventory().getSize() && plugin.getInvseeMenu().isBlockedSlot(slot)) {
                    e.setCancelled(true);
                    return;
                }
            }
            Player target = Bukkit.getPlayer(holder.getTargetUuid());
            if (target == null) {
                e.setCancelled(true);
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () ->
                    plugin.getInvseeMenu().saveToTarget(e.getInventory(), target));
        }
        if (e.getInventory().getHolder() instanceof InfoMenu.Holder) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInvseeClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof InvseeMenu.Holder holder) {
            Player target = Bukkit.getPlayer(holder.getTargetUuid());
            if (target != null) {
                plugin.getInvseeMenu().saveToTarget(e.getInventory(), target);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (!plugin.getFreezeManager().isFrozen(e.getPlayer().getUniqueId())) return;
        if (e.getTo() == null) return;
        if (e.getFrom().getBlockX() != e.getTo().getBlockX()
                || e.getFrom().getBlockY() != e.getTo().getBlockY()
                || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Text.color(plugin.getConfigManager().prefix()
                    + plugin.getConfigManager().msg("freeze-blocked", "&cВы заморожены и не можете двигаться!")));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (plugin.getFreezeManager().isFrozen(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (plugin.getFreezeManager().isFrozen(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (plugin.getFreezeManager().isFrozen(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if (plugin.getFreezeManager().isFrozen(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p
                && plugin.getFreezeManager().isFrozen(p.getUniqueId())) {
            e.setCancelled(true);
        }
        if (e.getEntity() instanceof Player p
                && plugin.getFreezeManager().isFrozen(p.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p
                && plugin.getFreezeManager().isFrozen(p.getUniqueId())) {
            e.setCancelled(true);
        }
    }
}
