package se.banco.server.handlers;

import java.io.IOException;
import java.io.OutputStream;

import se.banco.lang.Language;
import se.banco.lang.Strings;
import se.banco.pablo.PABLO.Command;
import se.banco.pablo.PABLO.Flags;
import se.banco.pablo.PABLOBinary;
import se.banco.pablo.PABLOCommand;
import se.banco.pablo.PABLONetworkListener;
import se.banco.pablo.PABLONetworkListener.PabloReciever;
import se.banco.server.ATMRunnable;
import se.banco.server.Account;
import se.banco.server.Phrases;



/**
 * Handle messages sent to the oswald server. This should be used as a reciever to a {@link PABLONetworkListener}.
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
	public boolean recieve(PABLOBinary msg) throws IOException {
		
		switch(msg.getCode()) {
		case Command.MESSAGE_FORMAT:
			System.out.print(msg.getMessage());
			break;
			
		case Command.UPDATE_WELCOME:
			if(runnable.getAccount() == null || !runnable.getAccount().isAdmin()) {
				clientShowString(Phrases.CANT_DO_THAT);
				break;
			}
			
			if(msg.getData().length > 80) {
				clientShowString(Phrases.WELCOME_TOO_LNG);
				break;
			}
			
			runnable.getServer().setWelcomeMessage(msg.getMessage());
		}
		
		showMenu();
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
		
		case Command.LANG_SELECT:
			new PABLOCommand(Command.SET_LANG, cmd.getNumber1(), 0, Flags.RESPONSE).write(out);
			return retval;
			
		case Command.GET_LANGUAGE:
			PABLOBinary langDownload = new PABLOBinary();
			
			byte[] language = Strings.getLanguageDescriptor(cmd.getNumber1());
			
			if(language != null) {
				langDownload.setData(language);
				langDownload.write(out, Command.LANG_DOWNLOAD);
			
			} else {
				clientShowString(Phrases.UNKNOWN_LANG);
			}
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
			
		case Command.LANG_SELECT:
			clientShowString(Phrases.SELECT_LANG);
			PABLOBinary.println(Language.printLanguages(), out);
			
			new PABLOCommand(
					Command.REQUEST_1INT,
					Command.LANG_SELECT,
					0,
					Flags.REQUEST)
				.write(out);
			
			break;
			
		case 5: //TODO:Exit hardcoded
			return true;
			
			
		case 6:
			if(runnable.getAccount() == null || !runnable.getAccount().isAdmin()) {
				clientShowString(Phrases.CANT_DO_THAT);
				showMenu();
				return false;
			}
			
			new PABLOCommand(
					Command.REQUEST_STRING,
					Command.UPDATE_WELCOME,
					Phrases.SET_WELCOME,
					Flags.REQUEST)
				.write(out);
			break;
			
		default:
			clientShowString(Phrases.CANT_DO_THAT);
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
		if(runnable.getAccount() != null  && runnable.getAccount().isAdmin()) {
			new PABLOCommand(Command.REQUEST_1INT, Command.MENU_SELECT, Phrases.BANK_MENU, Flags.REQUEST).write(out);
		} else {
			new PABLOCommand(Command.REQUEST_1INT, Command.MENU_SELECT, Phrases.MENU, Flags.REQUEST).write(out);
		}
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
		new PABLOCommand(Command.PRINT_MSG, stringId, 0, Flags.RESPONSE).write(out);
	}
	
	/**
	 * Utility function for telling the client to display the current balance.
	 * @throws IOException
	 */
	private void clientShowBalance() throws IOException {
		clientShowString(Phrases.BALANCE_IS, runnable.getAccount().getBalance());
	}
}
