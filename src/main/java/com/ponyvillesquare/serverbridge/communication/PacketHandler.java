package com.ponyvillesquare.serverbridge.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import org.apache.commons.lang.ArrayUtils;

import com.google.common.base.Joiner;
import com.ponyvillesquare.serverbridge.Log;
import com.ponyvillesquare.serverbridge.packets.Packet;

/**
 * The packet handler will manage the packets received and sent; it will also allow packets to be processed by a processing class
 */
public abstract class PacketHandler {
	// Streams, will be used to transmit packages, and receive them
	protected PrintWriter out;
	protected BufferedReader in;

	// Core variables
	private final boolean isMasterSide;

	protected PacketHandler(final boolean isMasterSide) {
		this.isMasterSide = isMasterSide;
	}

	/** Called every now and then, used to process packets when they are found */
	public final void onTick() {
		while (!incoming.isEmpty())
			readPacket(incoming.poll());
	}

	// ///////////////////////////////////////////////////////////////////////////
	// PACKET HANDLING // PACKET HANDLING // PACKET HANDLING // PACKET HANDLING //
	// ///////////////////////////////////////////////////////////////////////////

	// Connection data
	private boolean isOpen = true;

	// Raw string messages that are to be processed
	private final ConcurrentLinkedQueue<String> incoming = new ConcurrentLinkedQueue<String>();
	private final ConcurrentLinkedQueue<String> outgoing = new ConcurrentLinkedQueue<String>();

	// Worker threads to deal with things in a multithreaded manner, to avoid the main thread from being hogged
	private Thread worker;

	/** Sets up the working threads, allowing the system to be multithreaded */
	private final void initWorkerThreads() {
		worker = new Thread() {
			@Override
			public void run() {
				while (isOpen)
					try {
						while (in != null && in.ready())
							incoming.add(in.readLine());
						while (out != null && !outgoing.isEmpty())
							out.println(outgoing.remove());
						onUpdate();
						sleep(1000 / 5);
					} catch (final IOException e) {
						e.printStackTrace();
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
			}
		};
		worker.start();
	}

	// /////////////////////////////////////////////////////////////////////////////

	// All packets that are registered into the system
	private final HashMap<Class<? extends Packet>, String> packetNames = new HashMap<Class<? extends Packet>, String>();
	private final HashMap<String, Packet.Parser> packetParsers = new HashMap<String, Packet.Parser>();
	private final HashMap<String, Packet.Processor> packetProcessors = new HashMap<String, Packet.Processor>();
	private static final String spacer = "@#@";

	/** Registers a new packet for use within the system */
	public final void registerPacket(final Class<? extends Packet> packetClass, final Packet.Parser parser, final Packet.Processor processor, final String identifier) {
		packetNames.put(packetClass, identifier);
		packetParsers.put(identifier, parser);
		packetProcessors.put(identifier, processor);
	}

	/** Sends a packet to the other side */
	public final void sendPacket(final Packet packet) {
		// Validate that the packet is valid
		if (packet == null)
			return;
		final String identifier = packetNames.get(packet.getClass());
		final Packet.Parser parser = packetParsers.get(identifier);
		if (parser == null)
			return;

		// Send the packet
		Log.debug("Sending packet '" + identifier + spacer + parser.write(packet) + "'");
		outgoing.add(identifier + spacer + parser.write(packet));
	}

	/** Processes packets that are received */
	private final void readPacket(final String string) {
		Log.debug("Received packet '" + string + "'");

		// Validate that the received string is valid
		if (string == null)
			return;
		final String[] components = string.split(spacer);
		if (components.length < 2)
			return;

		// Validate that the contents of the string are valid
		final String identifier = components[0];
		final String payload = Joiner.on(spacer).join(ArrayUtils.subarray(components, 1, components.length));
		final Packet.Parser parser = packetParsers.get(identifier);
		final Packet.Processor processor = packetProcessors.get(identifier);
		if (parser == null || processor == null) {
			Log.log("Packet didn't have any parser and/or processor associated with it!", Level.SEVERE);
			return;
		}

		// Parse packet
		try {
			final Packet packet = parser.read(payload);
			packet.setSide(isMasterSide);
			processor.process(packet);
		} catch (final Exception exception) {
			Log.log(String.format("Failed to parse packet '%s' with payload '%s'!", identifier, payload), Level.SEVERE);
			Log.log(exception.getLocalizedMessage(), Level.SEVERE);
			exception.printStackTrace();
		}
	}

	/** Clears all the packets that are in the queue */
	protected final void clearPackets() {
		incoming.clear();
		outgoing.clear();
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// NETWORKING STUFF // NETWORKING STUFF // NETWORKING STUFF // NETWORKING STUFF //
	// ///////////////////////////////////////////////////////////////////////////////

	/** Opens a connection; the ip doesn't matter for the master side, only the client needs the ip */
	public final void open(final String ip, final int port) {
		try {
			onOpen(ip, port);
			initWorkerThreads();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/** Closes the connection */
	public final void close() {
		isOpen = false;
		try {
			worker.join();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		try {
			onClose();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/** Invoked when the sockets are opened, and initialize the system */
	protected abstract void onOpen(String ip, int port) throws IOException;

	/** Invoked when the sockets are closed, and shut down the system */
	protected abstract void onClose() throws IOException;

	/** Invoked five times every second, useful for performing various things */
	protected abstract void onUpdate() throws IOException;
}
