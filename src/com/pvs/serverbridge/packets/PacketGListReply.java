package com.pvs.serverbridge.packets;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;

public class PacketGListReply extends Packet
{
	private final UUID sender;
	private final String[] players;

	public PacketGListReply(final Player sender, final String[] players)
	{
		this(sender.getUniqueId(), players);
	}

	public PacketGListReply(final UUID sender, final String[] players)
	{
		this.sender = sender;
		this.players = players;
	}

	/** Returns the sender that is associated with this packet */
	public final UUID getSender()
	{
		return sender;
	}

	/** Returns the players that were online on the end the packet originates from */
	public final String[] getPlayers()
	{
		return players;
	}

	// //////////////////////////////////////////////////////////////
	public static class Parser extends Packet.Parser
	{
		@Override
		public String write(final Packet packet)
		{
			final PacketGListReply p = (PacketGListReply) packet;
			return String.format("%s %s", p.getSender().toString(), Joiner.on(" ").join(p.getPlayers()));
		}

		@Override
		public Packet read(final String string)
		{
			// Validate that the packet is properly structured
			final String[] components = string.split(" ");
			if (components.length < 1)
				return null;

			// Construct the packet object
			final UUID sender = UUID.fromString(components[0]);
			final String[] players = new String[components.length - 1];
			for (int i = 0; i < players.length; i++)
				players[i] = components[i + 1];
			return new PacketGListReply(sender, players);
		}
	}

	public static class Processor extends Packet.Processor
	{
		@Override
		public void process(final Packet packet)
		{
			final PacketGListReply p = (PacketGListReply) packet;
			final Player sender = Bukkit.getPlayer(p.getSender());
			if (sender == null)
				return;

			List<String> playerNames = new LinkedList<String>();
			for (Player player : Bukkit.getOnlinePlayers())
				playerNames.add(player.getName());
			for (String name : p.getPlayers())
				playerNames.add(name);
			playerNames = Collections.sort(playerNames);
			sender.sendMessage("Users online:");
			sender.sendMessage(Joiner.on(", ").join(playerNames));
		}
	}
}
