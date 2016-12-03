package com.pvs.serverbridge.packets;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PacketJoinServer extends Packet {

	private final String message;

	public PacketJoinServer(final String message) {
		this.message = message;
	}

	public static class Parser extends Packet.Parser {

		@Override
		public String write(final Packet packet) {
			final PacketJoinServer p = (PacketJoinServer) packet;
			return p.message;
		}

		@Override
		public Packet read(final String string) {
			return new PacketJoinServer(string);
		}
	}

	public static class Processor extends Packet.Processor {

		@Override
		public void process(final Packet packet) {
			final PacketJoinServer p = (PacketJoinServer) packet;
			for (final Player ply : Bukkit.getOnlinePlayers())
				ply.sendMessage(p.message);
		}

	}
}
