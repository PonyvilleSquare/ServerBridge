package com.pvs.serverbridge.packets;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;

import com.dthielke.Herochat;
import com.google.common.base.Joiner;

public class PacketMessage extends Packet {
	private final String message;

	public PacketMessage(final String message) {
		this.message = message;
	}

	/** Returns the message that is associated with this packet */
	public final String getMessage() {
		return message;
	}

	// //////////////////////////////////////////////////////////////
	public static class Parser extends Packet.Parser {
		@Override
		public String write(final Packet packet) {
			final PacketMessage p = (PacketMessage) packet;
			return p.getMessage();
		}

		@Override
		public Packet read(final String string) {
			return new PacketMessage(string);
		}
	}

	public static class Processor extends Packet.Processor {
		@Override
		public void process(final Packet packet) {
			final PacketMessage p = (PacketMessage) packet;
			final String[] parts = p.getMessage().split(" ");
			final String message = Joiner.on(' ').join(ArrayUtils.subarray(parts, 1, parts.length));
			Herochat.getChannelManager().getChannel(parts[0]).announce(ChatColor.translateAlternateColorCodes('&', message.trim()));
		}
	}
}
