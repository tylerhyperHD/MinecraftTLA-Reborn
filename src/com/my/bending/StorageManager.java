package com.my.bending;

import java.io.File;

import org.bukkit.plugin.Plugin;

public class StorageManager {

	private File dataFolder;
	public BendingPlayers config;
	public static Boolean useMySQL;
	public static Boolean useFlatFile;

	private Plugin tapi;

	static final String on_air_choose = "As an airbender, you now take no falling damage, have faster sprinting and higher "
			+ "jumps. Additionally, daily activities are easier for you - your food meter decays at a "
			+ "much slower rate";
	static final String on_earth_choose = "As an earthbender, upon landing on bendable earth, you will briefly turn the "
			+ "area to soft sand, negating any fall damage you would have otherwise taken.";
	static final String on_water_choose = "As a waterbender, you no longer take any fall damage when landing on ice, snow "
			+ "or even 1-block-deep water. Additionally, sneaking in the water with a bending ability "
			+ "selected that does not utilize sneak (or no ability at all)"
			+ " will give you accelerated swimming. "
			+ "Lastly, you can pull water from plants with your abilities.";
	static final String on_fire_choose = "As a firebender, you now more quickly smother yourself when you catch on fire.";
	static final String on_chi_choose = "As a chiblocker, you have no active abilities to bind. Instead, you have improved "
			+ "sprinting and jumping, have a dodge chance and deal more damage with your fists. "
			+ "Additionally, punching a bender will block his/her chi for a few seconds, preventing "
			+ "him/her from bending (and even stopping their passive!).";
}