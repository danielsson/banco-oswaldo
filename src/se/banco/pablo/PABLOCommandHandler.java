package se.banco.pablo;

import java.security.InvalidParameterException;


/**
 * Interface for a PABLO command.
 * @author mattias
 *
 */
public interface PABLOCommandHandler {
	
	/**
	 * @return The command code for this command.
	 */
	public int getCommandCode();
	
	
	/**
	 * Process an incoming pablo command. Call getCommandCode to validate
	 * the incoming command.
	 * @param message The command to process.
	 * @throws InvalidParameterException if the message type is not handled by this class.
	 */
	public void process(PABLOCommand message) throws InvalidParameterException;
	
}
