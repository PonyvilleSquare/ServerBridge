package com.pvs.serverbridge.packets;

public abstract class Packet
{
	private boolean isMasterSide;

	/** Assigns the side the packet is currently on, do not use this method unless you know what you're doing... */
	public final void setSide(final boolean isMasterSide)
	{
		this.isMasterSide = isMasterSide;
	}

	/** Returns the side the packet is currently on */
	public final boolean isMasterSide()
	{
		return isMasterSide;
	}

	// ///////////////////////////////////////////////////////////
	
	/** The packet parser class will take care of constructing a packet, and parsing it when it is recieved */
	public static abstract class Parser
	{
		/** Writes the packet into a string that can be sent */
		public abstract String write(Packet packet);

		/** Reads a packet from the string */
		public abstract Packet read(String string);
	}

	/** The packet processor class will take care of performing some logic when the packet is received */
	public static abstract class Processor
	{
		/** Performs some action with the received packet */
		public abstract void process(Packet packet);
	}
}
