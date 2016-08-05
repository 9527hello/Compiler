package lib;

public class BinToAsc {
	public String BinToASCII(int c, boolean useHex) {
		String retStr = "";
		
		c &= 0xff;
		if (' ' <= c && c < 0x7f && '\'' != c && '\\' != c) 
			retStr += (char)c;

		else {
			retStr += '\\';
			switch (c) {
			case '\\': 	retStr += '\\';	break;
			case '\'': 	retStr += '\'';	break;
			case '\b':	retStr += 'b';	break;
			case '\f':	retStr += 'f';	break;
			case '\t':	retStr += 't';	break;
			case '\r':	retStr += 'r';	break;
			case '\n':	retStr += 'n';	break;
			default:	String bufFormat = useHex ? "x%03x" : "x%03o";
						retStr += String.format(bufFormat, c);
						break;
			}
		}
		return retStr;
	}
}
