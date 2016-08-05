package lex;

import java.util.Stack;
import lib.ComplementSet;

public class Nfa {
	private final int EMPTY = -3;
	
	public int edge;
	public ComplementSet set;
	public Nfa next;
	public Nfa next2;
	public int lineno;
	public int acceptStart;
	public String acceptStr;	
	public int num;
	public int anchor;
	public static int totalStates = 0;
	public static Stack<Integer> usedNumStack = new Stack<Integer>();
	
	public static void Discard(int num) {
		usedNumStack.push(num);
	}
	
	public Nfa() {
		edge        = EMPTY;
		next        = null;
		next2       = null;
		lineno      = 0;
		acceptStart = -1;
		acceptStr   = null;
		if (usedNumStack.isEmpty()) {
			num = totalStates;
			++totalStates;
		}
		else 
			num = usedNumStack.pop();
	}
}
