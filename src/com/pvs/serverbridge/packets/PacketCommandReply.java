package com.pvs.serverbridge.packets;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class PacketCommandReply extends Packet
{
	private final UUID sender;
	private final boolean result;

	public PacketCommandReply(final UUID sender, final boolean result)
	{
		this.sender = sender;
		this.result = result;
	}

	/** Returns the sender that is associated with this packet */
	public final UUID getSender()
	{
		return sender;
	}

	/** Returns the result of the command that was executed */
	public final boolean getResult()
	{
		return result;
	}

	// //////////////////////////////////////////////////////////////
	public static class Parser extends Packet.Parser
	{
		@Override
		public String write(final Packet packet)
		{
			final PacketCommandReply p = (PacketCommandReply) packet;
			return (p.getSender() == null ? "null" : p.getSender().toString()) + " " + Boolean.toString(p.getResult());
		}

		@Override
		public Packet read(final String string)
		{
			// Validate that the packet is properly structured
			final String[] components = string.split(" ");
			if (components.length != 2)
				return null;

			// Construct the packet object
			final UUID sender = (components[0].equalsIgnoreCase("null") ? null : UUID.fromString(components[0]));
			final boolean result = Boolean.parseBoolean(components[1]);
			return new PacketCommandReply(sender, result);
		}
	}

	public static class Processor extends Packet.Processor
	{
		@Override
		public void process(final Packet packet)
		{
			final PacketCommandReply p = (PacketCommandReply) packet;
			final CommandSender sender = p.getSender() == null ? Bukkit.getConsoleSender() : Bukkit.getPlayer(p.getSender());

			if (sender != null)
			{
				if (p.getResult())
					Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "The command was executed!");
				else
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The command failed to execute! Check your syntax");
			}
		}
	}
}
