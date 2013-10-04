package se.banco.lang;

import java.io.File;
import java.io.IOException;
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
	
	private static boolean has(int locale) {
		return languages.containsKey(locale);
	}
	
	
	/**
	 * Get the specified string for the specified locale.
	 * @param stringId The string id.
	 * @param locale 
	 * @return The string or null if not found.
	 */
	public static String get(int stringId, int locale) {
		
		if(!has(locale))
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
	 * Utility function for immediately printing the string requested.
	 * @param stringId
	 * @param locale
	 */
	public static void println(int stringId, int locale) {
		System.out.println(get(stringId, locale));
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
			scanner = new Scanner(new File("lang/" + locale + ".lang"));
			
			while(scanner.hasNext()) {
				int stringId = scanner.nextInt();
				String string = scanner.nextLine().trim();
				map.put(stringId, string);
			}
			
		} catch(IOException ex) {
			ex.printStackTrace();
			return false;
			
		} finally {
			if(scanner != null) scanner.close();
		}
		
		return true;
	}
}
