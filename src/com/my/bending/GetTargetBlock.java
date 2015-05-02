package com.my.bending;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class GetTargetBlock {
	public static Block getTargetBlock (Player player, int range) {
		
		if (range < 1) {
			throw new IllegalArgumentException("Range must be 1 or more.");
		}
		
		Location pl = player.getEyeLocation().subtract(0, 0.5, 0);

		double px = pl.getX();
		double py = pl.getY();
		double pz = pl.getZ();

		double yaw = Math.toRadians(pl.getYaw() + 90);
		double pitch = Math.toRadians(pl.getPitch() + 90);

		double x = Math.sin(pitch) * Math.cos(yaw);
		double y = Math.sin(pitch) * Math.sin(yaw);
		double z = Math.cos(pitch);
		
		for (int i = 1; i <= range; i++) {
				
			Location loc = new Location(player.getWorld(), px + i * x, py + i * z, pz + i * y);
				
				if (!(loc.getBlock().getType() == Material.AIR)) {
					return loc.getBlock();
				}
				
				if (i == range) {
					return loc.getBlock();
				}
		
		}
		return null;
	}
}