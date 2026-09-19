package mplugins;

import mplugins.commands.InfoCommand;
import mplugins.commands.InvseeCommand;
import mplugins.config.ConfigManager;
import mplugins.listeners.PlayerListener;
import mplugins.managers.FreezeManager;
import mplugins.managers.SessionManager;
import mplugins.managers.VanishManager;
import mplugins.menu.InfoMenu;
import mplugins.menu.InvseeMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MPlayerInfo extends JavaPlugin {

    private static MPlayerInfo instance;

    private ConfigManager configManager;
    private SessionManager sessionManager;
    private VanishManager vanishManager;
    private FreezeManager freezeManager;
    private InfoMenu infoMenu;
    private InvseeMenu invseeMenu;

    public static MPlayerInfo getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.sessionManager = new SessionManager();
        this.vanishManager = new VanishManager(this);
        this.freezeManager = new FreezeManager();
        this.infoMenu = new InfoMenu(this);
        this.invseeMenu = new InvseeMenu(this);

        configManager.load();

        long now = System.currentTimeMillis();
        for (Player p : Bukkit.getOnlinePlayers()) {
            sessionManager.markJoin(p.getUniqueId(), now);
            vanishManager.setVanishedQuiet(p.getUniqueId(), false);
        }

        InfoCommand infoCommand = new InfoCommand(this);
        InvseeCommand invseeCommand = new InvseeCommand(this);

        if (getCommand("mplayerinfo") != null) {
            getCommand("mplayerinfo").setExecutor(infoCommand);
            getCommand("mplayerinfo").setTabCompleter(infoCommand);
        }
        if (getCommand("invsee") != null) {
            getCommand("invsee").setExecutor(invseeCommand);
            getCommand("invsee").setTabCompleter(invseeCommand);
        }

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("mPlayerInfo enabled.");
    }

    @Override
    public void onDisable() {
        if (vanishManager != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (vanishManager.isVanished(p.getUniqueId())) {
                    vanishManager.setVanished(p, false);
                }
            }
        }
        getLogger().info("mPlayerInfo disabled.");
    }

    public void reloadPlugin() {
        reloadConfig();
        configManager.load();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public VanishManager getVanishManager() {
        return vanishManager;
    }

    public FreezeManager getFreezeManager() {
        return freezeManager;
    }

    public InfoMenu getInfoMenu() {
        return infoMenu;
    }

    public InvseeMenu getInvseeMenu() {
        return invseeMenu;
    }
}
