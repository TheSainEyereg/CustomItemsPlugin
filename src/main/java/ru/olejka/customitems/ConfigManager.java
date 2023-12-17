package ru.olejka.customitems;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

class CDDefinition {
	private final Material material;
	private final int time;
	private final String eiid;

	public CDDefinition(Material material, int time, String eiid) {
		this.material = material;
		this.time = time;
		this.eiid = eiid;
	}

	public Material getMaterial() {
		return material;
	}

	public int getTime() {
		return time;
	}

	public String getEiid() {
		return eiid;
	}
}

public class ConfigManager {
	private static final List<CDDefinition> cooldowns = new ArrayList<>();

	public static void parseConfig(FileConfiguration config) {
		for(var cd : config.getMapList("cooldowns")) {
			var cdMaterial = Material.getMaterial((String) cd.get("type"));
			var cdTime = cd.get("time");
			var cdEiid = cd.get("eiid");

			if (cdMaterial == null || cdEiid == null || cdTime == null) continue;

			cooldowns.add(new CDDefinition(cdMaterial, (int) cdTime, (String) cdEiid));
		}
	}

	public static CDDefinition getCooldownItem(ItemStack item) {
		var material = item.getType();

		var meta = item.getItemMeta();
		if (meta == null) return null;

		var eiid = meta.getPersistentDataContainer().get(NamespacedKey.fromString("executableitems:ei-id"), PersistentDataType.STRING);
		if (eiid == null) return null;

		for(var cd : cooldowns) {
			if (cd.getMaterial().equals(material) && cd.getEiid().equals(eiid)) return cd;
		}
		return null;
	}

	public static List<CDDefinition> getCooldowns() {
		return cooldowns;
	}
}
