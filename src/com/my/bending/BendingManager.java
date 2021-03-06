package com.my.bending;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.my.bending.tools.Abilities;
import com.my.bending.tools.AvatarState;
import com.my.bending.tools.BendingType;
import com.my.bending.tools.ConfigManager;
import com.my.bending.tools.Flight;
import com.my.bending.tools.TempPotionEffect;
import com.my.bending.tools.Tools;
import com.my.bending.waterbending.Bloodbending;
import com.my.bending.waterbending.FastSwimming;
import com.my.bending.waterbending.FreezeMelt;
import com.my.bending.waterbending.HealingWaters;
import com.my.bending.waterbending.IceSpike;
import com.my.bending.waterbending.IceSpike2;
import com.my.bending.waterbending.OctopusForm;
import com.my.bending.waterbending.Plantbending;
import com.my.bending.waterbending.Torrent;
import com.my.bending.waterbending.TorrentBurst;
import com.my.bending.waterbending.WaterManipulation;
import com.my.bending.waterbending.WaterPassive;
import com.my.bending.waterbending.WaterReturn;
import com.my.bending.waterbending.WaterSpout;
import com.my.bending.waterbending.WaterWall;
import com.my.bending.waterbending.Wave;
import com.my.bending.airbending.AirBlast;
import com.my.bending.airbending.AirBubble;
import com.my.bending.airbending.AirBurst;
import com.my.bending.airbending.AirPassive;
import com.my.bending.airbending.AirScooter;
import com.my.bending.airbending.AirShield;
import com.my.bending.airbending.AirSpout;
import com.my.bending.airbending.AirSuction;
import com.my.bending.airbending.AirSwipe;
import com.my.bending.airbending.Speed;
import com.my.bending.chiblocking.RapidPunch;
import com.my.bending.earthbending.Catapult;
import com.my.bending.earthbending.CompactColumn;
import com.my.bending.earthbending.EarthArmor;
import com.my.bending.earthbending.EarthBlast;
import com.my.bending.earthbending.EarthColumn;
import com.my.bending.earthbending.EarthPassive;
import com.my.bending.earthbending.EarthTunnel;
import com.my.bending.earthbending.Shockwave;
import com.my.bending.earthbending.Tremorsense;
import com.my.bending.firebending.Cook;
import com.my.bending.firebending.Enflamed;
import com.my.bending.firebending.FireBlast;
import com.my.bending.firebending.FireBurst;
import com.my.bending.firebending.FireJet;
import com.my.bending.firebending.FireShield;
import com.my.bending.firebending.FireStream;
import com.my.bending.firebending.Fireball;
import com.my.bending.firebending.Illumination;
import com.my.bending.firebending.Lightning;
import com.my.bending.firebending.WallOfFire;

public class BendingManager implements Runnable {

	public Bending plugin;

	public static ArrayList<Player> flyingplayers = new ArrayList<Player>();

	// private static boolean safeRevert = ConfigManager.safeRevert;

	private boolean verbose = false;
	private long verbosetime;
	private long verboseinterval = 3 * 60 * 1000;

	long time;
	long interval;
	long reverttime;
	ArrayList<World> worlds = new ArrayList<World>();
	ConcurrentHashMap<World, Boolean> nights = new ConcurrentHashMap<World, Boolean>();
	ConcurrentHashMap<World, Boolean> days = new ConcurrentHashMap<World, Boolean>();

	static final String defaultsunrisemessage = "You feel the strength of the rising sun empowering your firebending.";
	static final String defaultsunsetmessage = "You feel the empowering of your firebending subside as the sun sets.";
	static final String defaultmoonrisemessage = "You feel the strength of the rising moon empowering your waterbending.";
	static final String defaultmoonsetmessage = "You feel the empowering of your waterbending subside as the moon sets.";

	public BendingManager(Bending bending) {
		plugin = bending;
		time = System.currentTimeMillis();
		verbosetime = System.currentTimeMillis();
		reverttime = time;
	}

	public void run() {

		try {

			interval = System.currentTimeMillis() - time;
			time = System.currentTimeMillis();
			Bending.time_step = interval;

			manageAirbending();
			manageEarthbending();
			manageFirebending();
			manageWaterbending();
			manageChiBlocking();
			// manageMessages();
			TempPotionEffect.progressAll();
			AvatarState.manageAvatarStates();
			// handleFlying();
			Flight.handle();
			handleDayNight();

			if (verbose
					&& System.currentTimeMillis() > verbosetime
							+ verboseinterval)
				handleVerbosity();

		} catch (Exception e) {
			Tools.stopAllBending();
			Tools.writeToLog("Bending broke!");
			Tools.writeToLog(ExceptionUtils.getStackTrace(e));
			Tools.verbose("Bending just broke! It seems to have saved itself. The cause was reported in bending.log, and is repeated here for your convenience:");
			e.printStackTrace();
		}

	}

