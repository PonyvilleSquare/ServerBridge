package com.ponyvillesquare.serverbridge.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ponyvillesquare.serverbridge.ServerBridgePlugin;
import com.ponyvillesquare.serverbridge.packets.PacketGList;

public class CommandGList implements CommandExecutor {
	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command may only be used by a player");
			return true;
		}

		final Player player = (Player) sender;
		player.sendMessage(ChatColor.AQUA + "Obtaining list, please be patient...");
		ServerBridgePlugin.getPacketHandler().sendPacket(new PacketGList(player.getUniqueId()));
		return true;
	}
}
