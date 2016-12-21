package com.ponyvillesquare.serverbridge.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;
import com.ponyvillesquare.serverbridge.ServerBridgePlugin;
import com.ponyvillesquare.serverbridge.packets.PacketCommand;

public class CommandRunCommand implements CommandExecutor {
	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		final UUID uuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
		final String command = Joiner.on(" ").join(args);
		sender.sendMessage(ChatColor.AQUA + "Trying to execute '" + command + "' on the other side...");
		sender.sendMessage(ChatColor.AQUA + "Please wait for a reply...");
		ServerBridgePlugin.getPacketHandler().sendPacket(new PacketCommand(uuid, command));
		return true;
	}
}
