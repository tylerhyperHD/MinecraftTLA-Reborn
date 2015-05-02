package com.my.bending;

import com.my.bending.listener.BendingListener;
import com.my.bending.listener.TagAPIListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.my.bending.tools.Abilities;
import com.my.bending.tools.BendingPlayer;
import com.my.bending.tools.ConfigManager;
import com.my.bending.tools.Tools;
import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;

public class Bending extends JavaPlugin {

	public static long time_step = 1;
	public static Logger log = Logger.getLogger("Bending");

	public static Bending plugin;

	public final BendingManager manager = new BendingManager(this);
	public final BendingListener listener = new BendingListener(this);
	private final RevertChecker revertChecker = new RevertChecker(this);
	private final BendingPlayersSaver saver = new BendingPlayersSaver();
	public final TagAPIListener Taglistener = new TagAPIListener();
	public static Consumer logblock = null;
	static Map<String, String> commands = new HashMap<String, String>();
	public static ConfigManager configManager = new ConfigManager();
	public static Language language = new Language();
	public BendingPlayers config;
	public Tools tools;

	public String[] waterbendingabilities;
	public String[] airbendingabilities;
	public String[] earthbendingabilities;
	public String[] firebendingabilities;
	public String[] chiblockingabilities;

	static int air = 0, earth = 0, water = 0, fire = 0, chi = 0;

	public void onDisable() {
        getLogger().info("Bending Reborn disabled.");
		Tools.stopAllBending();
		BendingPlayersSaver.save();
		getServer().getScheduler().cancelTasks(plugin);
	}

	public void onEnable() {

		plugin = this;
        getLogger().info("Enabling Bending Reborn by tylerhyperHD");
		ConfigurationSerialization.registerClass(BendingPlayer.class,
				"BendingPlayer");

		configManager.load(new File(getDataFolder(), "config.yml"));
		language.load(new File(getDataFolder(), "language.yml"));

		Plugin lb = getServer().getPluginManager().getPlugin("LogBlock");
		if (lb != null) {
			logblock = ((LogBlock) lb).getConsumer();
		}

		config = new BendingPlayers(getDataFolder());
		BendingPlayer.initializeCooldowns();

		tools = new Tools(config);

		waterbendingabilities = Abilities.getWaterbendingAbilities();
		airbendingabilities = Abilities.getAirbendingAbilities();
		earthbendingabilities = Abilities.getEarthbendingAbilities();
		firebendingabilities = Abilities.getFirebendingAbilities();
		chiblockingabilities = Abilities.getChiBlockingAbilities();

		getServer().getPluginManager().registerEvents(listener, this);

		if (Bukkit.getPluginManager().getPlugin("TagAPI") != null
				&& ConfigManager.useTagAPI) {
			getServer().getPluginManager().registerEvents(Taglistener, this);
		}

		getServer().getScheduler().scheduleSyncRepeatingTask(this, manager, 0,
				1);

		getServer().getScheduler().runTaskTimerAsynchronously(plugin,
				revertChecker, 0, 200);
		getServer().getScheduler().runTaskTimerAsynchronously(plugin, saver, 0,
				20 * 60 * 5);

		Tools.printHooks();
		Tools.verbose("Bending Reborn v" + this.getDescription().getVersion() + " has been loaded.");
		registerCommands();

	}

	public void reloadConfiguration() {
		getConfig().options().copyDefaults(true);
		saveConfig();

	}

	private void registerCommands() {
		commands.put("command.admin", "remove <player>");
		commands.put("admin.reload", "reload");
		commands.put("admin.permaremove", "permaremove <player>");
		commands.put("command.choose", "choose <element>");
		commands.put("admin.choose", "choose <player> <element>");
		commands.put("admin.add", "add <element>");
		commands.put("command.displayelement", "display <element>");
		commands.put("command.clear", "clear");
		commands.put("command.display", "display");
		commands.put("command.bind", "bind <ability>");
		commands.put("command.version", "version");
		commands.put("command.bindmode", "bindmode [item/slot]");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (cmd.getName().equalsIgnoreCase("bending")) {
			new BendingCommand(player, args, getDataFolder(), getServer());
		}

		return true;
	}
}