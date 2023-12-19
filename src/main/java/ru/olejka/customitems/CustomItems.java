package ru.olejka.customitems;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class CustomItems extends JavaPlugin {
	private static Logger logger;

	@Override
	public void onEnable() {
		// Plugin startup logic
		ConfigManager.parseConfig(getConfig());
		saveDefaultConfig();

		logger = getLogger();
		Bukkit.getPluginManager().registerEvents(new EventManager(), this);

		// Remove all teams that starts with "ci"
		var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		for (var team : scoreboard.getTeams()) {
			if (team.getName().startsWith("ci")) {
				team.unregister();
			}
		}
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}
	static Logger getPluginLogger() {
		return logger;
	}
}
