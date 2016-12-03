package com.pvs.serverbridge.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;

import com.pvs.serverbridge.Log;
import com.pvs.serverbridge.ServerBridgePlugin;
import com.pvs.serverbridge.packets.PacketKeepAlive;

public class PacketHandlerMaster extends PacketHandler {
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private int keepAliveTimer = 0;
	private int timer = 0;

	public PacketHandlerMaster() {
		super(true);
	}

	@Override
	protected void onOpen(final String ip, final int port) throws IOException {
		Log.log("Opening a server...", Level.INFO);
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(10000);
	}

	@Override
	protected void onClose() throws IOException {
		serverSocket.close();
		if (clientSocket != null)
			clientSocket.close();
		Log.log("Closed the server!", Level.INFO);
	}

	@Override
	protected void onUpdate() throws IOException {
		timer++;

		// Attempt to connect to the master
		if (clientSocket == null) {
			if (timer % (5 * ServerBridgePlugin.getSettings().retryTime) != 0)
				return;
			if (attemptConnection()) {
				Log.log("Detected a client, connected with it!", Level.INFO);
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} else {
				clearPackets();
				Log.log("Was unable to detect a client, is it not running?", Level.INFO);
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
			clientSocket = serverSocket.accept();
			return true;
		} catch (final SocketTimeoutException exception) {
		}
		return false;
	}

	/** Kills the connection, resetting everything */
	private final void killConnection() throws IOException {
		if (clientSocket != null)
			clientSocket.close();
		clientSocket = null;
		out = null;
		in = null;
		keepAliveTimer = 0;
	}

	/** Provides a keepalive signal to the master, preventing it from prematurely killing the connection */
	public final void keepAlive() {
		keepAliveTimer = 0;
	}
}
