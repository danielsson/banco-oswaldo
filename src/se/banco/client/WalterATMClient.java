package se.banco.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Console;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

import javax.management.RuntimeErrorException;

import com.sun.xml.internal.rngom.binary.PatternBuilder;

import se.banco.lang.Language;
import se.banco.lang.Strings;
import se.banco.pablo.PABLO;
import se.banco.pablo.PABLO.Command;
import se.banco.pablo.PABLO.Flags;
import se.banco.pablo.PABLOCommand;
import se.banco.pablo.PABLOBinary;
import se.banco.pablo.PABLONetworkListener;
import se.banco.pablo.PABLONetworkListener.PabloReciever;
import se.banco.server.Phrases;


/**
 * WALTER is the client to the OSWALD ATM banking server. 
 * @author mattias
 *
 */
public class WalterATMClient implements PabloReciever {
	
	private PABLONetworkListener listener;
	private Scanner inputScanner;
	
	private int locale = Language.ENGLISH.getCode();
	private volatile boolean isRunning = true;
	
	private LinkedBlockingQueue<PABLOCommand> queue = new LinkedBlockingQueue<PABLOCommand>();
	
	public static void main(String[] args) {
		String adress;
		int port;
		
		try {
			adress = args[0];
			port = Integer.parseInt(args[1]);
			
		} catch (Exception ex) {
			Strings.println(Phrases.USAGE, 1);
			return;
		}
		
		new WalterATMClient().run(adress, port);
	}
	
	
	/**
	 * Start the client by trying to connect to the specified server.
	 */
	public void run(String adress, int port) {
		System.out.println(Strings.get(Phrases.TRYING_CONN, locale) + " " + adress + ":" + port);
		
		Socket socket = null;
		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		PABLOCommand serverCommand = null;
		
		
		try {
			socket = new Socket(InetAddress.getByName(adress), port);
			
			in = new BufferedInputStream(socket.getInputStream());
			out = new BufferedOutputStream(socket.getOutputStream());
			
		} catch (UnknownHostException ex) {
			System.err.println("Unknown host:" + adress);
			return;
			
		} catch(IOException ex) {
			System.err.println("Couldn't open connection to: " + adress);
			return;
		}
		
		Strings.println(Phrases.CONN_TO_BNK, locale);
		
		listener = new PABLONetworkListener(in, this);
		new Thread(listener).start();
		
		inputScanner = new Scanner(System.in);
		
		while(isRunning) {
			try {
				//Recieve requests from the server.
				serverCommand = queue.take();
				PABLOCommand cmdToSend = new PABLOCommand();
				int a = 0, b = 0;
				
				/*
				 * The server sends one type of request to the client: The Request_*
				 * family of op-codes. We handle them and return the command specified
				 * in the first paramter in the command from the server.
				 */
				
				switch(serverCommand.getCode()) {
				
				case Command.REQUEST_1INT:
					if(serverCommand.getNumber2() != 0) {
						Strings.println(serverCommand.getNumber2(), locale);
					}
					
					printCursor(1);
					a = inputScanner.nextInt();
					break;
					
				case Command.REQUEST_2INT:
					if(serverCommand.getNumber2() != 0) {
						Strings.println(serverCommand.getNumber2(), locale);
					}
					
					printCursor(1);
					a = inputScanner.nextInt();
					printCursor(2);
					b = inputScanner.nextInt();
					break;
					
				case Command.REQUEST_STRING:
					PABLOBinary ret = new PABLOBinary();
					
					if(serverCommand.getNumber2() != 0) {
						Strings.println(serverCommand.getNumber2(), locale);
					}
					
					printCursor(1);
					inputScanner.nextLine();
					String input = inputScanner.nextLine();
					
					ret.setData(input.getBytes(PABLO.CHARSET));
					ret.write(out, Command.UPDATE_WELCOME);
					
					continue;
					
				case Command.PRINT_MSG:
					Strings.println(serverCommand.getNumber1(), locale);
					continue;
					
				case Command.PRINT_MSG_INT:
					//Print the specified string formatted with the specified number.
					if(Strings.get(serverCommand.getNumber1(), locale) == null) continue;
					System.out.println(
							String.format(
									Strings.get(serverCommand.getNumber1(), locale),
									serverCommand.getNumber2()));
					continue;
					
				case Command.SET_LANG:
					locale = serverCommand.getNumber1();
					if(!Strings.has(locale)) {
						//We dont have the specified language. Try to download it
						new PABLOCommand(Command.GET_LANGUAGE, locale, 0, Flags.REQUEST).write(out);
					} else {
						new PABLOCommand(Command.NOP, 0, 0, Flags.REQUEST).write(out);
					}
					
					continue;
				
				case Command.BYE_BYE:
					stop();
					return;

				default:
					System.err.println("Unknown server request: " + serverCommand);
				}
				
				
				cmdToSend.setCode(serverCommand.getNumber1());
				cmdToSend.setNumber1(a);
				cmdToSend.setNumber2(b);
				cmdToSend.setFlags(Flags.RESPONSE);
				
				cmdToSend.write(out);
				
			} catch (NoSuchElementException ex) {
				ex.printStackTrace();
				//The scanner was blocking on program exit and had to be terminated forcefully.
				break; //Break out of the loop.
				
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
			
		} //END while
		
		try {
			if(inputScanner != null) inputScanner.close();
			if(out != null) out.close();
			if(in != null) in.close();
		} catch (Exception e) {e.printStackTrace();}
		
	}
	
	
	public void stop() {
		isRunning = false;
		listener.quit();
	}

	@Override
	public boolean recieve(PABLOBinary msg) {
		
		try {
			switch(msg.getCode()) {
			case Command.MESSAGE_FORMAT:
				System.out.println(msg.getMessage());
				break;
				
			case Command.LANG_DOWNLOAD:
				Strings.insert(msg.getData(), locale);
				break;
			}
			
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
		return false;
	}


	@Override
	public boolean recieve(PABLOCommand cmd) {
		try {
			queue.put(cmd);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		return (cmd.getCode() == Command.BYE_BYE); //Quit on bye-bye
	}


	@Override
	public void onTerminated() {
		try {
			isRunning = false;
			System.in.close(); //FIXME: Fulhack.
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void printCursor(Object s) {
		System.out.print(s + "> ");
	}

}
