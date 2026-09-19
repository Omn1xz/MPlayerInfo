package mplugins.commands;

import mplugins.MPlayerInfo;
import mplugins.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class InvseeCommand implements CommandExecutor, TabCompleter {

    private final MPlayerInfo plugin;

    public InvseeCommand(MPlayerInfo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mplayerinfo.use")) {
            sender.sendMessage(Text.color(plugin.getConfigManager().prefix()
                    + plugin.getConfigManager().msg("no-permission", "&cНет прав.")));
            return true;
        }
        if (!(sender instanceof Player viewer)) {
            sender.sendMessage(Text.color(plugin.getConfigManager().prefix()
                    + plugin.getConfigManager().msg("player-only", "&cТолько для игроков.")));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(Text.color(plugin.getConfigManager().prefix()
                    + plugin.getConfigManager().msg("invsee-usage", "&eИспользование: /invsee <nick>")));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(Text.color(plugin.getConfigManager().prefix()
                    + plugin.getConfigManager().msg("player-not-found", "&cИгрок не найден.").replace("{target}", args[0])));
            return true;
        }
        plugin.getInvseeMenu().open(viewer, target);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(prefix)) out.add(p.getName());
            }
        }
        return out;
    }
}
