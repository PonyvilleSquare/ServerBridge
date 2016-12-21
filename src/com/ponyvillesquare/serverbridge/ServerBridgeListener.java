package com.ponyvillesquare.serverbridge;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.kitteh.vanish.VanishPerms;

import com.dthielke.Herochat;
import com.dthielke.api.ChatResult;
import com.dthielke.api.event.ChannelChatEvent;
import com.ponyvillesquare.serverbridge.packets.PacketServerMessages;
import com.ponyvillesquare.serverbridge.packets.PacketMessage;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class ServerBridgeListener implements Listener {
	private final GeoIPHook hook;

	public ServerBridgeListener() {
		hook = new GeoIPHook();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void herochatMessage(final ChannelChatEvent event) {
		if (event.getResult() != ChatResult.ALLOWED)
			return;
		if (!ServerBridgePlugin.getSettings().whitelistedChannels.contains(event.getChannel().getName().toLowerCase()))
			return;

		final PermissionUser p = PermissionsEx.getPermissionManager().getUser(event.getChatter().getPlayer());
		String str = "";
		if (event.getFormat().equalsIgnoreCase("{default}"))
			str += Herochat.getInstance().getConfig().getString("format.default");
		else
			str += event.getFormat();
		str = str.replace("{nick}", "");
		str = str.replace("{prefix}", p.getPrefix());
		str = str.replace("{suffix}", p.getSuffix());
		str = str.replace("{sender}", event.getChatter().getName());
		str = str.replace("{msg}", event.getMessage());
		ServerBridgePlugin.getPacketHandler().sendPacket(new PacketMessage(event.getChannel().getName() + " " + str));
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		// If they want to join without announce, abort everything, let Vanish handle it.
		if (VanishPerms.joinWithoutAnnounce(player))
			return;
		event.setJoinMessage(null);
		final String side = ServerBridgePlugin.getSettings().side.equals("master") ? "Creative" : "Colonizations";
		final Optional<String> country = hook.getCountry(player.getAddress().getAddress());
		final String message = ChatColor.GOLD + player.getName() + ChatColor.GRAY + " joined " + ChatColor.GOLD + side;
		if (country.isPresent()) {
			final String toSend = message + ChatColor.GRAY + " from " + ChatColor.GOLD + country.get();
			ServerBridgePlugin.getPacketHandler().sendPacket(new PacketServerMessages(toSend));
			for (final Player ply : Bukkit.getOnlinePlayers())
				ply.sendMessage(toSend);
		} else {
			final String toSend = message;
			ServerBridgePlugin.getPacketHandler().sendPacket(new PacketServerMessages(toSend));
			for (final Player ply : Bukkit.getOnlinePlayers())
				ply.sendMessage(toSend);
		}
	}
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerJoin(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		// If they want to quit vanished or something like that, let Vanish handle it.
		if (VanishPerms.silentQuit(player))
			return;
		event.setQuitMessage(null);
		final String side = ServerBridgePlugin.getSettings().side.equals("master") ? "Creative" : "Colonizations";
		final String message = ChatColor.YELLOW + player.getName() + " left " + side;
		ServerBridgePlugin.getPacketHandler().sendPacket(new PacketServerMessages(message));
	}
}
