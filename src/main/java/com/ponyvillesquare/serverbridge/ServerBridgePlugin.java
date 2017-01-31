package com.ponyvillesquare.serverbridge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import com.ponyvillesquare.serverbridge.commands.CommandGList;
import com.ponyvillesquare.serverbridge.commands.CommandPrivateMessage;
import com.ponyvillesquare.serverbridge.commands.CommandReload;
import com.ponyvillesquare.serverbridge.commands.CommandRunCommand;
import com.ponyvillesquare.serverbridge.communication.PacketHandler;
import com.ponyvillesquare.serverbridge.communication.PacketHandlerMaster;
import com.ponyvillesquare.serverbridge.communication.PacketHandlerSlave;
import com.ponyvillesquare.serverbridge.packets.PacketCommand;
import com.ponyvillesquare.serverbridge.packets.PacketCommandReply;
import com.ponyvillesquare.serverbridge.packets.PacketGList;
import com.ponyvillesquare.serverbridge.packets.PacketGListReply;
import com.ponyvillesquare.serverbridge.packets.PacketServerMessages;
import com.ponyvillesquare.serverbridge.packets.PacketKeepAlive;
import com.ponyvillesquare.serverbridge.packets.PacketMessage;
import com.ponyvillesquare.serverbridge.packets.PacketPrivateMessage;
import com.ponyvillesquare.serverbridge.packets.PacketPrivateMessageReply;

public class ServerBridgePlugin extends JavaPlugin {
	private static ServerBridgePlugin instance;
	private static Settings settings;
	private static PacketHandler packetHandler;

	private static final Logger nastyLoggerLine = LogManager.getLogger();

	@Override
	public void onEnable() {
		Log.initialize(this);
		instance = this;
		settings = new Settings(this);
		getCommand("bridgecommand").setExecutor(new CommandRunCommand());
		getCommand("glist").setExecutor(new CommandGList());
		getCommand("bridgereload").setExecutor(new CommandReload());
		getCommand("msb").setExecutor(new CommandPrivateMessage());

		// Initialize the packet handler, depending on which side it is on
		if (settings.isMaster)
			packetHandler = new PacketHandlerMaster();
		else
			packetHandler = new PacketHandlerSlave();
		packetHandler.open(settings.ip, settings.port);

		// Register all the types of packages
		packetHandler.registerPacket(PacketMessage.class, new PacketMessage.Parser(), new PacketMessage.Processor(), "CHAT");
		packetHandler.registerPacket(PacketCommand.class, new PacketCommand.Parser(), new PacketCommand.Processor(), "CMD");
		packetHandler.registerPacket(PacketCommandReply.class, new PacketCommandReply.Parser(), new PacketCommandReply.Processor(), "CMDR");
		packetHandler.registerPacket(PacketKeepAlive.class, new PacketKeepAlive.Parser(), new PacketKeepAlive.Processor(), "KA");
		packetHandler.registerPacket(PacketGList.class, new PacketGList.Parser(), new PacketGList.Processor(), "GL");
		packetHandler.registerPacket(PacketGListReply.class, new PacketGListReply.Parser(), new PacketGListReply.Processor(), "GLR");
		packetHandler.registerPacket(PacketPrivateMessage.class, new PacketPrivateMessage.Parser(), new PacketPrivateMessage.Processor(), "PM");
		packetHandler.registerPacket(PacketPrivateMessageReply.class, new PacketPrivateMessageReply.Parser(), new PacketPrivateMessageReply.Processor(), "PMR");
		packetHandler.registerPacket(PacketServerMessages.class, new PacketServerMessages.Parser(), new PacketServerMessages.Processor(), "JOIN");

		// Set up the task that ticks the packet handler, and register the listener
		getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> packetHandler.onTick(), 0, 20);
		getServer().getPluginManager().registerEvents(new ServerBridgeListener(), this);
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		packetHandler.close();
	}

	// //////////////////////////////////////////////////

	/** Returns the plugin instance */
	public final static ServerBridgePlugin getInstance() {
		return instance;
	}

	/** Returns the packet handler instance */
	public final static PacketHandler getPacketHandler() {
		return packetHandler;
	}

	/** Returns the settings */
	public final static Settings getSettings() {
		return settings;
	}

	/** Returns the raw plugin logger instance */
	public final static org.apache.logging.log4j.Logger getRawLogger() {
		return nastyLoggerLine;
	}
}
