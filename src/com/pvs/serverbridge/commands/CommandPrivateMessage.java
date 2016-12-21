package com.pvs.serverbridge.commands;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;
import com.ponyvillesquare.serverbridge.ServerBridgePlugin;
import com.pvs.serverbridge.packets.PacketPrivateMessage;

public class CommandPrivateMessage implements CommandExecutor {
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length < 2)
			return false;

		final String messageSender = sender.getName();
		final String receiver = args[0];
		final String message = Joiner.on(" ").join(ArrayUtils.subarray(args, 1, args.length));

		if (sendMessage(messageSender, receiver, message)) {
			if (!verifyMessage(sender, receiver, message, false))
				sender.sendMessage(ChatColor.RED + "Unexpected error has ocurred! The message was attempted being sent, but couldn't be verified as received");
			return true;
		}
		sender.sendMessage(ChatColor.AQUA + "Sending message over the bridge...");
		ServerBridgePlugin.getPacketHandler().sendPacket(new PacketPrivateMessage(messageSender, receiver, message));
		return true;
	}

	/** Transmits a message from the sender to the receiver; returns true if the message was sent */
	public final static boolean sendMessage(final String sender, final String receiver, final String message) {
		final Player target = Bukkit.getPlayer(receiver);
		if (target == null)
			return false;
		target.sendMessage(ChatColor.LIGHT_PURPLE + sender + " -> you: " + ChatColor.translateAlternateColorCodes('&', message));
		return true;
	}

	/** Displays the message as being sent to the receiver */
	public final static boolean verifyMessage(final CommandSender sender, final String receiver, final String message, final boolean guaranteedSafe) {
		final Player target = Bukkit.getPlayer(receiver);
		if (target == null && !guaranteedSafe)
			return false;
		final String senderName = sender.getName();
		final String receiverName = guaranteedSafe ? receiver : target.getName();

		if (!guaranteedSafe)
			ServerBridgePlugin.getRawLogger().info(senderName + " -> " + receiverName + ": " + ChatColor.stripColor(message));
		else
			ServerBridgePlugin.getRawLogger().info(senderName + " -> *" + receiverName + ": " + ChatColor.stripColor(message));
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "You -> " + receiverName + ": " + ChatColor.translateAlternateColorCodes('&', message));
		return true;
	}
}
