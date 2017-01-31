package com.ponyvillesquare.serverbridge;

import java.util.Collection;

public class Settings {
	// Control variables
	public String side;
	public boolean isMaster;
	public static boolean debug;
	public int port;
	public String ip;
	public int retryTime;
	public Collection<String> whitelistedChannels;
	private final ServerBridgePlugin plugin;

	public Settings(final ServerBridgePlugin p) {
		plugin = p;
		reload();
	}

	/** Reloads the configuration file */
	public void reload() {
		plugin.reloadConfig();
		plugin.saveConfig();

		side = plugin.getConfig().getString("side", "null");
		isMaster = side.equalsIgnoreCase("master");
		port = plugin.getConfig().getInt("port", 9998);
		ip = plugin.getConfig().getString("ip", "localhost");
		retryTime = plugin.getConfig().getInt("retryTime", 30);
		debug = plugin.getConfig().getBoolean("debug", false);
		whitelistedChannels = plugin.getConfig().getStringList("channels");
	}
}
