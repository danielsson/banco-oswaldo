package se.banco.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import se.banco.pablo.PABLO.CommandCodes;
import se.banco.pablo.PABLO.CommandFlags;
import se.banco.pablo.PABLOCommand;
import se.banco.pablo.PABLOMessage;
import se.banco.pablo.PabloNetworkListener;
import se.banco.server.handlers.ServerCommandHandler;


/**
 * Listens to commands on the socket and returns 
 * 
 * @author mattias
 *
 */
public class ATMRunnable implements Runnable {
	
	private Socket socket;
	private OswaldATMServer server;
	
	private ServerCommandHandler handler;
	private PabloNetworkListener networkListener;
	
	private Account account;
	
	
	private volatile boolean isRunning = true;
	
	public ATMRunnable(Socket socket, OswaldATMServer server) {
		this.socket = socket;
		this.server = server;
	}
	

	@Override
	public void run() {
		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		
		try {
			out = new BufferedOutputStream(socket.getOutputStream());
			in = new BufferedInputStream(socket.getInputStream());
			
			handler = new ServerCommandHandler(out, this);
			networkListener = new PabloNetworkListener(in, handler);
			
			
			PABLOMessage.send("HELLO!!! Welcome to Bancô Oswaldo! カタカナ \n", out);
			
			while(isRunning) {
				
				
				/*
				 * Print login message.
				 */
				if(account == null) {
					//User not logged in
					new PABLOCommand(CommandCodes.REQUEST_2INT, CommandCodes.LOGIN, ServerStrings.PLS_LOG_IN, CommandFlags.AUTH_FAIL).write(out);
				} 
				
				networkListener.listen();
			}
			
			PABLOMessage.send("Goodbye \n", out);
			new PABLOCommand(CommandCodes.BYE_BYE, 0, 0, CommandFlags.RESPONSE).write(out);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
			
		} finally {
			try {
				if(in != null) in.close();
				if(out != null) out.close();
			} catch (Exception e) {}
		}
	}


	public OswaldATMServer getServer() {
		return server;
	}


	public void setServer(OswaldATMServer server) {
		this.server = server;
	}


	public Account getAccount() {
		return account;
	}


	public void setAccount(Account account) {
		this.account = account;
	}


	public boolean isRunning() {
		return isRunning;
	}


	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
}