	private void manageAirbending() {
		AirPassive.handlePassive(plugin.getServer());
		AirBubble.handleBubbles(plugin.getServer());

		AirBlast.progressAll();

		for (int ID : AirShield.instances.keySet()) {
			AirShield.progress(ID);
		}

		AirSuction.progressAll();

		for (int ID : AirSwipe.instances.keySet()) {
			AirSwipe.progress(ID);
		}

		for (int ID : Speed.instances.keySet()) {
			Speed.progress(ID);
		}

		AirBurst.progressAll();

		AirScooter.progressAll();

		AirSpout.spoutAll();
	}

	private void manageEarthbending() {

		for (int ID : Catapult.instances.keySet()) {
			Catapult.progress(ID);
		}

		for (int ID : EarthColumn.instances.keySet()) {
			EarthColumn.progress(ID);
		}

		for (int ID : CompactColumn.instances.keySet()) {
			CompactColumn.progress(ID);
		}

		for (int ID : EarthBlast.instances.keySet()) {
			EarthBlast.progress(ID);
		}

		for (Player player : EarthTunnel.instances.keySet()) {
			EarthTunnel.progress(player);
		}

		for (Player player : EarthArmor.instances.keySet()) {
			EarthArmor.moveArmor(player);
		}
		EarthPassive.revertSands();

		Shockwave.progressAll();

		Tremorsense.manage(plugin.getServer());

		for (Block block : RevertChecker.revertQueue.keySet()) {
			// Tools.removeEarthbendedBlockByIndex(block);
			// if (Tools.revertBlock(block))
			Tools.revertBlock(block);
			RevertChecker.revertQueue.remove(block);
		}

		for (int i : RevertChecker.airRevertQueue.keySet()) {
			Tools.revertAirBlock(i);
			RevertChecker.airRevertQueue.remove(i);
		}

		// for (Block block : RevertChecker.movedEarthQueue.keySet()) {
		// block.setType(RevertChecker.movedEarthQueue.get(block));
		// RevertChecker.movedEarthQueue.remove(block);
		// }

	}

	private void manageFirebending() {

		for (int ID : FireStream.instances.keySet()) {
			FireStream.progress(ID);
		}

		for (Block block : FireStream.ignitedblocks.keySet()) {
			if (block.getType() != Material.FIRE) {
				FireStream.ignitedblocks.remove(block);
			}
		}

		Fireball.progressAll();

		WallOfFire.manage();

		Lightning.progressAll();

		FireShield.progressAll();

		FireBlast.progressAll();

		FireBurst.progressAll();

		FireJet.progressAll();

		FireStream.dissipateAll();

		Cook.progressAll();

		Illumination.manage(plugin.getServer());

		Enflamed.handleFlames();

	}

	private void manageChiBlocking() {
		for (Player p : RapidPunch.instance.keySet())
			RapidPunch.instance.get(p).startPunch(p);
	}

	private void manageWaterbending() {
		// WalkOnWater.handleFreezing(plugin.getServer());

		FreezeMelt.handleFrozenBlocks();

		WaterSpout.handleSpouts(plugin.getServer());

		for (int ID : WaterManipulation.instances.keySet()) {
			WaterManipulation.progress(ID);
		}

		for (int ID : WaterWall.instances.keySet()) {
			WaterWall.progress(ID);
		}

		for (int ID : Wave.instances.keySet()) {
			Wave.progress(ID);
		}

		for (int ID : IceSpike.instances.keySet()) {
			IceSpike.instances.get(ID).progress();
		}

		for (int ID : IceSpike.instances.keySet()) {
			IceSpike.instances.get(ID).progress();
		}

		IceSpike2.progressAll();

		Torrent.progressAll();
		TorrentBurst.progressAll();

		Bloodbending.progressAll();

		HealingWaters.heal(plugin.getServer());

		WaterPassive.handlePassive(plugin.getServer());
		FastSwimming.HandleSwim(plugin.getServer());
		OctopusForm.progressAll();

		Plantbending.regrow();

		WaterReturn.progressAll();

	}

