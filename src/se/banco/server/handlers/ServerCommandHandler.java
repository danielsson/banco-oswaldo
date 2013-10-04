package se.banco.server.handlers;

import java.io.IOException;
import java.io.OutputStream;

import se.banco.pablo.PABLO.Command;
import se.banco.pablo.PABLO.Flags;
import se.banco.pablo.PABLOCommand;
import se.banco.pablo.PABLOMessage;
import se.banco.pablo.PABLONetworkListener.PabloReciever;
import se.banco.server.ATMRunnable;
import se.banco.server.Account;
import se.banco.server.Phrases;



/**
 * Handle the different commands sent to the server.
 * @author mattias
 *
 */
public class ServerCommandHandler implements PabloReciever {
	
	private OutputStream out;
	private ATMRunnable runnable;
	
	public ServerCommandHandler(OutputStream out, ATMRunnable runnable) {
		this.runnable = runnable;
		this.out = out;
	}

	
	@Override
	public boolean recieve(PABLOMessage msg) throws IOException {
		System.out.print(msg.getMessage());
		return false;
	}

	
	@Override
	public boolean recieve(PABLOCommand cmd) throws IOException {
		Account account;
		boolean retval = false;
		
		//Check logged in
		if(runnable.getAccount() == null && cmd.getCode() != Command.LOGIN) {
			new PABLOCommand(Command.REQUEST_2INT, Command.LOGIN, Phrases.PLS_LOG_IN, Flags.AUTH_FAIL).write(out); //Ask the client to log in.
			return false;
		}
		
		switch(cmd.getCode()) {
		case Command.LOGIN:
			account = runnable.getServer().getAccounts().get(cmd.getNumber1());
			
			if(account != null && account.getPin() == cmd.getNumber2()) {
				runnable.setAccount(account);
			}
			
			break;
			
		case Command.MENU_SELECT:
			retval = menuResponse(cmd);
			return retval; //We dont know if we should print the menu.
			
		case Command.WITHDRAW:
			account = runnable.getAccount();
			
			if(account.authenticate(cmd.getNumber2())) {
				account.setBalance(account.getBalance() - cmd.getNumber1());
			
			} else {
				clientShowString(Phrases.WRONG_CODE);
			}
			
			clientShowBalance();
			
			break;
			
		case Command.DEPOSIT:
			account = runnable.getAccount();
			
			account.setBalance(account.getBalance() + cmd.getNumber1());
			
			clientShowBalance();
			break;
			
		}
		
		if(runnable.getAccount() != null) {
			showMenu();
		}
		
		return retval;
	}
	
	
	/**
	 * Respond to a menu selection from the client.
	 * @param cmd
	 * @return
	 * @throws IOException
	 */
	private boolean menuResponse(PABLOCommand cmd) throws IOException {
		PABLOCommand retval = new PABLOCommand();
		
		/*
		 * NOTE ON THE MENU
		 * In here you must call showMenu manually if you want it to show up.
		 */
		
		switch(cmd.getNumber1()) {
		case Command.BALANCE:
			clientShowBalance();
			showMenu();
			break;
			
		case Command.WITHDRAW:
			new PABLOCommand(
					Command.REQUEST_2INT, 
					Command.WITHDRAW,
					Phrases.PLS_WITHDRAW,
					(byte) (Flags.RESPONSE | Flags.REQUEST)) //Possible wtf
				.write(out);
			
			break;
			
		case Command.DEPOSIT:
			new PABLOCommand(
					Command.REQUEST_1INT, 
					Command.DEPOSIT,
					Phrases.AMNT_DEPOSIT,
					(byte) (Flags.RESPONSE | Flags.REQUEST)) //Possible wtf
				.write(out);
			
			break;
			
			
		case 4: //TODO:Exit hardcoded
			return true;
			
		default:
			PABLOMessage.send("Unknown option \n", out);
			showMenu();
		}
		
		return false;
	}
	
	
	@Override
	public void onTerminated() {
		runnable.setRunning(false);
	}
	
	
	/**
	 * Make the client show the menu.
	 * @throws IOException
	 */
	private void showMenu() throws IOException {
		new PABLOCommand(Command.REQUEST_1INT, Command.MENU_SELECT, Phrases.MENU, Flags.REQUEST).write(out);
	}
	
	
	/**
	 * Tell the client to display the specified language.
	 * @param stringId The message to display
	 * @param value The value to format into the string, or zero.
	 * @throws IOException
	 */
	private void clientShowString(int stringId, int value) throws IOException {
		new PABLOCommand(Command.PRINT_MSG_INT, stringId, value, Flags.RESPONSE).write(out);
	}
	
	/**
	 * Tell the client to display the specified language.
	 * @param stringId The message to display
	 * @throws IOException
	 */
	private void clientShowString(int stringId) throws IOException {
		clientShowString(stringId, 0);
	}
	
	/**
	 * Utility function for telling the client to display the current balance.
	 * @throws IOException
	 */
	private void clientShowBalance() throws IOException {
		clientShowString(Phrases.BALANCE_IS, runnable.getAccount().getBalance());
	}
}
