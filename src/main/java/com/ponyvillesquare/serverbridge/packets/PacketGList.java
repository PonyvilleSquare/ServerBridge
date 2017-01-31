package com.ponyvillesquare.serverbridge.packets;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.ponyvillesquare.serverbridge.ServerBridgePlugin;

public class PacketGList extends Packet {
	private final UUID sender;

	public PacketGList(final Player sender) {
		this(sender.getUniqueId());
	}

	public PacketGList(final UUID sender) {
		this.sender = sender;
	}

	/** Returns the sender that is associated with this packet */
	public final UUID getSender() {
		return sender;
	}

	// //////////////////////////////////////////////////////////////
	public static class Parser extends Packet.Parser {
		@Override
		public String write(final Packet packet) {
			final PacketGList p = (PacketGList) packet;
			return String.format("%s", p.getSender().toString());
		}

		@Override
		public Packet read(final String string) {
			final UUID sender = UUID.fromString(string);
			return new PacketGList(sender);
		}
	}

	public static class Processor extends Packet.Processor {
		@Override
		public void process(final Packet packet) {
			final PacketGList p = (PacketGList) packet;

			int i = 0;
			final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
			final String[] playerNames = new String[players.size()];
			for (final Player player : players)
				playerNames[i++] = player.getName();

			ServerBridgePlugin.getPacketHandler().sendPacket(new PacketGListReply(p.getSender(), playerNames));
		}
	}
}