	// private void handleFlying() {
	//
	// ArrayList<Player> players = new ArrayList<Player>();
	// ArrayList<Player> newflyingplayers = new ArrayList<Player>();
	// ArrayList<Player> avatarstateplayers = new ArrayList<Player>();
	// ArrayList<Player> airscooterplayers = new ArrayList<Player>();
	// ArrayList<Player> waterspoutplayers = new ArrayList<Player>();
	// ArrayList<Player> airspoutplayers = new ArrayList<Player>();
	//
	// players.addAll(Tornado.getPlayers());
	// players.addAll(Speed.getPlayers());
	// players.addAll(FireJet.getPlayers());
	// players.addAll(Catapult.getPlayers());
	// avatarstateplayers = AvatarState.getPlayers();
	// airscooterplayers = AirScooter.getPlayers();
	// waterspoutplayers = WaterSpout.getPlayers();
	// airspoutplayers = AirSpout.getPlayers();
	// // players.addAll(avatarstateplayers);
	//
	// for (Player player : plugin.getServer().getOnlinePlayers()) {
	// if (avatarstateplayers.contains(player)
	// || airscooterplayers.contains(player)
	// || waterspoutplayers.contains(player)
	// || airspoutplayers.contains(player)) {
	// continue;
	// }
	// if (Bloodbending.isBloodbended(player)) {
	// player.setAllowFlight(true);
	// player.setFlying(false);
	// continue;
	// }
	// if (flyingplayers.contains(player) && players.contains(player)) {
	// player.setAllowFlight(true);
	// if (player.getGameMode() != GameMode.CREATIVE)
	// player.setFlying(false);
	// newflyingplayers.add(player);
	// } else if (players.contains(player)
	// && !flyingplayers.contains(player)) {
	// newflyingplayers.add(player);
	// if (player.getGameMode() != GameMode.CREATIVE)
	// player.setFlying(false);
	// } else if (flyingplayers.contains(player)
	// && !players.contains(player)) {
	// player.setAllowFlight(player.getGameMode() == GameMode.CREATIVE);
	// if (player.getGameMode() != GameMode.CREATIVE)
	// player.setFlying(false);
	// } else {
	// player.setAllowFlight(player.getGameMode() == GameMode.CREATIVE);
	// if (player.getGameMode() != GameMode.CREATIVE)
	// player.setFlying(false);
	// }
	// }
	// flyingplayers.clear();
	// flyingplayers.addAll(newflyingplayers);
	// }
	//
	// public static void removeFlyers() {
	// for (Player player : flyingplayers) {
	// player.setAllowFlight(player.getGameMode() == GameMode.CREATIVE);
	// }
	// }

	private void handleDayNight() {

		for (World world : plugin.getServer().getWorlds())
			if (world.getWorldType() == WorldType.NORMAL
					&& !worlds.contains(world)) {
				worlds.add(world);
				nights.put(world, false);
				days.put(world, false);
			}

		ArrayList<World> removeworlds = new ArrayList<World>();
		for (World world : worlds) {
			if (!plugin.getServer().getWorlds().contains(world)) {
				removeworlds.add(world);
				continue;
			}
			boolean night = nights.get(world);
			boolean day = days.get(world);
			if (Tools.isDay(world) && !day) {
				for (Player player : world.getPlayers()) {
					if (Tools.isBender(player.getName(), BendingType.Fire)
							&& player
									.hasPermission("bending.message.daymessage")) {
						ChatColor color = ChatColor.WHITE;
						color = Tools.getColor(ConfigManager.getColor("Fire"));
						player.sendMessage(color
								+ "You feel the strength of the rising sun empowering your firebending.");
					}
				}
				days.replace(world, true);
			}

			if (!Tools.isDay(world) && day) {
				for (Player player : world.getPlayers()) {
					if (Tools.isBender(player.getName(), BendingType.Fire)
							&& player
									.hasPermission("bending.message.daymessage")) {
						ChatColor color = ChatColor.WHITE;
						color = Tools.getColor(ConfigManager.getColor("Fire"));
						player.sendMessage(color
								+ "You feel the empowering of your firebending subside as the sun sets.");
					}
				}
				days.replace(world, false);
			}

			if (Tools.isNight(world) && !night) {
				for (Player player : world.getPlayers()) {
					if (Tools.isBender(player.getName(), BendingType.Water)
							&& player
									.hasPermission("bending.message.nightmessage")) {
						ChatColor color = ChatColor.WHITE;
						color = Tools.getColor(ConfigManager.getColor("Water"));
						player.sendMessage(color
								+ "You feel the strength of the rising moon empowering your waterbending.");
					}
				}
				nights.replace(world, true);
			}

			if (!Tools.isNight(world) && night) {
				for (Player player : world.getPlayers()) {
					if (Tools.isBender(player.getName(), BendingType.Water)
							&& player
									.hasPermission("bending.message.nightmessage")) {
						ChatColor color = ChatColor.WHITE;
						color = Tools.getColor(ConfigManager.getColor("Water"));
						player.sendMessage(color
								+ "You feel the empowering of your waterbending subside as the moon sets.");
					}
				}
				nights.replace(world, false);
			}
		}

		for (World world : removeworlds) {
			worlds.remove(world);
		}

	}

