package ru.olejka.customitems;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.logging.Logger;

public class EventManager implements Listener {

	private static final Logger logger = CustomItems.getPluginLogger();

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onInterractEvent(PlayerInteractEvent event) {
		var player = event.getPlayer();
		var hand = event.getHand();
		if (hand == null) return;

		var inventory = player.getInventory();
		var item = hand.equals(EquipmentSlot.HAND) ? inventory.getItemInMainHand() : inventory.getItemInOffHand();
		var material = item.getType();

		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			logger.info("Right click");

			if (player.hasCooldown(material)) return;

			var cd = ConfigManager.getCooldownItem(item);
			if (cd == null) return;

			player.setCooldown(material, 20 * cd.getTime());
		}
	}
}
