package com.my.bending.waterbending;

import com.my.bending.GetTargetBlock;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.my.bending.tools.Abilities;
import com.my.bending.tools.AvatarState;
import com.my.bending.tools.BendingPlayer;
import com.my.bending.tools.ConfigManager;
import com.my.bending.tools.TempBlock;
import com.my.bending.tools.Tools;

public class Melt {

	private static final int defaultrange = FreezeMelt.defaultrange;
	private static final int defaultradius = FreezeMelt.defaultradius;
	private static final int defaultevaporateradius = 3;
	private static final int seaLevel = ConfigManager.seaLevel;

	private static final byte full = 0x0;

	public Melt(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.PhaseChange))
			return;

		int range = (int) Tools.waterbendingNightAugment(defaultrange,
				player.getWorld());
		int radius = (int) Tools.waterbendingNightAugment(defaultradius,
				player.getWorld());

		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(range);
			radius = AvatarState.getValue(radius);
		}
		boolean evaporate = false;
		Location location = Tools.getTargetedLocation(player, range);
		if (Tools.isWater(GetTargetBlock.getTargetBlock(null, range))
				&& !(player.getEyeLocation().getBlockY() <= 62)) {
			evaporate = true;
			radius = (int) Tools.waterbendingNightAugment(
					defaultevaporateradius, player.getWorld());
		}
		for (Block block : Tools.getBlocksAroundPoint(location, radius)) {
			if (evaporate) {
				if (block.getY() > seaLevel)
					evaporate(player, block);
			} else {
				melt(player, block);
			}
		}

		bPlayer.cooldown(Abilities.PhaseChange);
	}

	public static void melt(Player player, Block block) {
		if (Tools.isRegionProtectedFromBuild(player, Abilities.PhaseChange,
				block.getLocation()))
			return;
		if (!Wave.canThaw(block)) {
			Wave.thaw(block);
			return;
		}
		if (!Torrent.canThaw(block)) {
			Torrent.thaw(block);
			return;
		}
		if (Tools.isMeltable(block) && !TempBlock.isTempBlock(block)
				&& WaterManipulation.canPhysicsChange(block)) {
			if (block.getType() == Material.SNOW) {
				block.setType(Material.AIR);
				return;
			}
			if (FreezeMelt.frozenblocks.containsKey(block)) {
				FreezeMelt.thaw(block);
			} else {
				block.setType(Material.WATER);
				block.setData(full);
			}
		}
	}

	public static void evaporate(Player player, Block block) {
		if (Tools.isRegionProtectedFromBuild(player, Abilities.PhaseChange,
				block.getLocation()))
			return;
		if (Tools.isWater(block) && !TempBlock.isTempBlock(block)
				&& WaterManipulation.canPhysicsChange(block)) {
			block.setType(Material.AIR);
			block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 1);
		}
	}

}
