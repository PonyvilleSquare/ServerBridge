package com.pvs.serverbridge;

import org.bukkit.plugin.java.JavaPlugin;

import com.pvs.serverbridge.commands.CommandGList;
import com.pvs.serverbridge.commands.CommandPrivateMessage;
import com.pvs.serverbridge.commands.CommandReload;
import com.pvs.serverbridge.commands.CommandRunCommand;
import com.pvs.serverbridge.communication.PacketHandler;
import com.pvs.serverbridge.communication.PacketHandlerMaster;
import com.pvs.serverbridge.communication.PacketHandlerSlave;
import com.pvs.serverbridge.packets.PacketCommand;
import com.pvs.serverbridge.packets.PacketCommandReply;
import com.pvs.serverbridge.packets.PacketGList;
import com.pvs.serverbridge.packets.PacketGListReply;
import com.pvs.serverbridge.packets.PacketKeepAlive;
import com.pvs.serverbridge.packets.PacketMessage;
import com.pvs.serverbridge.packets.PacketPrivateMessage;
import com.pvs.serverbridge.packets.PacketPrivateMessageReply;

public class ServerBridgePlugin extends JavaPlugin
{
	private static ServerBridgePlugin instance;
	private static Settings settings;
	private static PacketHandler packetHandler;

	private static final org.apache.logging.log4j.Logger nastyLoggerLine = org.apache.logging.log4j.LogManager.getLogger();

	@Override
	public void onEnable()
	{
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

		// Set up the task that ticks the packet handler, and register the listener
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				packetHandler.onTick();
			}
		}, 0, 20);
		getServer().getPluginManager().registerEvents(new ServerBridgeListener(), this);
	}

	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		packetHandler.close();
	}

	// //////////////////////////////////////////////////

	/** Returns the plugin instance */
	public final static ServerBridgePlugin getInstance()
	{
		return instance;
	}

	/** Returns the packet handler instance */
	public final static PacketHandler getPacketHandler()
	{
		return packetHandler;
	}

	/** Returns the settings */
	public final static Settings getSettings()
	{
		return settings;
	}

	/** Returns the raw plugin logger instance */
	public final static org.apache.logging.log4j.Logger getRawLogger()
	{
		return nastyLoggerLine;
	}
}
