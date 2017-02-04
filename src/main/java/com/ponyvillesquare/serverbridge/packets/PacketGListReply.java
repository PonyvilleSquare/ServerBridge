package com.ponyvillesquare.serverbridge.packets;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.kitteh.vanish.VanishManager;
import com.google.common.base.Joiner;
import com.ponyvillesquare.serverbridge.Log;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PacketGListReply extends Packet {
    private final UUID sender;
    private final String[] players;
    private static VanishManager vanish;

    public PacketGListReply(final Player sender, final String[] players) {
        this(sender.getUniqueId(), players);
    }

    public PacketGListReply(final UUID sender, final String[] players) {
        this.sender = sender;
        this.players = players;
    }

    /** Returns the sender that is associated with this packet */
    public final UUID getSender() {
        return sender;
    }

    /** Returns the players that were online on the end the packet originates from */
    public final String[] getPlayers() {
        return players;
    }

    // //////////////////////////////////////////////////////////////
    public static class Parser extends Packet.Parser {
        @Override
        public String write(final Packet packet) {
            final PacketGListReply p = (PacketGListReply) packet;
            return String.format("%s %s", p.getSender().toString(), Joiner.on(" ").join(p.getPlayers()));
        }

        @Override
        public Packet read(final String string) {
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

    public static class Processor extends Packet.Processor {
        @Override
        public void process(final Packet packet) {
            final PacketGListReply p = (PacketGListReply) packet;
            PermissionUser sender = PermissionsEx.getPermissionManager().getUser(p.sender);
            if (sender == null) {
                Log.debug("Sender is null, not doing anything.");
                return;
            }
            final List<String> playerNames = new LinkedList<String>();
            for (final Player player : Bukkit.getOnlinePlayers()) {
                if (vanish.isVanished(player)) {
                    if (sender.has("vanish.list")) {
                        playerNames.add(player.getName());
                    }
                } else
                    playerNames.add(player.getName());
            }

            for (final String name : p.getPlayers())
                playerNames.add(name);
            playerNames.sort(null);
            Optional<Player> senderPly = Optional.<Player>ofNullable(sender.getPlayer());
            if (senderPly.isPresent()) {
                senderPly.get().sendMessage("Users online:");
                senderPly.get().sendMessage(Joiner.on(", ").join(playerNames));
            }
        }
    }

    public static void setVanish(VanishManager vanishManager) {
        Validate.notNull(vanishManager);
        vanish = vanishManager;
    }
}
