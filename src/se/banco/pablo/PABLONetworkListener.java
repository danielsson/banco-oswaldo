package se.banco.pablo;

import java.io.IOException;
import java.io.InputStream;

import se.banco.pablo.PABLO.Command;


/**
 * Listens to the network for messages from the server. 
 *
 * @author mattias
 *
 */
public class PABLONetworkListener implements Runnable {
	
	public interface PabloReciever {
		
		/**
		 * Recieve a text message from the network.
		 * @param msg The message.
		 * @return true if this should close the connection.
		 */
		public boolean recieve(PABLOBinary msg) throws IOException;
		
		/**
		 * Recieve a command message from the network.
		 * @param msg The message.
		 * @return true if this should close the connection.
		 */
		public boolean recieve(PABLOCommand cmd) throws IOException;
		
		
		/**
		 * Called when the connection is terminated.
		 */
		public void onTerminated();
		
		
	}
	
	private InputStream in;
	private PabloReciever reciever;
	
	private volatile boolean isRunning = true;
	
	
	
	public PABLONetworkListener(InputStream in, PabloReciever reciever) {
		this.in = in;
		this.reciever = reciever;
	}
	
	
	@Override
	public void run() {
		
		try {
			
			while(isRunning) {
				
				listen();
				
			}
			
		} catch(IOException ex) {
			System.err.println("Reading failed");
		}
	}
	
	
	/**
	 * Listens for a single incoming command and delegates it to the callbacks.
	 * @throws IOException
	 */
	public void listen() throws IOException {
		//Read the command byte
		in.mark(1);
		int command = in.read();
		in.reset();
		
		switch(command) {
		case -1:
			System.out.println("Connection closed");
			quit();
			break;
			
		case Command.UPDATE_WELCOME:
		case Command.MESSAGE_FORMAT:
		case Command.LANG_DOWNLOAD:
			PABLOBinary msg = PABLOBinary.parse(in);
			if(reciever.recieve(msg)) quit();
			break;
			
		default:
			PABLOCommand cmd = PABLOCommand.parse(in);
			
			if(PABLO.DEBUG) System.out.println("Debug: got: " + cmd.getCode()); //TODO
			
			if(reciever.recieve(cmd)) quit();
		}
	}
	
	public void quit() {
		isRunning = false;
		reciever.onTerminated();
	}
	
}
