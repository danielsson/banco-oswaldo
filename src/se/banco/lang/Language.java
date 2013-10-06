package se.banco.lang;


public enum Language {
	ENGLISH    (1),
	LATIN      (2),
	JAPANESE   (3),
	GERMAN     (4),
	SWAHILI    (5);
	
	int code;
	private Language(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
	public static String printLanguages() {
		StringBuilder sb = new StringBuilder();
		
		for(Language lang : Language.values()) {
			sb.append("(");
			sb.append(lang.getCode());
			sb.append(")");
			sb.append(lang.toString());
			sb.append(", ");
		}
		
		sb.append("\n");
		return sb.toString();
		
	}
}
