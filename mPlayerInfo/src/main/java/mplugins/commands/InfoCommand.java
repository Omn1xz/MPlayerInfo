package mplugins.commands;

import mplugins.MPlayerInfo;
import mplugins.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class InfoCommand implements CommandExecutor, TabCompleter {

    private final MPlayerInfo plugin;

    public InfoCommand(MPlayerInfo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mplayerinfo.use")) {
            sender.sendMessage(Text.color(plugin.getConfigManager().prefix()
                    + plugin.getConfigManager().msg("no-permission", "&cНет прав.")));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPlugin();
            sender.sendMessage(Text.color(plugin.getConfigManager().prefix()
                    + plugin.getConfigManager().msg("reloaded", "&aПлагин перезагружен.")));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(Text.color(plugin.getConfigManager().prefix()
                    + plugin.getConfigManager().msg("usage", "&eИспользование: /mplayerinfo <nick>")));
            return true;
        }

        if (!(sender instanceof Player viewer)) {
            sender.sendMessage(Text.color(plugin.getConfigManager().prefix()
                    + plugin.getConfigManager().msg("player-only", "&cТолько для игроков.")));
            return true;
        }

        String nick = args[0];
        Player online = Bukkit.getPlayerExact(nick);
        OfflinePlayer target;
        if (online != null) {
            target = online;
        } else {
            target = Bukkit.getOfflinePlayer(nick);
            if (target.getName() == null && !target.hasPlayedBefore()) {
                sender.sendMessage(Text.color(plugin.getConfigManager().prefix()
                        + plugin.getConfigManager().msg("player-not-found", "&cИгрок не найден.").replace("{target}", nick)));
                return true;
            }
        }

        plugin.getInfoMenu().open(viewer, target);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (!sender.hasPermission("mplayerinfo.use")) return out;
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            if ("reload".startsWith(prefix)) out.add("reload");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(prefix)) out.add(p.getName());
            }
        }
        return out;
    }
}
