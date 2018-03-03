package com.tistory.hornslied.evitaonline.mine.block;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.RenameTownEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.tistory.hornslied.evitaonline.core.EvitaCoreMain;
import com.tistory.hornslied.evitaonline.db.DB;
import com.tistory.hornslied.evitaonline.events.MineBlockBuildEvent;
import com.tistory.hornslied.evitaonline.mine.EvitaMineMain;
import com.tistory.hornslied.evitaonline.utils.Resources;

public class MineBlockManager implements Listener {
	private volatile static MineBlockManager instance;

	public static ItemStack MINEBLOCK;

	static {
		MINEBLOCK = new ItemStack(Material.STONE);

		ItemMeta meta = MINEBLOCK.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + ChatColor.BOLD.toString() + "광산 블록");
		ArrayList<String> lore = new ArrayList<>();
		lore.add(ChatColor.WHITE + ChatColor.ITALIC.toString() + "설치하면 부술때마다 광물을 생성합니다.");
		lore.add(ChatColor.WHITE + ChatColor.ITALIC.toString() + "금 곡괭이로 회수 가능");
		meta.setLore(lore);

		MINEBLOCK.setItemMeta(meta);
	}

	private MineBlockManager() {
		loadMineBlocks();

		Bukkit.getPluginManager().registerEvents(this, EvitaMineMain.getInstance());
	}

	private void loadMineBlocks() {
		DB db = EvitaCoreMain.getInstance().getDB();

		ResultSet rs = db.selectUpdatable("SELECT * FROM mineblocks;");
		try {
			while (rs.next()) {
				try {
					Block block = Bukkit.getWorld(rs.getString("world")).getBlockAt(rs.getInt("x"), rs.getInt("y"),
							rs.getInt("z"));
					String townName = rs.getString("town");

					if (townName == null) {
						block.setMetadata("mine", new FixedMetadataValue(EvitaMineMain.getInstance(), null));
					} else {
						try {
							Town town = TownyUniverse.getDataSource().getTown(townName);

							block.setMetadata("mine", new FixedMetadataValue(EvitaMineMain.getInstance(), town));
						} catch (NotRegisteredException e) {
							rs.deleteRow();
						}
					}

					if (block.getType() == Material.AIR)
						regenMine(block);
				} catch (NullPointerException e) {
					rs.deleteRow();
				}
			}

			rs.close();
		} catch (SQLException e) {
			return;
		}
	}

	public static MineBlockManager getInstance() {
		if (instance == null) {
			synchronized (MineBlockManager.class) {
				if (instance == null) {
					instance = new MineBlockManager();
				}
			}
		}

		return instance;
	}

	public void addMineBlock(Town town, Block block) {
		block.setMetadata("mine", new FixedMetadataValue(EvitaMineMain.getInstance(), town));

		if (town == null) {
			EvitaCoreMain.getInstance().getDB().query("INSERT INTO mineblocks (x, y, z, world) VALUES (" + block.getX()
					+ ", " + block.getY() + ", " + block.getZ() + ", '" + block.getWorld().getName() + "');");
		} else {
			EvitaCoreMain.getInstance().getDB()
					.query("INSERT INTO mineblocks (x, y, z, world, town) VALUES (" + block.getX() + ", " + block.getY()
							+ ", " + block.getZ() + ", '" + block.getWorld().getName() + "', '" + town.getName()
							+ "');");
		}

	}

	public void removeMineBlock(Block block) {
		block.removeMetadata("mine", EvitaMineMain.getInstance());
		EvitaCoreMain.getInstance().getDB().query("DELETE FROM mineblocks WHERE x=" + block.getX() + " AND y="
				+ block.getY() + " AND z=" + block.getZ() + " AND world = '" + block.getWorld().getName() + "';");
	}

	public void regenMine(Block block) {
		int i = new Random().nextInt(100);
		new BukkitRunnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				if (!block.getChunk().isLoaded())
					block.getChunk().load();
				if (0 <= i && i <= 49) {
					block.setType(Material.STONE);
				} else if (i <= 67) {
					block.setType(Material.COAL_ORE);
				} else if (i <= 85) {
					block.setType(Material.IRON_ORE);
				} else if (i <= 89) {
					block.setType(Material.REDSTONE_ORE);
				} else if (i <= 92) {
					block.setType(Material.GLOWSTONE);
				} else if (i <= 94) {
					block.setType(Material.GOLD_ORE);
				} else if (i <= 96) {
					block.setType(Material.LAPIS_ORE);
				} else if (i <= 97) {
					block.setType(Material.PRISMARINE);
					block.setData((byte) 2);
				} else if (i <= 98) {
					block.setType(Material.EMERALD_ORE);
				} else if (i <= 99) {
					block.setType(Material.DIAMOND_ORE);
				}
			}
		}.runTaskLater(EvitaMineMain.getInstance(), 100);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onMinePlace_Low(BlockPlaceEvent e) {
		if (e.getBlockPlaced().hasMetadata("mine")) {
			e.getPlayer().sendMessage(Resources.tagServer + ChatColor.RED + "광산 지역에는 설치할수 없습니다. 광산은 금 곡괭이로 회수할수 있습니다.");
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMinePlace(BlockPlaceEvent e) {
		ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
		Block block = e.getBlockPlaced();
		if (item.getType().equals(Material.STONE) && item.getItemMeta().getDisplayName() != null
				&& item.getItemMeta().getDisplayName().equals(MINEBLOCK.getItemMeta().getDisplayName())) {
			TownBlock townBlock = TownyUniverse.getTownBlock(block.getLocation());

			if (townBlock == null || !townBlock.hasTown()) {
				addMineBlock(null, block);
				e.getPlayer().sendMessage(Resources.tagServer + ChatColor.YELLOW + "광산 블록을 설치했습니다. (어드민)");
				Bukkit.getPluginManager().callEvent(new MineBlockBuildEvent(null, e.getPlayer(), block.getLocation()));
			} else {
				try {
					addMineBlock(townBlock.getTown(), block);
					e.getPlayer().sendMessage(Resources.tagServer + ChatColor.YELLOW + "광산 블록을 설치했습니다.");
					Bukkit.getPluginManager().callEvent(
							new MineBlockBuildEvent(townBlock.getTown(), e.getPlayer(), block.getLocation()));
				} catch (NotRegisteredException e1) {
					return;
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMineBreak_Highest(BlockBreakEvent e) {
		Block block = e.getBlock();
		Player player = e.getPlayer();

		if (block.hasMetadata("mine")) {
			if (block.getMetadata("mine").get(0).value() == null) {
				if (player.getInventory().getItemInMainHand().getType().equals(Material.GOLD_PICKAXE)) {
					if (player.hasPermission("evita.admin")) {
						removeMineBlock(block);
						player.sendMessage(Resources.tagServer + ChatColor.YELLOW + "광산 블록을 회수했습니다.");
						block.getWorld().dropItemNaturally(block.getLocation(), MINEBLOCK);
						Bukkit.getPluginManager().callEvent(new MineBlockBuildEvent(null, player, block.getLocation()));
					} else {
						player.sendMessage(Resources.tagServer + ChatColor.RED + "회수할수 없는 광산 블록입니다.");
						e.setCancelled(true);
					}
				} else {
					e.setCancelled(false);

					if (block.getType() == Material.PRISMARINE && block.getData() == (byte) 2) {
						player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
					} else {
						for (ItemStack is : block.getDrops()) {
							player.getInventory().addItem(is);
						}
					}
					e.setDropItems(false);
				}

			} else if (player.getInventory().getItemInMainHand().getType().equals(Material.GOLD_PICKAXE)
					&& !e.isCancelled()) {
				Town town = (Town) block.getMetadata("mine").get(0).value();
				removeMineBlock(block);
				block.getWorld().dropItemNaturally(block.getLocation(), MINEBLOCK);
				e.getPlayer().sendMessage(Resources.tagServer + ChatColor.YELLOW + "광산 블록을 회수했습니다.");
				Bukkit.getPluginManager().callEvent(new MineBlockBuildEvent(town, player, block.getLocation()));
			} else if (!e.isCancelled() && block.getType() == Material.PRISMARINE && block.getData() == (byte) 2) {
				block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.ENDER_PEARL));
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMineBreak(BlockBreakEvent e) {
		if (e.getBlock().hasMetadata("mine"))
			regenMine(e.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onTownRename(RenameTownEvent e) {
		EvitaCoreMain.getInstance().getDB()
				.query("UPDATE mineblocks SET town = '" + e.getNewName() + "' WHERE town = '" + e.getOldName() + "';");
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onTownDelete(DeleteTownEvent e) {
		DB db = EvitaCoreMain.getInstance().getDB();

		ResultSet rs = db.select("SELECT * FROM mineblocks WHERE town = '" + e.getTownName() + "';");

		try {
			while (rs.next()) {
				Block block = Bukkit.getWorld(rs.getString("world")).getBlockAt(rs.getInt("x"), rs.getInt("y"),
						rs.getInt("z"));
				block.removeMetadata("mine", EvitaMineMain.getInstance());
			}

			rs.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		db.query("DELETE FROM mineblocks WHERE town = '" + e.getTownName() + "';");
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPistonExtend(BlockPistonExtendEvent e) {
		for (Block block : e.getBlocks()) {
			if (block.hasMetadata("mine")) {
				e.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPistonRetract(BlockPistonRetractEvent e) {
		for (Block block : e.getBlocks()) {
			if (block.hasMetadata("mine")) {
				e.setCancelled(true);
				return;
			}
		}
	}
}
