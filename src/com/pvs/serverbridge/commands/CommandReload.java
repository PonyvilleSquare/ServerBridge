package com.pvs.serverbridge.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.ponyvillesquare.serverbridge.ServerBridgePlugin;

public class CommandReload implements CommandExecutor {
	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		ServerBridgePlugin.getSettings().reload();
		sender.sendMessage(ChatColor.AQUA + "Reloaded the ServerBridge configs!");
		return true;
	}
}
