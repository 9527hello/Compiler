package lex;

import java.util.Vector;

public class Dfa {
	public boolean mark;
	public Vector<Nfa> set;
	public int lineno;
	public String acceptStr;
	public int anchor;
		
	public Dfa() {
		mark      = false;
		set       = null;
		lineno    = 0;
		acceptStr = null;
		anchor    = 0;
	}
}
