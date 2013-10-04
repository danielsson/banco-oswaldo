package se.banco.server.handlers;

import java.io.IOException;
import java.io.OutputStream;

import se.banco.pablo.PABLO.CommandCodes;
import se.banco.pablo.PABLO.CommandFlags;
import se.banco.pablo.PABLOCommand;
import se.banco.pablo.PABLOMessage;
import se.banco.pablo.PabloNetworkListener.PabloReciever;
import se.banco.server.ATMRunnable;
import se.banco.server.Account;
import se.banco.server.ServerStrings;



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
		
		//Check logged in
		if(runnable.getAccount() == null && cmd.getCode() != CommandCodes.LOGIN) {
			new PABLOCommand(CommandCodes.REQUEST_2INT, CommandCodes.LOGIN, ServerStrings.PLS_LOG_IN, CommandFlags.AUTH_FAIL).write(out); //Ask the client to log in.
			return false;
		}
		
		switch(cmd.getCode()) {
		case CommandCodes.LOGIN:
			
			account = runnable.getServer().getAccounts().get(cmd.getNumber1());
			
			if(account != null && account.getPin() == cmd.getNumber2()) {
				runnable.setAccount(account);
			}
			
			break;
			
		case CommandCodes.MENU_SELECT:
			
			return menuResponse(cmd);
			
		case CommandCodes.WITHDRAW:
			account = runnable.getAccount();
			
			if(account.authenticate(cmd.getNumber2())) {
				account.setBalance(account.getBalance() - cmd.getNumber1());
			} else {
				PABLOMessage.send("NOPE DOPE, yo!", out);
			}
			break;
			
		case CommandCodes.DEPOSIT:
			account = runnable.getAccount();
			
			account.setBalance(account.getBalance() + cmd.getNumber1());
			break;
			
		}
		
		if(runnable.getAccount() != null) {
			showMenu();
		}
		
		return false;
	}
	
	
	/**
	 * Respond to a menu selection from the client.
	 * @param cmd
	 * @return
	 * @throws IOException
	 */
	private boolean menuResponse(PABLOCommand cmd) throws IOException {
		PABLOCommand retval = new PABLOCommand();
		
		switch(cmd.getNumber1()) {
		case CommandCodes.BALANCE:
			new PABLOCommand(CommandCodes.PRINT_MSG_INT, ServerStrings.BALANCE_IS, runnable.getAccount().getBalance(), CommandFlags.RESPONSE).write(out);
			showMenu();
			break;
			
		case CommandCodes.WITHDRAW:
			retval.setCode(CommandCodes.REQUEST_2INT);
			retval.setNumber1(CommandCodes.WITHDRAW);
			retval.setNumber2(ServerStrings.PLS_WITHDRAW);
			retval.setFlags((byte) (CommandFlags.RESPONSE | CommandFlags.REQUEST)); //Possible wtf
			retval.write(out);
			
			break;
			
		case CommandCodes.DEPOSIT:
			retval.setCode(CommandCodes.REQUEST_1INT);
			retval.setNumber1(CommandCodes.DEPOSIT);
			retval.setNumber2(ServerStrings.AMNT_DEPOSIT);
			retval.setFlags((byte) (CommandFlags.RESPONSE | CommandFlags.REQUEST));
			retval.write(out);
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
	 * 
	 * @throws IOException
	 */
	private void showMenu() throws IOException {
		new PABLOCommand(CommandCodes.REQUEST_1INT, CommandCodes.MENU_SELECT, 0, CommandFlags.REQUEST).write(out);
	}
}
