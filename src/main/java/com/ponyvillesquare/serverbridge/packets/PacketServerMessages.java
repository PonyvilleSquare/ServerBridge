package com.ponyvillesquare.serverbridge.packets;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PacketServerMessages extends Packet {

	private final String message;

	public PacketServerMessages(final String message) {
		this.message = message;
	}

	public static class Parser extends Packet.Parser {

		@Override
		public String write(final Packet packet) {
			final PacketServerMessages p = (PacketServerMessages) packet;
			return p.message;
		}

		@Override
		public Packet read(final String string) {
			return new PacketServerMessages(string);
		}
	}

	public static class Processor extends Packet.Processor {

		@Override
		public void process(final Packet packet) {
			final PacketServerMessages p = (PacketServerMessages) packet;
			for (final Player ply : Bukkit.getOnlinePlayers())
				ply.sendMessage(p.message);
		}

	}
}
