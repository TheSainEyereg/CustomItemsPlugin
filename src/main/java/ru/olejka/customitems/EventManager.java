package ru.olejka.customitems;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Bell;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EventManager implements Listener {

	private static final Logger logger = CustomItems.getPluginLogger();
	private static final List<Block> bellRanged = new ArrayList<>();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInterractEvent(final PlayerInteractEvent event) {
		var player = event.getPlayer();
		var hand = event.getHand();
		if (hand == null) return;

		var inventory = player.getInventory();
		var item = hand.equals(EquipmentSlot.HAND) ? inventory.getItemInMainHand() : inventory.getItemInOffHand();
		var material = item.getType();

		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (player.hasCooldown(material)) {
				event.setCancelled(true);
				return;
			};

			var ci = ConfigManager.getItem(item);
			if (ci == null) return;

			player.setCooldown(material, 20 * ci.getCooldown());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlaceToChest(final InventoryClickEvent event) {
		var action = event.getAction();
		var clickedInventory = event.getClickedInventory();

		var inventoryType = event.getInventory().getType();
		var isSharableInventory = inventoryType == InventoryType.ENDER_CHEST || inventoryType == InventoryType.SHULKER_BOX;

		var player = (Player) event.getWhoClicked();

		// Block moving from hotbar
		if (action == InventoryAction.HOTBAR_SWAP && event.getClick() == ClickType.NUMBER_KEY) {
			int hotbarButton = event.getHotbarButton();
			if (hotbarButton > -1) {
				var item = player.getInventory().getItem(hotbarButton);
				if (item == null) return;
				var ci = ConfigManager.getItem(item);
				if (ci == null) return;

				if (clickedInventory != player.getInventory() && isSharableInventory && ci.isNotSharable()) {
					event.setCancelled(true);
					return;
				}
			}
		}

		// Block moving cursor item
		var cursor = event.getCursor();
		if (cursor.getType() != Material.AIR) {
			var ci = ConfigManager.getItem(cursor);
			if (ci == null) return;

			if (clickedInventory == null || clickedInventory.getType() != player.getInventory().getType() && isSharableInventory && ci.isNotSharable()) {
				event.setCancelled(true);
				return;
			}
		}

		// Block moving with shift
		var currentItem = event.getCurrentItem();
		if (currentItem != null && action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
			var ci = ConfigManager.getItem(currentItem);
			if (ci == null) return;

			if (event.getInventory().getHolder() != player && isSharableInventory && ci.isNotSharable()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBellPlace(final BlockPlaceEvent event) {
		var block = event.getBlockPlaced();
		if (!block.getType().equals(Material.BELL)) return;

		var item = event.getItemInHand();
		var bellConfig = ConfigManager.getBell();
		if (bellConfig == null || !bellConfig.isApplicable(item)) return;

		var meta = item.getItemMeta();
		var bellEntity = (Bell) block.getState();
		var bellData = bellEntity.getPersistentDataContainer();
		var key = NamespacedKey.fromString("ci:bell-meta");
		if (key == null) return;

		bellData.set(key, PersistentDataType.STRING, meta.getAsString());
		bellEntity.update();
	}

//	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
//	public void onBellBreak(final BlockBreakEvent event) {
//		var block = event.getBlock();
//		if (!block.getType().equals(Material.BELL)) return;
//
//		var bellEntity = (Bell) block.getState();
//		var bellData = bellEntity.getPersistentDataContainer();
//		var key = NamespacedKey.fromString("ci:bell-meta");
//		if (key == null) return;
//
//		var meta = bellData.get(key, PersistentDataType.STRING);
//		if (meta == null) return;
//
//		var item = new ItemStack(Material.BELL);
//
//		event.setDropItems(false);
//		block.getDrops(item).clear();
//
//		var player = event.getPlayer();
//		var world = player.getWorld();
//		world.dropItemNaturally(block.getLocation(), item);
//	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBellRing(final PlayerInteractEvent event) {
		var action = event.getAction();
		var block = event.getClickedBlock();
		if (action != Action.RIGHT_CLICK_BLOCK || block == null || !block.getType().equals(Material.BELL)) return;

		if (bellRanged.contains(block)) {
			event.setCancelled(true);
			return;
		}

		var bellEntity = (Bell) block.getState();
		var bellData = bellEntity.getPersistentDataContainer();
		var key = NamespacedKey.fromString("ci:bell-meta");
		if (key == null) return;

		var meta = bellData.get(key, PersistentDataType.STRING);
		if (meta == null) return;

		var bellConfig = ConfigManager.getBell();
		if (bellConfig == null) return;

		//get players in radius
		var radius = bellConfig.getRadius();
		var effectTime = bellConfig.getEffectTime();
		var cooldown = bellConfig.getCooldown();

		// Add cooldown & delay removal
		bellRanged.add(block);
		new BukkitRunnable() {
			@Override
			public void run() {
				bellRanged.remove(block);
			}
		}.runTaskLaterAsynchronously(CustomItems.getPlugin(CustomItems.class), cooldown * 20L);

		// Get players near bell
		var player = event.getPlayer();
		var players = block.getLocation().getNearbyPlayers(radius).stream().filter(p -> p != player).toList();
		if (players.isEmpty()) return;

		// Create team
		var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		var invisibleTteam = scoreboard.registerNewTeam("ci-bell-" + new Date().getTime());
		invisibleTteam.color(NamedTextColor.RED);

		for (var p : players) {
			if (!p.isOnline()) continue;
			if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) invisibleTteam.addEntity(p);
			p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, effectTime * 20, 0, false, false));
		}

		// Delay team removal
		new BukkitRunnable() {
			@Override
			public void run() {
				invisibleTteam.unregister();
			}
		}.runTaskLaterAsynchronously(CustomItems.getPlugin(CustomItems.class), effectTime * 20L);
	}
}
