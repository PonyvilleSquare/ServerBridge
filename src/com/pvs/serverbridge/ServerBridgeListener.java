package com.pvs.serverbridge;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.dthielke.Herochat;
import com.dthielke.api.ChatResult;
import com.dthielke.api.event.ChannelChatEvent;
import com.pvs.serverbridge.packets.PacketMessage;

public class ServerBridgeListener implements Listener
{
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void herochatMessage(final ChannelChatEvent event)
	{
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
}
