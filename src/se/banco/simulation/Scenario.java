package se.banco.simulation;

import java.io.InputStream;


/**
 * Defines a senario that a client runner should use to test the client.
 * 
 * @author mattias
 *
 */
public abstract class Scenario {
	
	public abstract boolean send(InputStream stream);
}
