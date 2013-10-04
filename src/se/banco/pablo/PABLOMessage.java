package se.banco.pablo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import se.banco.pablo.PABLO.Command;


/**
 * A PABLO message accordning to the standard. A PABLO message is a special format for
 * sending strings of arbirary length. 
 * 
 * @author mattias
 *
 */
public class PABLOMessage {
	
	private String message;
	
	public PABLOMessage() {
		// TODO Auto-generated constructor stub
	}
	
	public PABLOMessage(String message) {
		this.message = message;
	}
	
	/**
	 * Read a PABLO message from the stream. The first byte in the stream MUST be the magic
	 * message format constant, 0xFE.
	 * @param stream The stream to read from.
	 * @return An instantiated message.
	 * @throws IOException
	 */
	public static PABLOMessage parse(InputStream stream) throws IOException {
		PABLOMessage msg = new PABLOMessage();
		
		//Initial byte must be the magic value for message format
		if(stream.read() != Command.MESSAGE_FORMAT) {
			throw new IOException("The stream is not in message format.");
		}
		
		//The second byte is the size of the message
		int size = stream.read();
		
		byte[] stringbuffer = new byte[size];
		stream.read(stringbuffer);
		
		msg.setMessage(new String(stringbuffer, PABLO.CHARSET));
		
		return msg;
		
	}
	
	
	/**
	 * Write a new PABLO message to the stream.
	 * @param stream
	 * @throws IOException
	 */
	public void write(OutputStream stream) throws IOException {
		byte[] string_buffer = message.getBytes(PABLO.CHARSET);
		int message_size = 2 + string_buffer.length;
		ByteBuffer buffer = ByteBuffer.allocate(message_size);
		
		buffer.put(new Integer(Command.MESSAGE_FORMAT).byteValue());
		buffer.put(new Integer(string_buffer.length).byteValue());
		buffer.put(string_buffer);
		
		stream.write(buffer.array());
		stream.flush();

		if(PABLO.DEBUG) System.out.println(Arrays.toString(buffer.array()));
	}
	
	
	/**
	 * Utility method for sending text messages.
	 * @param msg
	 * @param out
	 * @throws IOException
	 */
	public static void send(String msg, OutputStream out) throws IOException {
		new PABLOMessage(msg).write(out);
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}
