package lib;

import java.util.Vector;
import lex.Thomposon;

public class Esc {
	private Thomposon thomMethod;
	private int inputIndex;
	
	private boolean IsHexDigit(char c) {
		c = Character.toUpperCase(c);
		return c >= '0' && c <= 'F';
	}
	
	private boolean IsOctDigit(char c) {
		return c >= '0' && c <= '7';
	}
	
	private int HexToBin(char c) {
		return (Character.isDigit(c) ? (c) - '0' : (Character.toUpperCase(c) - 'A' + 10)) & 0xf;
	}
	
	private int OctToBin(char c) {
		return (c - '0') & 0x7;
	}
	
	private char ReadNextByte(Vector<Integer>input) {
		char next;
		thomMethod.IncreaseReadIndex();
		inputIndex += 1;
		next = (char)input.get(inputIndex).intValue();	
		return next;
	}
	
	public Esc(Thomposon thom) {
		thomMethod = thom;
	}

	public char ESC(Vector<Integer>input, int readIndex) {
		char retVal = 0;
		char cur    = (char)input.get(readIndex).intValue();
		inputIndex  = readIndex;
		
		if ('\\' != cur) {
			retVal = cur;
			thomMethod.IncreaseReadIndex();
		}
		else {
			cur = ReadNextByte(input);
			if ('\r' == cur) {
				cur = ReadNextByte(input);
				if ('\n' == cur) 
					retVal = '\\';
			}
			else {
				int tmp;
				switch (cur = Character.toUpperCase(cur)) {
				case 'B':	retVal = '\b'; thomMethod.IncreaseReadIndex();		
							break;
				case 'F':	retVal = '\f'; thomMethod.IncreaseReadIndex();		
							break;
				case 'N':	retVal = '\n'; thomMethod.IncreaseReadIndex();		
							break;
				case 'R':	retVal = '\r'; thomMethod.IncreaseReadIndex();		
							break;
				case 'S':	retVal = ' '; thomMethod.IncreaseReadIndex();		
							break;
				case 'T':	retVal = '\t'; thomMethod.IncreaseReadIndex();		
							break;
				case 'E':	retVal = '\033'; thomMethod.IncreaseReadIndex();	
							break; //ESC
				case '^':	retVal = ReadNextByte(input); //´ý²âÊÔ
					        retVal = (char)(Character.toUpperCase(retVal) - '@');
					        thomMethod.IncreaseReadIndex();
							break;
				case 'X':	tmp = 0;
							cur = ReadNextByte(input);
							if (IsHexDigit(cur)) {
								tmp = HexToBin(cur);
								cur = ReadNextByte(input);
							}
							if (IsHexDigit(cur)) {
								tmp <<= 4;
								tmp  |= HexToBin(cur);
								cur = ReadNextByte(input);
							}
							if (IsHexDigit(cur)) {
								tmp <<= 4;
								tmp  |= HexToBin(cur);
								cur = ReadNextByte(input);
							}		
							retVal = (char)tmp;
							break;
				default:	if (!IsOctDigit(cur)) {
								retVal = cur;
								thomMethod.IncreaseReadIndex();
							}
							else {
								tmp = 0;
								cur = ReadNextByte(input);
								if (IsOctDigit(cur)) {
									tmp = OctToBin(cur);
									cur = ReadNextByte(input);
								}
								if (IsOctDigit(cur)) {
									tmp <<= 3;
									tmp  |= OctToBin(cur);
									cur = ReadNextByte(input);
								}
								if (IsOctDigit(cur)) {
									tmp <<= 3;
									tmp  |= OctToBin(cur);
									cur = ReadNextByte(input);
								}		
								retVal = (char)tmp;
							}
							break;							
				}
			}
		}
		return retVal;
	}
}
