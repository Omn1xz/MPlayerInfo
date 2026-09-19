package mplugins.menu;

import mplugins.MPlayerInfo;
import mplugins.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class InvseeMenu {

    private final MPlayerInfo plugin;

    public InvseeMenu(MPlayerInfo plugin) {
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

    public void open(Player viewer, Player target) {
        String title = Text.color(plugin.getConfigManager().getInvseeTitle()
                .replace("{target}", target.getName())
                .replace("{viewer}", viewer.getName()));
        if (title.length() > 32) title = title.substring(0, 32);
        Holder holder = new Holder(target.getUniqueId());
        Inventory gui = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(gui);
        fillFromTarget(gui, target);
        viewer.openInventory(gui);
    }

    public void fillFromTarget(Inventory gui, Player target) {
        ItemStack[] armor = target.getInventory().getArmorContents();
        gui.setItem(0, armor.length > 3 ? armor[3] : null);
        gui.setItem(1, armor.length > 2 ? armor[2] : null);
        gui.setItem(2, armor.length > 1 ? armor[1] : null);
        gui.setItem(3, armor.length > 0 ? armor[0] : null);
        gui.setItem(4, target.getInventory().getItemInOffHand());

        ItemStack[] storage = target.getInventory().getStorageContents();
        for (int i = 0; i < 36 && i < storage.length; i++) {
            gui.setItem(9 + i, storage[i]);
        }
    }

    public void saveToTarget(Inventory gui, Player target) {
        try {
            target.getInventory().setHelmet(gui.getItem(0));
            target.getInventory().setChestplate(gui.getItem(1));
            target.getInventory().setLeggings(gui.getItem(2));
            target.getInventory().setBoots(gui.getItem(3));
            target.getInventory().setItemInOffHand(gui.getItem(4));

            ItemStack[] storage = new ItemStack[36];
            for (int i = 0; i < 36; i++) {
                storage[i] = gui.getItem(9 + i);
            }
            target.getInventory().setStorageContents(storage);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not save invsee: " + e.getMessage());
        }
    }

    public boolean isBlockedSlot(int slot) {
        if (slot >= 5 && slot <= 8) return true;
        return slot >= 45 && slot <= 53;
    }
}
