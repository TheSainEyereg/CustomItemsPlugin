package ru.olejka.customitems;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.logging.Logger;

import static ru.olejka.customitems.ConfigManager.getItem;

public class EventManager implements Listener {

	private static final Logger logger = CustomItems.getPluginLogger();

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
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

			var ci = getItem(item);
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
				var ci = getItem(item);
				if (ci == null) return;

				if (clickedInventory != player.getInventory() && isSharableInventory && !ci.isSharable()) {
					event.setCancelled(true);
					return;
				}
			}
		}

		// Block moving cursor item
		var cursor = event.getCursor();
		if (cursor.getType() != Material.AIR) {
			var ci = getItem(cursor);
			if (ci == null) return;

			if (clickedInventory == null || clickedInventory.getType() != player.getInventory().getType() && isSharableInventory && !ci.isSharable()) {
				event.setCancelled(true);
				return;
			}
		}

		// Block moving with shift
		var currentItem = event.getCurrentItem();
		if (currentItem != null && action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
			var ci = getItem(currentItem);
			if (ci == null) return;

			if (event.getInventory().getHolder() != player && isSharableInventory && !ci.isSharable()) {
				event.setCancelled(true);
			}
		}
	}
}
