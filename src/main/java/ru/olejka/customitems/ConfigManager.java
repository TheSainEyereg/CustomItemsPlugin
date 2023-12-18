package ru.olejka.customitems;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

class ItemDefinition {
	private final Material material;
	private final int cooldown;
	private final String eiid;

	private final boolean sharable;

	public ItemDefinition(Material material, int cooldown, String eiid, boolean sharable) {
		this.material = material;
		this.cooldown = cooldown;
		this.eiid = eiid;
		this.sharable = sharable;
	}

	public Material getMaterial() {
		return material;
	}

	public int getCooldown() {
		return cooldown;
	}

	public String getEiid() {
		return eiid;
	}

	public boolean isSharable() {
		return sharable;
	}
}

public class ConfigManager {
	private static final List<ItemDefinition> items = new ArrayList<>();

	public static void parseConfig(FileConfiguration config) {
		for(var ci : config.getMapList("items")) {
			var ciMaterial = Material.getMaterial((String) ci.get("type"));
			var ciCooldown = ci.get("cooldown");
			var ciEiid = ci.get("eiid");
			var ciSharable = ci.get("sharable");

			if (ciMaterial == null || ciEiid == null || ciCooldown == null) continue;

			items.add(new ItemDefinition(ciMaterial, (int) ciCooldown, (String) ciEiid, ciSharable == null || (boolean) ciSharable));
		}
	}

	public static ItemDefinition getItem(ItemStack item) {
		var material = item.getType();

		var meta = item.getItemMeta();
		if (meta == null) return null;

		var eiid = meta.getPersistentDataContainer().get(NamespacedKey.fromString("executableitems:ei-id"), PersistentDataType.STRING);
		if (eiid == null) return null;

		for(var ci : items) {
			if (ci.getMaterial().equals(material) && ci.getEiid().equals(eiid)) return ci;
		}
		return null;
	}
}
