package se.banco.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * The new, better ATM server. 
 * 
 * @author Lovisa & Mattias
 *
 */
public class OswaldATMServer {
	
	private static final int PORT = 8989;
	
	/**
	 * Keeps tabs of the threads, prevents unnecessary thread creation.
	 */
	private static Executor listener_executor = Executors.newCachedThreadPool();
	
	private ServerSocket socket;
	private volatile boolean isRunning = true;
	
	private Map<Integer, Account> accounts = Account.getDefaultAccounts();
	
	
	
	public static void main(String[] args) {
		
		OswaldATMServer server = new OswaldATMServer();
		
		server.run();
	}
	
	
	
	/**
	 * Start an instance of the server and begin listening to incoming connections.
	 */
	public void run() {
		
		try {
			socket = new ServerSocket(PORT);
			
		} catch(IOException ex) {
			System.err.println("Failed to bind port " + PORT);
			return;
		}

		System.out.println("Server started listening to port " + PORT);
		
		while(isRunning) {
			try {
				ATMRunnable runnable = new ATMRunnable(socket.accept(), this);
				listener_executor.execute(runnable);
			
			} catch (IOException ex) {
				System.out.println("IOError: " + ex.getMessage());
				isRunning = false;
			}
		}
		
		try {
			socket.close();
		} catch(Exception e) {}		
	}
	
	
	
	public Map<Integer, Account> getAccounts() {
		return accounts;
	}
}
