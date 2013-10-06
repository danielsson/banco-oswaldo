package se.banco.lang;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Scanner;


/**
 * Provides internationalized strings for applications. The strings are loaded from text files.
 * 
 * An attempt to read a language not already loaded will result in an attempt to load the language 
 * from a file in ./lang/<localeid>.lang
 * 
 * The format of the file must be
 * <StringID> Somestring \n
 * ...
 * 
 * Example:
 * 
 * 12 Hello world.
 * 13 Bye world!
 * 
 * @author mattias
 *
 */
public class Strings {

	/**
	 * Language => (String Code => String).
	 */
	private static HashMap<Integer, HashMap<Integer, String>> languages = new HashMap<Integer, HashMap<Integer,String>>();
	
	public static boolean has(int locale) {
		return (languages.containsKey(locale) || getLanguageFile(locale).exists());
	}
	
	
	/**
	 * Get the specified string for the specified locale.
	 * @param stringId The string id.
	 * @param locale 
	 * @return The string or null if not found.
	 */
	public static String get(int stringId, int locale) {
		
		if(!languages.containsKey(locale))
			if(!load(locale))
				return languages.get(1).get(stringId);
		
		
		String retval = languages.get(locale).get(stringId);
		
		//Fallback to english if there is no translation for this phrase
		if(retval == null) {
			retval = languages.get(1).get(stringId);
		}
		
		if(retval == null) System.out.println("Failed to find string " + stringId);
		
		return retval;
	}
	
	
	/**
	 * Read the entire file into a byte array. TODO: This is bad in so many ways.
	 * @param locale
	 * @return
	 * @throws IOException
	 */
	public static byte[] getLanguageDescriptor(int locale) throws IOException {
		File file = getLanguageFile(locale);
		if(!has(locale)) {
			return null;
		}
		
		byte[] retval = new byte[(int) file.length()];
		
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(file));
			stream.read(retval);
			
		} catch (IOException ex) {
			throw ex;
		} finally {
			if(stream != null) stream.close();
		}
		
		return retval;
	}
	
	
	public static void insert(byte[] langBytes, int locale) throws IOException {
		File file = getLanguageFile(locale);
		file.createNewFile();
		
		OutputStream stream = null;
		try {
			stream = new BufferedOutputStream(new FileOutputStream(file));
			stream.write(langBytes);
			
		} finally {
			if (stream != null) stream.close();
		}
		
		load(locale);
	}
	
	
	/**
	 * Utility function for immediately printing the string requested.
	 * @param stringId
	 * @param locale
	 */
	public static void println(int stringId, int locale) {
		System.out.println(get(stringId, locale));
	}
	
	private static File getLanguageFile(int locale) {
		return new File("lang/" + locale + ".lang");
	}
	
	/**
	 * Attempt to load the specified language from the file system.
	 * @param local The language to load.
	 * @return true on success.
	 */
	private static boolean load(int locale) {
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		Scanner scanner = null;
		
		languages.put(locale, map); //This prevents further attempts to read the file if this fails.
		
		try {
			scanner = new Scanner(getLanguageFile(locale));
			
			while(scanner.hasNext()) {
				int stringId = scanner.nextInt();
				String string = scanner.nextLine().trim();
				map.put(stringId, string);
			}
			
		} catch(IOException ex) {
			return false;
			
		} finally {
			if(scanner != null) scanner.close();
		}
		
		return true;
	}
}
