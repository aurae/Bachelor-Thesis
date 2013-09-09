package de.hsb.ms.syn.mobile.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import de.hsb.ms.syn.common.interfaces.AndroidConnection;
import de.hsb.ms.syn.common.interfaces.Connection;
import de.hsb.ms.syn.common.net.ConnectionInputListener;
import de.hsb.ms.syn.common.net.NetMessage;
import de.hsb.ms.syn.common.util.Constants;
import de.hsb.ms.syn.common.util.Utils;

/**
 * Android connection using bluetooth
 * @author Marcel
 *
 */
public class AndroidBluetoothConnection extends AndroidConnection {
	
	private static final long serialVersionUID = 3783102027032937067L;
	
	/** Android bluetooth adapter */
	private BluetoothAdapter btAdapter = null;
	/** Bluetooth socket to hold the connection in */
	private BluetoothSocket btSocket = null;
	/** Outgoing stream to the Desktop host */
	private ObjectOutputStream outStream = null;
	/** Incoming stream from the Desktop host */
	private InputStream inStream = null;
	
	/** Android Handler to use for incoming NetMessages (needed because of Android's thread-safety */
	private Handler callback;
	/** Thread listening for incoming NetMessages */
	private Thread listeningThread;
	
	/**
	 * Constructor
	 * @param handler	Android Handler to use for incoming NetMessages
	 */
	public AndroidBluetoothConnection(Handler handler) {
		this.kind = Connection.BLUETOOTH;
		this.callback = handler;
		btAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	@Override
	public boolean isAvailable() {
		return (btAdapter != null) && (btAdapter.isEnabled());
	}
	
	@Override
	public void connect() {
		// Don't make this block
		final Connection c = this;
		new Thread(new Runnable() {
			@Override
			public void run() {
				Utils.log("Connecting...");
				// TODO Bluetooth discovery, rather than explicit MAC address lookup
				BluetoothDevice device = btAdapter.getRemoteDevice(Constants.LAPTOP_MAC_TIFFY);
				
				try {
					btSocket = device.createRfcommSocketToServiceRecord(Constants.LAPTOP_UUID);
				} catch (IOException e) {
					Log.d(Constants.LOG_TAG, "connect() createRfcomm: " + e.getMessage());
				}
				
				btAdapter.cancelDiscovery();
				
				try {
					btSocket.connect();
				} catch (IOException e) {
					Log.d(Constants.LOG_TAG, "connect() connect: " + e.getMessage());
				}
				try {
					outStream = new ObjectOutputStream(btSocket.getOutputStream());
				} catch (IOException e) {
					Log.d(Constants.LOG_TAG, "connect() getOutputStream: " + e.getMessage());
				}
				try {
					inStream = btSocket.getInputStream();
					// Initialize the listening thread
					listeningThread = new Thread(new ConnectionInputListener(inStream, c));
					listeningThread.start();
					
				} catch (IOException e) {
					Log.d(Constants.LOG_TAG, "connect() getInputStream: " + e.getMessage());
				}
			}
		}).start();
	}
	
	@Override
	public boolean isConnected() {
		return (btSocket != null) && (btSocket.isConnected());
	}
	
	@Override
	public void send(NetMessage message) {
		if (!isConnected()) return;
		// Attach this connection's ID to the NetMessage (in case a callback is needed)
		message.setSenderId(this.id);
		// Write out the message via the output stream
		try {
			outStream.writeObject(message);
			outStream.flush();
		} catch (IOException e) {
			Log.d(Constants.LOG_TAG, "send(): " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override
	public void receive(NetMessage message) {
		// Wrap the received NetMessage inside of an Android Message object for the Handler to handle
		Message m = new Message();
		m.obj = message;
		callback.sendMessage(m);
	}
	
	@Override
	public void close() {
		try {
			if (outStream != null)
				outStream.flush();
			if (listeningThread != null)
				listeningThread.interrupt();
			if (btSocket != null)
				btSocket.close();
		} catch (IOException e) {
			Log.d(Constants.LOG_TAG, "close(): " + e.getMessage());
		}
	}

	@Override
	public String getDescription() {
		return "Bluetooth";
	}
}
