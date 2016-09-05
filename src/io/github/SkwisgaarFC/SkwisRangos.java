package io.github.SkwisgaarFC;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class SkwisRangos extends JavaPlugin {
    public FileConfiguration config = getConfig();
    public static Economy econ = null;

    private void defaults() {
	config.addDefault("commands.playerpromote", "subirderango");
	config.addDefault("messages.playerpromote", "You've been promoted!");
	config.addDefault("messages.commandfromconsole", "The console can't be promoted!");
	config.addDefault("messages.notenoughmoney", "YOU'RE POOR!!!");
	config.addDefault("messages.rankpurchased", "Now your rank is ");
	config.addDefault("messages.errorpromote", "You couldn't have been promoted. Contact the admin");
	HashMap<String, Double> list = new HashMap<String, Double>();
	list.put("Newfag", 0.0);
	list.put("Usuario", 25000.0);
	list.put("Elite", 100000.0);
	config.addDefault("ranks.list", list);
	
    }

    private void loadPluginConfig() {
	//defaults();
	config.options().copyDefaults(true);
	saveConfig();
    }

    private boolean setupEconomy() {
	if (getServer().getPluginManager().getPlugin("Vault") == null) {
	    return false;
	}
	RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
	if (rsp == null) {
	    return false;
	}
	econ = rsp.getProvider();
	return econ != null;
    }

    public void onEnable() {
	if (!setupEconomy()) {
	    getLogger().severe(
		    String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
	    getServer().getPluginManager().disablePlugin(this);
	}
	loadPluginConfig();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
	// Command to promote
	if (cmd.getName().equalsIgnoreCase("subirderango")) {
	    Player p = (Player) sender;
	    if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
		p.sendMessage(ChatColor.GOLD + "This plugin has been coded by " + ChatColor.DARK_RED + "SkwisgaarFC");
		return true;
	    }
	    if (args.length == 1 && args[0].equalsIgnoreCase("version")) {
		p.sendMessage("No version needed to be known");
		return true;
	    }
	    if (args.length > 1) {
		return false;
	    }

	    if (!(sender instanceof Player)) {
		p.sendMessage(config.getString("messages.commandfromconsole"));
		return true;
	    }
	    
	    // Get user rank, so we can charge user the correct amount of money
	    String playerRank = PermissionsEx.getUser(p).getRankLadderGroup("default").getName();
	    
	    Object rankList = config.get("ranks.list");
	    double rankCost = (double) ((MemorySection) rankList).get(playerRank);

	    // Let's charge the player. Ranks ain't free, sweetheart!
	    if (econ.getBalance(p) >= rankCost) {
		EconomyResponse r = econ.withdrawPlayer(p, rankCost);
		if (r.transactionSuccess()) {
		    if (promotePlayer(p)) { // promotePlayer returns true if
					    // promotion was successful
			p.sendMessage(ChatColor.GREEN + config.getString("messages.rankpurchased") + "Usuario");
		    }
		} else {
		    p.sendMessage(ChatColor.GREEN + config.getString("messages.errorpromoted"));
		}
	    } else {
		p.sendMessage(ChatColor.RED + config.getString("messages.notenoughmoney"));
	    }
	}
	return false;
    }

    private boolean promotePlayer(Player p) {
	Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex promote " + p.getName());
	p.sendMessage(ChatColor.GREEN + config.getString("messages.playerpromote"));
	return true;

    }
}
