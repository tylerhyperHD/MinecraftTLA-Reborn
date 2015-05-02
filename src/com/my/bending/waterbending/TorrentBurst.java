package com.my.bending.waterbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.my.bending.tools.Abilities;
import com.my.bending.tools.TempBlock;
import com.my.bending.tools.Tools;

public class TorrentBurst {

	public static ConcurrentHashMap<Integer, TorrentBurst> instances = new ConcurrentHashMap<Integer, TorrentBurst>();

	private static int ID = Integer.MIN_VALUE;
	private static double defaultmaxradius = 15;
	private static double dr = 0.5;
	private static double defaultfactor = 1.5;
	private static long interval = Torrent.interval;

	private static final byte full = 0x0;
	// private static final Vector reference = new Vector(1, 0, 0);

	private Player player;
	private int id;
	private long time;
	private Location origin;
	private double radius = dr;
	private double maxradius = defaultmaxradius;
	private double factor = defaultfactor;
	private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Double>> heights = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Double>>();
	private ArrayList<TempBlock> blocks = new ArrayList<TempBlock>();
	private ArrayList<Entity> affectedentities = new ArrayList<Entity>();

	public TorrentBurst(Player player) {
		this(player, player.getEyeLocation(), dr);
	}

	public TorrentBurst(Player player, Location location) {
		this(player, location, dr);
	}

	public TorrentBurst(Player player, double radius) {
		this(player, player.getEyeLocation(), radius);
	}

	public TorrentBurst(Player player, Location location, double radius) {
		this.player = player;
		World world = player.getWorld();
		origin = location.clone();
		time = System.currentTimeMillis();
		id = ID++;
		factor = Tools.waterbendingNightAugment(factor, world);
		maxradius = Tools.waterbendingNightAugment(maxradius, world);
		this.radius = radius;
		if (ID >= Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
		initializeHeightsMap();
		instances.put(id, this);
	}

	private void initializeHeightsMap() {
		for (int i = -1; i <= 1; i++) {
			ConcurrentHashMap<Integer, Double> angles = new ConcurrentHashMap<Integer, Double>();
			double dtheta = Math.toDegrees(1 / (maxradius + 2));
			int j = 0;
			for (double theta = 0; theta < 360; theta += dtheta) {
				angles.put(j, theta);
				j++;
			}
			heights.put(i, angles);
		}
	}

	private void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}

		if (!Tools.canBend(player, Abilities.Torrent)) {
			remove();
			return;
		}

		if (System.currentTimeMillis() > time + interval) {
			if (radius < maxradius) {
				radius += dr;
			} else {
				remove();
				returnWater();
				return;
			}

			formBurst();

			time = System.currentTimeMillis();

		}
	}

	private void formBurst() {
		for (TempBlock tempBlock : blocks) {
			tempBlock.revertBlock();
		}

		blocks.clear();

		affectedentities.clear();

		ArrayList<Entity> indexlist = new ArrayList<Entity>();
		indexlist.addAll(Tools.getEntitiesAroundPoint(origin, radius + 2));

		ArrayList<Block> torrentblocks = new ArrayList<Block>();

		if (indexlist.contains(player))
			indexlist.remove(player);

		for (int id : heights.keySet()) {
			ConcurrentHashMap<Integer, Double> angles = heights.get(id);
			for (int index : angles.keySet()) {
				double angle = angles.get(index);
				double theta = Math.toRadians(angle);
				double dx = Math.cos(theta) * radius;
				double dy = id;
				double dz = Math.sin(theta) * radius;
				Location location = origin.clone().add(dx, dy, dz);
				Block block = location.getBlock();
				if (torrentblocks.contains(block))
					continue;
				if (Tools.isTransparentToEarthbending(player,
						Abilities.Torrent, block)) {
					TempBlock tempBlock = new TempBlock(block, Material.WATER,
							full);
					blocks.add(tempBlock);
					torrentblocks.add(block);
				} else {
					angles.remove(index);
					continue;
				}
				for (Entity entity : indexlist) {
					if (!affectedentities.contains(entity)) {
						if (entity.getLocation().distance(location) <= 2) {
							affectedentities.add(entity);
							affect(entity);
						}
					}
				}
			}
			if (angles.isEmpty())
				heights.remove(id);
		}
		if (heights.isEmpty())
			remove();
	}

	private void affect(Entity entity) {
		Vector direction = Tools.getDirection(origin, entity.getLocation());
		direction.setY(0);
		direction.normalize();
		entity.setVelocity(entity.getVelocity().clone()
				.add(direction.multiply(factor)));
	}

	private void remove() {
		for (TempBlock block : blocks) {
			block.revertBlock();
		}
		instances.remove(id);
	}

	private void returnWater() {
		Location location = new Location(origin.getWorld(), origin.getX()
				+ radius, origin.getY(), origin.getZ());
		if (!location.getWorld().equals(player.getWorld()))
			return;
		if (location.distance(player.getLocation()) > maxradius + 5)
			return;
		new WaterReturn(player, location.getBlock());
	}

	public static void progressAll() {
		for (int id : instances.keySet())
			instances.get(id).progress();
	}

	public static void removeAll() {
		for (int id : instances.keySet())
			instances.get(id).remove();
	}

}
