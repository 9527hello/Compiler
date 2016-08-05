package lex;

import java.util.HashMap;
import java.util.Vector;

public class MacroTable {
	private HashMap<String, String> table;
	
	public MacroTable() {
		table = new HashMap<String, String>();
	}
	
	public void InsertMacro(Vector<Integer> vec) {
		String key, val;
		key = val = "";
		boolean appendKey = true;
		for (int i = 0; i < vec.size(); ++i) {
			char tmp = (char)vec.get(i).intValue();
			if (Character.isWhitespace(tmp)) {
				appendKey = false;
				continue;
			}
			if (appendKey)
				key += tmp;
			else
				val += tmp;
		}
		//val += '\0'; //这个必不可少，在Thomposon的Advance中用来判断当前栈帧是否读取完毕
		table.put(key, val);
	}
	
	public Vector<Integer> GetMacro(String key) {
		String val             = table.get(key);
		Vector<Integer> valVec = new Vector<Integer>();
		byte[] tmp             = val.getBytes();
		for (int i = 0; i < tmp.length; ++i) 
			valVec.add((int)tmp[i]);
		return valVec;
	}
}
