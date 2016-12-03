package com.pvs.serverbridge.packets;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.base.Joiner;
import com.pvs.serverbridge.commands.CommandPrivateMessage;

public class PacketPrivateMessageReply extends Packet {
	private final String sender;
	private final String receiver;
	private final String message;
	private final boolean result;

	public PacketPrivateMessageReply(final String sender, final String receiver, final String message, final boolean result) {
		this.sender = sender;
		this.receiver = receiver;
		this.message = message;
		this.result = result;
	}

	/** Returns the sender that is associated with this packet */
	public final String getSender() {
		return sender;
	}

	/** Returns the receiver that is associated with this packet */
	public final String getReceiver() {
		return receiver;
	}

	/** Returns the message that is associated with this packet */
	public final String getMessage() {
		return message;
	}

	/** Returns the result of the command that was executed */
	public final boolean getResult() {
		return result;
	}

	// //////////////////////////////////////////////////////////////
	public static class Parser extends Packet.Parser {
		@Override
		public String write(final Packet packet) {
			final PacketPrivateMessageReply p = (PacketPrivateMessageReply) packet;
			return p.getSender() + " " + p.getReceiver() + " " + Boolean.toString(p.getResult()) + " " + p.getMessage();
		}

		@Override
		public Packet read(final String string) {
			// Validate that the packet is properly structured
			final String[] components = string.split(" ");
			if (components.length < 4)
				return null;

			// Construct the packet object
			final String sender = components[0];
			final String receiver = components[1];
			final boolean result = Boolean.parseBoolean(components[2]);
			final String message = Joiner.on(" ").join(ArrayUtils.subarray(components, 3, components.length));
			return new PacketPrivateMessageReply(sender, receiver, message, result);
		}
	}

	public static class Processor extends Packet.Processor {
		@Override
		public void process(final Packet packet) {
			final PacketPrivateMessageReply p = (PacketPrivateMessageReply) packet;
			final CommandSender sender = p.getSender().equalsIgnoreCase("console") ? Bukkit.getConsoleSender() : Bukkit.getPlayer(p.getSender());
			if (sender != null)
				if (p.getResult()) {
					if (!CommandPrivateMessage.verifyMessage(sender, p.getReceiver(), p.getMessage(), true))
						sender.sendMessage(ChatColor.RED + "Could not verify that the message was delivered; user confirmed to be online");
				} else
					sender.sendMessage(ChatColor.RED + "Failed to deliver the message; recipent not online");
		}
	}
}
