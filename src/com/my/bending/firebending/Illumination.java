package com.my.bending.firebending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.my.bending.tools.Abilities;
import com.my.bending.tools.BendingPlayer;
import com.my.bending.tools.ConfigManager;
import com.my.bending.tools.Tools;

public class Illumination {

	public static ConcurrentHashMap<Player, Illumination> instances = new ConcurrentHashMap<Player, Illumination>();
	public static ConcurrentHashMap<Block, Player> blocks = new ConcurrentHashMap<Block, Player>();

	private static final int range = ConfigManager.illuminationRange;

	private Player player;
	private Block block;
	private Material normaltype;
	private byte normaldata;

	public Illumination(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.Illumination))
			return;
		if (instances.containsKey(player)) {
			instances.get(player).revert();
			instances.remove(player);
		} else {
			this.player = player;
			set();
			instances.put(player, this);
			bPlayer.cooldown(Abilities.Illumination);
		}
	}

	private void set() {
		Block standingblock = player.getLocation().getBlock();
		Block standblock = standingblock.getRelative(BlockFace.DOWN);
		if ((FireStream.isIgnitable(player, standingblock) && standblock
				.getType() != Material.LEAVES)
				&& block == null
				&& !blocks.containsKey(standblock)) {
			block = standingblock;
			normaltype = block.getType();
			normaldata = block.getData();
			block.setType(Material.TORCH);
			blocks.put(block, player);
		} else if ((FireStream.isIgnitable(player, standingblock) && standblock
				.getType() != Material.LEAVES)
				&& !block.equals(standblock)
				&& !blocks.containsKey(standblock) && Tools.isSolid(standblock)) {
			revert();
			block = standingblock;
			normaltype = block.getType();
			normaldata = block.getData();
			block.setType(Material.TORCH);
			blocks.put(block, player);
		} else if (block == null) {
			return;
		} else if (player.getWorld() != block.getWorld()) {
			revert();
		} else if (player.getLocation().distance(block.getLocation()) > Tools
				.firebendingDayAugment(range, player.getWorld())) {
			revert();
		}
	}

	private void revert() {
		if (block != null) {
			blocks.remove(block);
			block.setType(normaltype);
			block.setData(normaldata);
		}
	}

	public static void revert(Block block) {
		Player player = blocks.get(block);
		instances.get(player).revert();
	}

	public static void manage(Server server) {
		for (Player player : server.getOnlinePlayers()) {
			if (instances.containsKey(player)
					&& (!Tools.hasAbility(player, Abilities.Illumination) || !Tools
							.canBend(player, Abilities.Illumination))) {
				instances.get(player).revert();
				instances.remove(player);
			} else if (instances.containsKey(player)) {
				instances.get(player).set();
			}
		}

		for (Player player : instances.keySet()) {
			if (!player.isOnline() || player.isDead()) {
				instances.get(player).revert();
				instances.remove(player);
			}
		}
	}

	public static void removeAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).revert();
			instances.remove(player);
		}

	}

	public static String getDescription() {
		return "This ability gives firebenders a means of illuminating the area. It is a toggle - clicking "
				+ "will create a torch that follows you around. The torch will only appear on objects that are "
				+ "ignitable and can hold a torch (e.g. not leaves or ice). If you get too far away from the torch, "
				+ "it will disappear, but will reappear when you get on another ignitable block. Clicking again "
				+ "dismisses this torch.";
	}

}
