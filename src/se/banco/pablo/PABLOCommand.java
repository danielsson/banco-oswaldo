package se.banco.pablo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import se.banco.pablo.PABLO.Command;
import se.banco.pablo.PABLO.Format;


/**
 * A holder for a PABLO Command Message. 
 * 
 * @author mattias
 *
 */
public class PABLOCommand {
	
	private int code = Command.NOP;
	
	int number1 = 0;
	int number2 = 0;
	
	byte flags = 0;
	
	public PABLOCommand(int code, int number1, int number2, byte flags) {
		this.code = code;
		this.number1 = number1;
		this.number2 = number2;
		this.flags = flags;
	}
	
	public PABLOCommand() {
	}
	
	
	/**
	 * Parse a PABLO command from the specified packet. The first byte must be the command byte.
	 * @param packet
	 * @return
	 * @throws IOException
	 */
	public static PABLOCommand parse(InputStream stream) throws IOException {
		
		byte[] packet = new byte[Format.SIZE];
		
		int bytes_read = stream.read(packet);
		
		if(bytes_read != 10) {
			throw new IOException("Incorrect data length, read " + bytes_read + " bytes.");
		}
		
		return parse(packet);
		
	}
	
	/**
	 * Parse a PABLO command from the specified packet. The first byte must be the command byte.
	 * @param packet
	 * @return
	 * @throws IOException
	 */
	public static PABLOCommand parse(byte[] packet) throws IOException {
		PABLOCommand msg = new PABLOCommand();
		
		ByteBuffer buffer = ByteBuffer.allocate(Format.SIZE);
		buffer.put(packet);
		
		buffer.position(0);
		
		msg.setCode(    buffer.get(    Format.CMD_INDEX) & 0xFF); //Fix java signedness weirdness
		msg.setNumber1( buffer.getInt( Format.NUM1_INDEX));
		msg.setNumber2( buffer.getInt( Format.NUM2_INDEX));
		msg.setFlags(   buffer.get(    Format.FLAG_INDEX));
		
		if(PABLO.DEBUG) System.out.println("Recieved: " + Arrays.toString(buffer.array()));
		
		return msg;
	}
	
	
	/**
	 * Write this command to the specified stream.
	 * @param stream
	 * @throws IOException
	 */
	public void write(OutputStream stream) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(Format.SIZE);
		
		
		buffer.put(new Integer(code).byteValue());
		
		buffer.putInt(getNumber1());
		buffer.putInt(getNumber2());
		
		buffer.put(flags);
		
		stream.write(buffer.array());
		if(PABLO.DEBUG) System.out.println("Writing: " + Arrays.toString(buffer.array()));
		stream.flush();
	}
	
	
	
	

	public int getNumber1() {
		return number1;
	}

	public void setNumber1(int number1) {
		this.number1 = number1;
	}

	public int getNumber2() {
		return number2;
	}

	public void setNumber2(int number2) {
		this.number2 = number2;
	}

	public byte getFlags() {
		return flags;
	}

	public void setFlags(byte flags) {
		this.flags = flags;
	}


	public int getCode() {
		return code;
	}


	public void setCode(int code) {
		this.code = code;
	}
	
	
	public static void main(String[] args) {
		PABLOCommand cmd = new PABLOCommand();
		
		cmd.setCode(Command.LOGIN_RES);
		cmd.setNumber1(78);
		cmd.setNumber2(99);
		
		try {
			cmd.write(System.out);
			
			
			byte[] test = new byte[] {80, 0, 0, 0, 78, 0, 0, 0, 99, 4};
			
			
			PABLOCommand ret = PABLOCommand.parse(test);
			
			
			System.out.println(ret.getCode());
			System.out.println(ret.getFlags());
		} catch (IOException e) {
			// TODO Auto-generated catch blockŒ
			e.printStackTrace();
		}
		

		
		
	}
	
	@Override
	public String toString() {
		return code + " " + number1 + " " + number2 + " " + flags;
	}

}
