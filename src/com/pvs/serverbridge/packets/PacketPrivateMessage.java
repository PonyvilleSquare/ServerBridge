package com.pvs.serverbridge.packets;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;
import com.pvs.serverbridge.ServerBridgePlugin;
import com.pvs.serverbridge.commands.CommandPrivateMessage;

public class PacketPrivateMessage extends Packet {
	private final String sender;
	private final String receiver;
	private final String message;

	public PacketPrivateMessage(final String sender, final String receiver, final String command) {
		this.sender = sender;
		this.receiver = receiver;
		message = command;
	}

	/** Returns the message that is associated with this packet */
	public final String getMessage() {
		return message;
	}

	/** Returns the sender that is associated with this command */
	public final String getSender() {
		return sender;
	}

	/** Returns the receiver that is associated with this command */
	public final String getReceiver() {
		return receiver;
	}

	// //////////////////////////////////////////////////////////////
	public static class Parser extends Packet.Parser {
		@Override
		public String write(final Packet packet) {
			final PacketPrivateMessage p = (PacketPrivateMessage) packet;
			return String.format("%s %s %s", p.getSender().toString(), p.getReceiver(), p.getMessage());
		}

		@Override
		public Packet read(final String string) {
			// Validate that the packet is properly structured
			final String[] components = string.split(" ");
			if (components.length < 3)
				return null;

			// Construct the packet object
			final String sender = components[0];
			final String receiver = components[1];
			final String message = Joiner.on(" ").join(ArrayUtils.subarray(components, 2, components.length));
			return new PacketPrivateMessage(sender, receiver, message);
		}
	}

	public static class Processor extends Packet.Processor {
		@Override
		public void process(final Packet packet) {
			final PacketPrivateMessage p = (PacketPrivateMessage) packet;
			final String sender = p.getSender();
			final String receiver = p.getReceiver();
			final String message = p.getMessage();

			if (CommandPrivateMessage.sendMessage(sender, receiver, message)) {
				final Player player = Bukkit.getPlayer(receiver);
				ServerBridgePlugin.getPacketHandler().sendPacket(new PacketPrivateMessageReply(sender, player == null ? receiver : player.getName(), message, true));
			} else
				ServerBridgePlugin.getPacketHandler().sendPacket(new PacketPrivateMessageReply(sender, receiver, message, false));
		}
	}
}
