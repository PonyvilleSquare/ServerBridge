package com.ponyvillesquare.serverbridge.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;

import com.ponyvillesquare.serverbridge.Log;
import com.ponyvillesquare.serverbridge.ServerBridgePlugin;
import com.ponyvillesquare.serverbridge.packets.PacketKeepAlive;

public class PacketHandlerSlave extends PacketHandler {
	private Socket socket;
	private String ip;
	private int port;
	private int keepAliveTimer = 0;
	private int timer = 0;

	public PacketHandlerSlave() {
		super(false);
	}

	@Override
	protected void onOpen(final String ip, final int port) {
		this.ip = ip;
		this.port = port;
	}

	@Override
	protected void onClose() throws IOException {
		if (socket != null)
			socket.close();
	}

	@Override
	protected void onUpdate() throws IOException {
		timer++;

		// Attempt to connect to the master
		if (socket == null) {
			if (timer % (5 * ServerBridgePlugin.getSettings().retryTime) != 0)
				return;
			if (attemptConnection()) {
				Log.log("Connected to the server!", Level.INFO);
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} else {
				clearPackets();
				Log.log("Failed to connect to the server! Is it down?", Level.INFO);
			}
		}
		// Check that the connection hasn't been severed, that it is still valid
		else {
			if (timer % ServerBridgePlugin.getSettings().retryTime != 0)
				return;
			sendPacket(new PacketKeepAlive());
			if (keepAliveTimer++ > 5) {
				Log.log("Didn't get any keepalive messages, killing connection...", Level.INFO);
				killConnection();
			}
		}
	}

	// ///////////////////////////////////////////////////////////
	/** Attempts to open a connection with the master server. Returns true if connected */
	private final boolean attemptConnection() throws IOException {
		try {
			// InetAddress.getLocalHost()
			socket = new Socket(InetAddress.getByName(ip), port);
			return socket != null && socket.isConnected();
		} catch (final ConnectException exception) {
			socket = null;
		}
		return false;
	}

	/** Kills the connection, resetting everything */
	private final void killConnection() throws IOException {
		onClose();
		socket = null;
		out = null;
		in = null;
		keepAliveTimer = 0;
	}

	/** Provides a keepalive signal to the slave, preventing it from prematurely killing connection */
	public final void keepAlive() {
		keepAliveTimer = 0;
	}
}
