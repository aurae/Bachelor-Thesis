package de.hsb.ms.syn.common.abs;

import java.io.IOException;

import de.hsb.ms.syn.common.vo.NetMessage;

public abstract class DesktopConnection extends Connection {
	
	private static final long serialVersionUID = 4891894783887222840L;

	public abstract void broadcast(NetMessage message);
	
	public abstract void send(NetMessage message, int id);

	public abstract int getConnectedCount();

	public abstract void disconnect(int id) throws IOException;
}