	// private void manageMessages() {
	// for (Player player : newplayers) {
	// player.sendMessage(ChatColor.GOLD
	// + "Use '/bending choose <element>' to get started!");
	// }
	// newplayers.clear();
	// }

	private void handleVerbosity() {
		verbosetime = System.currentTimeMillis();

		int airblasts, airbubbles, airscooters, airshields, airsuctions, airswipes, tornados; // ,airbursts,
		// airspouts;

		int airblastplayers = 0;
		airblasts = AirBlast.instances.size();

		int airbubbleplayers = 0;
		airbubbles = AirBubble.instances.size();

		// int airburstplayers = 0;
		// airbursts = AirBurst.instances.size();

		int airscooterplayers = 0;
		airscooters = AirScooter.instances.size();

		int airshieldplayers = 0;
		airshields = AirShield.instances.size();

		// int airspoutplayer = 0;
		// airspouts = AirSpout.instances.size();

		int airsuctionplayers = 0;
		airsuctions = AirSuction.instances.size();

		int airswipeplayers = 0;
		airswipes = AirSwipe.instances.size();

		int catapults, compactcolumns, earthblasts, earthcolumns, earthtunnels, tremorsenses; // ,shockwaves;

		int catapultplayers = 0;
		catapults = Catapult.instances.size();

		int compactcolumnplayers = 0;
		compactcolumns = CompactColumn.instances.size();

		int earthblastplayers = 0;
		earthblasts = EarthBlast.instances.size();

		int earthcolumnplayers = 0;
		earthcolumns = EarthColumn.instances.size();

		int earthtunnelplayers = 0;
		earthtunnels = EarthTunnel.instances.size();

		// int shockwaveplayers = 0;
		// shockwaves = Shockwave.instances.size();

		int tremorsenseplayers = 0;
		tremorsenses = Tremorsense.instances.size();

		int fireballs, fireblasts, firejets, firestreams, illuminations, walloffires; // ,lightings;

		int fireblastplayers = 0;
		fireblasts = FireBlast.instances.size();

		int firestreamplayers = 0;
		firestreams = FireStream.instances.size();

		int fireballplayers = 0;
		fireballs = Fireball.instances.size();

		int firejetplayers = 0;
		firejets = FireJet.instances.size();

		int illuminationplayers = 0;
		illuminations = Illumination.instances.size();

		// int lightningplayers = 0;
		// lightnings = Lightning.instances.size();

		int walloffireplayers = 0;
		walloffires = WallOfFire.instances.size();

		int bloodbendings, freezemelts, watermanipulations, waterspouts, waterwalls, waves;

		int bloodbendingplayers = 0;
		bloodbendings = Bloodbending.instances.size();

		int freezemeltplayers = 0;
		freezemelts = FreezeMelt.frozenblocks.size();

		int watermanipulationplayers = 0;
		watermanipulations = WaterManipulation.instances.size();

		int waterspoutplayers = 0;
		waterspouts = WaterSpout.instances.size();

		int waterwallplayers = 0;
		waterwalls = WaterWall.instances.size();

		int waveplayers = 0;
		waves = Wave.instances.size();

		for (Player player : plugin.getServer().getOnlinePlayers()) {
			Abilities ability = Tools.getBendingAbility(player);
			if (ability == Abilities.AirBlast)
				airblastplayers++;
			if (ability == Abilities.AirBubble)
				airbubbleplayers++;
			// if (ability==Abilities.AirBurst)
			// airburstplayers++;
			if (ability == Abilities.AirScooter)
				airscooterplayers++;
			if (ability == Abilities.AirShield)
				airshieldplayers++;
			// if (ability==Abilities.AirSpout)
			// airspoutplayers++;
			if (ability == Abilities.AirSuction)
				airsuctionplayers++;
			if (ability == Abilities.AirSwipe)
				airswipeplayers++;
			if (ability == Abilities.Catapult)
				catapultplayers++;
			if (ability == Abilities.Collapse)
				compactcolumnplayers++;
			// if (ability == Abilities.CompactColumn)
			// compactcolumnplayers++;
			if (ability == Abilities.EarthBlast)
				earthblastplayers++;
			if (ability == Abilities.RaiseEarth)
				earthcolumnplayers++;
			if (ability == Abilities.EarthGrab)
				earthcolumnplayers++;
			if (ability == Abilities.EarthTunnel)
				earthtunnelplayers++;
			if (Tools.hasAbility(player, Abilities.Tremorsense))
				tremorsenseplayers++;
			// if (ability==Abilities.Shockwave) shockwaveplayers++;

			if (ability == Abilities.Blaze)
				firestreamplayers++;
			if (ability == Abilities.FireBlast)
				fireballplayers++;
			if (ability == Abilities.FireBlast)
				fireblastplayers++;
			if (Tools.hasAbility(player, Abilities.FireJet))
				firejetplayers++;
			if (Tools.hasAbility(player, Abilities.Illumination))
				illuminationplayers++;
			// if (ability==Abilities.Lightning) lightningplayers++;
			if (ability == Abilities.WallOfFire)
				walloffireplayers++;

			if (ability == Abilities.Bloodbending)
				bloodbendingplayers++;
			if (Tools.hasAbility(player, Abilities.PhaseChange))
				freezemeltplayers++;
			// if (ability == Abilities.WalkOnWater)
			// freezemeltplayers++;
			if (ability == Abilities.WaterBubble)
				airbubbleplayers++;
			if (ability == Abilities.WaterManipulation)
				watermanipulationplayers++;
			if (Tools.hasAbility(player, Abilities.WaterSpout))
				waterspoutplayers++;
			if (ability == Abilities.Surge)
				waterwallplayers++;
			if (ability == Abilities.Surge)
				waveplayers++;
		}

		Tools.writeToLog("Debug data at "
				+ Calendar.getInstance().get(Calendar.HOUR) + "h "
				+ Calendar.getInstance().get(Calendar.MINUTE) + "m "
				+ Calendar.getInstance().get(Calendar.SECOND) + "s");

		verbose("airblasts", airblasts, airblastplayers, false);
		verbose("airbubbles", airbubbles, airbubbleplayers, true);
		// verbose("airbursts", airbursts, airburstplayers, false);
		verbose("airscooters", airscooters, airscooterplayers, true);
		verbose("airshields", airshields, airshieldplayers, true);
		// verbose("airspouts", airspouts, airspoutplayers, false);
		verbose("airsuctions", airsuctions, airsuctionplayers, false);
		verbose("airswipes", airswipes, airswipeplayers, false);

		verbose("catapults", catapults, catapultplayers, true);
		verbose("compactcolumns", compactcolumns, compactcolumnplayers, false);
		verbose("earthblasts", earthblasts, earthblastplayers, true);
		verbose("earthcolumns", earthcolumns, earthcolumnplayers, false);
		verbose("earthtunnels", earthtunnels, earthtunnelplayers, true);
		// verbose("shockwaves", shockwaves, shockwaveplayers, false);
		verbose("tremorsenses", tremorsenses, tremorsenseplayers, true);

		verbose("fireballs", fireballs, fireballplayers, false);
		verbose("fireblasts", fireblasts, fireblastplayers, false);
		verbose("firejets", firejets, firejetplayers, true);
		verbose("firestreams", firestreams, firestreamplayers, false);
		verbose("illuminations", illuminations, illuminationplayers, true);
		// verbose("lightnings", lightnings, lightningplayers, true);
		verbose("walloffires", walloffires, walloffireplayers, false);

		verbose("bloodbendings", bloodbendings, bloodbendingplayers, true);
		verbose("freezemelts", freezemelts, freezemeltplayers, false);
		verbose("watermanipulations", watermanipulations,
				watermanipulationplayers, false);
		verbose("waterspouts", waterspouts, waterspoutplayers, true);
		verbose("waterwalls", waterwalls, waterwallplayers, true);
		verbose("waves", waves, waveplayers, true);

		Tools.writeToLog(null);
	}

	private void verbose(String name, int instances, int players,
			boolean warning) {
		if (warning && instances > players) {
			name = "==WARNING== " + name;
		}
		Tools.writeToLog(name + ": " + instances + " instances for " + players
				+ " players.");
	}

}
