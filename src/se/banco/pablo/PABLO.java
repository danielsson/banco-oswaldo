package se.banco.pablo;

import java.nio.charset.Charset;


/**
 * General constants and utility functions for PABLO - the Protocol for Advanced
 * Bank, Legal and Ortodontal communication.
 * 
 * @author mattias
 *
 */
public class PABLO {
	
	public static final boolean DEBUG = false;
	public static final Charset CHARSET = Charset.forName("UTF-8");
	
	/**
	 * Binary format offsets.
	 */
	public static class CommandFormat {
		public static final int SIZE = 10;
		
		public static final int CMD_INDEX = 0;
		public static final int CMD_SIZE = 1;
		
		public static final int NUM1_INDEX = 1;
		public static final int NUM1_SIZE = 4;
	
		public static final int NUM2_INDEX = 5;
		public static final int NUM2_SIZE = 4;
	
		public static final int FLAG_INDEX = 9;
		public static final int FLAG_SIZE = 1;
		
	}
	
	
	
	/**
	 * Supported commands in the PABLO standard.
	 */
	public static class CommandCodes {
		public static final int BALANCE        = 0x01;
		public static final int WITHDRAW       = 0x02;
		public static final int DEPOSIT        = 0x03;
		public static final int ADJUST         = 0x04;

		public static final int LOGIN_RES      = 0x50; //Login response
		public static final int REQUEST_1INT   = 0x51; //The server requests one int
		public static final int REQUEST_2INT   = 0x52; //The server requests two int
		public static final int PRINT_MSG      = 0x53;
		public static final int PRINT_MSG_INT  = 0x54; //Print the in the second param
		
		public static final int LOGIN          = 0x70; //Login request
		public static final int MENU_SELECT    = 0x71; //Menu select

		public static final int NOP            = 0xFD;
		public static final int MESSAGE_FORMAT = 0xFE;
		public static final int BYE_BYE        = 0xFF;
	}
	
	/**
	 * Supported PABLO command flags.
	 */
	public static class CommandFlags {
		public static final byte FAILURE        = 0x01;
		public static final byte AUTH_FAIL      = 0x02;
		public static final byte REQUEST        = 0x04;
		public static final byte RESPONSE       = 0x08;
	}
}
