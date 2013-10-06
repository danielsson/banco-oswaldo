package se.banco.server;

import java.util.HashMap;
import java.util.Map;

/**
 * A single account in the system. Atm there is no user type, so we store the pin here aswell.
 * 
 * @author mattias
 *
 */
public class Account {
	
	private int account_number;
	private int pin;
	private int balance;
	
	private boolean isAdmin = false;
	


	public Account(int account_number, int pin, int balance, boolean isAdmin) {
		this.account_number = account_number;
		this.pin = pin;
		this.balance = balance;
		this.isAdmin = isAdmin;
	}
	

	/**
	 * Validates the supplied code against the list of single-use codes for this account.
	 * @param code The code to be validated.
	 * @return True if the code is valid.
	 */
	public boolean authenticate(int code) {
		return code > 0 && code < 100 && code % 2 != 0; //FIXME: I would not use this bank
	}
	
	/**
	 * Get some testing accounts.
	 * @return
	 */
	public static Map<Integer, Account> getDefaultAccounts() {
		Map<Integer, Account> accounts = new HashMap<Integer, Account>();
		
		accounts.put(1, new Account(1, 1234, 234, false));
		accounts.put(2, new Account(2, 1234, 999, true));
		accounts.put(3, new Account(3, 4321, -543666, false));
		accounts.put(4, new Account(4, 1234, 1, true));
		
		
		return accounts;
	}
	
	
	public int getAccount_number() {
		return account_number;
	}
	public void setAccount_number(int account_number) {
		this.account_number = account_number;
	}
	public int getPin() {
		return pin;
	}
	public void setPin(int pin) {
		this.pin = pin;
	}
	public int getBalance() {
		return balance;
	}
	public void setBalance(int balance) {
		this.balance = balance;
	}
	public boolean isAdmin() {
		return isAdmin;
	}

}
