package com.pvs.serverbridge.packets;

import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;
import com.ponyvillesquare.serverbridge.Log;
import com.ponyvillesquare.serverbridge.ServerBridgePlugin;

public class PacketCommand extends Packet {
	private final UUID sender;
	private final String command;

	public PacketCommand(final Player sender, final String command) {
		this(sender.getUniqueId(), command);
	}

	public PacketCommand(final UUID sender, final String command) {
		this.sender = sender;
		this.command = command;
	}

	/** Returns the command that is associated with this packet */
	public final String getCommand() {
		return command;
	}

	/** Returns the sender that is associated with this command */
	public final UUID getSender() {
		return sender;
	}

	// //////////////////////////////////////////////////////////////
	public static class Parser extends Packet.Parser {
		@Override
		public String write(final Packet packet) {
			final PacketCommand p = (PacketCommand) packet;
			return String.format("%s %s", p.getSender() == null ? "null" : p.getSender().toString(), p.getCommand());
		}

		@Override
		public Packet read(final String string) {
			// Validate that the packet is properly structured
			final String[] components = string.split(" ");
			if (components.length < 2)
				return null;

			// Construct the packet object
			final UUID sender = components[0].equalsIgnoreCase("null") ? null : UUID.fromString(components[0]);
			final String message = Joiner.on(" ").join(ArrayUtils.subarray(components, 1, components.length));
			return new PacketCommand(sender, message);
		}
	}

	public static class Processor extends Packet.Processor {
		@Override
		public void process(final Packet packet) {
			final PacketCommand p = (PacketCommand) packet;
			final String sender = p.getSender() == null ? "console" : Bukkit.getOfflinePlayer(p.getSender()).getName();
			Log.log("Attempting to execute command " + p.getCommand() + " that was sent by " + sender);
			if (Bukkit.dispatchCommand(Bukkit.getConsoleSender(), p.getCommand()))
				ServerBridgePlugin.getPacketHandler().sendPacket(new PacketCommandReply(p.getSender(), true));
			else
				ServerBridgePlugin.getPacketHandler().sendPacket(new PacketCommandReply(p.getSender(), false));
		}
	}
}
