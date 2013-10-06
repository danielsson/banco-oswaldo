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
public class PABLOBinary {
	
	private byte[] data;
	
	private int command;
	


	public PABLOBinary() {
		// TODO Auto-generated constructor stub
	}
	
	public PABLOBinary(String message) {
		this.data = message.getBytes(PABLO.CHARSET);
	}
	
	/**
	 * Read a PABLO message from the stream. The first byte in the stream MUST be the magic
	 * message format constant, 0xFE.
	 * @param stream The stream to read from.
	 * @return An instantiated message.
	 * @throws IOException
	 */
	public static PABLOBinary parse(InputStream stream) throws IOException {
		PABLOBinary msg = new PABLOBinary();
		
		byte[] headerBytes = new byte[5];
		stream.read(headerBytes);
		
		ByteBuffer header = ByteBuffer.allocate(5);
		header.put(headerBytes);
		header.position(0);
		
		msg.setCode(header.get() & 0xFF);
		
		//The next 4 bytes are the size of the message
		int size = header.getInt();
		
		byte [] data = new byte[size];
		stream.read(data);
		
		msg.setData(data);
		
		return msg;
		
	}
	
	
	/**
	 * Write a new PABLO binary message to the stream.
	 * @param stream
	 * @throws IOException
	 */
	public void write(OutputStream stream, int commandCode) throws IOException {
		int message_size = 5 + data.length;
		ByteBuffer buffer = ByteBuffer.allocate(message_size);
		
		buffer.put((byte) commandCode);
		buffer.putInt(data.length);
		buffer.put(data);
		
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
		new PABLOBinary(msg).write(out, Command.MESSAGE_FORMAT);
	}
	
	/**
	 * @return 
	 */
	public String getMessage() {
		return new String(data, PABLO.CHARSET);
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public byte[] getData() {
		return data;
	}
	
	
	public int getCode() {
		return command;
	}

	public void setCode(int command) {
		this.command = command;
	}
	
}
