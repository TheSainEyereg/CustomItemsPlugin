package ru.olejka.customitems;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

import static ru.olejka.customitems.ConfigManager.parseConfig;

public final class CustomItems extends JavaPlugin {
	private static Logger logger;

	@Override
	public void onEnable() {
		// Plugin startup logic
		parseConfig(getConfig());
		saveDefaultConfig();

		logger = getLogger();

		Bukkit.getPluginManager().registerEvents(new EventManager(), this);
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}
	static Logger getPluginLogger() {
		return logger;
	}
}
