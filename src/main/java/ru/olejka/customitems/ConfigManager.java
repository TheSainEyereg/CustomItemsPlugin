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

	public boolean isNotSharable() {
		return !sharable;
	}
}

class BellDefinition {
	private final int radius;
	private final int effectTime;
	private final int cooldown;
	private final String loreIdent;

	public BellDefinition(int radius, int effectTime, int cooldown, String loreIdent) {
		this.radius = radius;
		this.effectTime = effectTime;
		this.cooldown = cooldown;
		this.loreIdent = loreIdent;
	}

	public boolean isApplicable(ItemStack item) {
		var material = item.getType();
		if (material != Material.BELL) return false;

		var meta = item.getItemMeta();
		if (meta == null) return false;

		var lore = meta.getLore();
		return lore != null && lore.toString().contains(loreIdent);
	}

	public int getRadius() {
		return radius;
	}

	public int getEffectTime() {
		return effectTime;
	}

	public int getCooldown() {
		return cooldown;
	}

	public String getLoreIdent() {
		return loreIdent;
	}
}

public class ConfigManager {
	private static final List<ItemDefinition> items = new ArrayList<>();
	private static BellDefinition bell = null;

	public static void parseConfig(FileConfiguration config) {
		for(var ci : config.getMapList("items")) {
			var ciMaterial = Material.getMaterial((String) ci.get("type"));
			var ciCooldown = ci.get("cooldown");
			var ciEiid = ci.get("eiid");
			var ciSharable = ci.get("sharable");

			if (ciMaterial == null || ciEiid == null || ciCooldown == null) continue;

			items.add(new ItemDefinition(ciMaterial, (int) ciCooldown, (String) ciEiid, ciSharable == null || (boolean) ciSharable));
		}

		var bellConfig = config.getConfigurationSection("bell");
		if (bellConfig != null) {
			var bellRadius = bellConfig.get("radius");
			var bellEffectTime = bellConfig.get("effect_time");
			var bellCooldown = bellConfig.get("cooldown");
			var bellLoreIdent = bellConfig.get("lore_ident");

			if (bellRadius == null || bellEffectTime == null || bellCooldown == null || bellLoreIdent == null) return;

			bell = new BellDefinition((int) bellRadius, (int) bellEffectTime, (int) bellCooldown, (String) bellLoreIdent);
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

	public static BellDefinition getBell() {
		return bell;
	}
}
