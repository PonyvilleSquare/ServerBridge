package com.pvs.serverbridge.packets;

import com.ponyvillesquare.serverbridge.ServerBridgePlugin;
import com.pvs.serverbridge.communication.PacketHandlerMaster;
import com.pvs.serverbridge.communication.PacketHandlerSlave;

public class PacketKeepAlive extends Packet {
	public static class Parser extends Packet.Parser {
		@Override
		public String write(final Packet packet) {
			return "Payload";
		}

		@Override
		public Packet read(final String string) {
			return new PacketKeepAlive();
		}
	}

	public static class Processor extends Packet.Processor {
		@Override
		public void process(final Packet packet) {
			if (packet.isMasterSide())
				((PacketHandlerMaster) ServerBridgePlugin.getPacketHandler()).keepAlive();
			else
				((PacketHandlerSlave) ServerBridgePlugin.getPacketHandler()).keepAlive();
		}
	}
}
