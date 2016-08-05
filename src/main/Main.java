package main;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;
import lex.Globals;
import lex.Lex;
import lex.RegExpInput;
import lex.Thomposon;
import lib.ComplementSet;
import lib.Utils;

class Cell {
	public Cell next;
	public int num;
	ArrayList<Integer> list;
	public Cell(int n) {
		num  = n;
		list = new ArrayList<Integer>();
		list.add(n * 2);
		list.add(n * 3);
	}
}

class CellUnion {
	public Cell start;
	public Cell end;
}

class Item {
	public int a;
	public int b;
}

public class Main extends Utils {
	
	public static void test() {
		try {
			Vector<Byte> vec = new Vector<Byte>();
			BufferedInputStream input = new BufferedInputStream(new FileInputStream("a.txt"));
			byte[] test = new byte[2];
			int got;
			while (-1 != (got = input.read(test))) {
				for (int i = 0; i < got; ++i)
					vec.add(test[i]);
			}
			for (int i = 0; i < vec.size(); ++i) {
				System.out.println((byte)vec.get(i));
			}
			input.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static CellUnion union(int n) {
		Cell start    = new Cell(n);
		Cell end      = new Cell(n+1);
		start.next    = end;
		end.next      = null;
		end.num       = -1;
		end.list.clear();
		CellUnion ret = new CellUnion();
		ret.start     = start;
		ret.end       = end;
		return ret;
	}
	
	public static void celllist(Cell iter) {
		System.out.println("------------");
		while (null != iter) {
			System.out.println(iter.num);
			if (0 != iter.list.size()) {
				System.out.print("array:(");
				for (int i = 0; i < iter.list.size(); ++i) {
					System.out.print(iter.list.get(i));
					System.out.print(" ");
				}
				System.out.println(")");
			}
			System.out.print("->");
			iter = iter.next; 
		}		
	}
	
	public static void cell() {
		CellUnion first  = union(1);
		CellUnion second = union(2);
		first.end.next   = second.start;
		celllist(first.start);
		first.end.next = second.start.next;
		first.end.num  = second.start.num;
		first.end.list = second.start.list;
		celllist(first.start);
	}
	
	public static Cell newcell(Cell iter) {
		Cell tmp = iter;
		++tmp.num;
		return tmp;
	}
	
	public static void cellnew() {
		Cell test = new Cell(2);
		test      = newcell(test);
		System.out.println(test.num);
	}
	
	public static void settest() {
		ComplementSet compleSet = new ComplementSet(255);
		compleSet.Add(1);
		compleSet.Add(2);
		compleSet.PrintSet();
		compleSet.Complement();
		compleSet.PrintSet();
	}
		
	public static void vecadd(Vector<Integer> vec) {
		for (int i = 0; i < 10; ++i)
			vec.add(i);
	}
	
	public static void vecmodify(Vector<Integer> vec) {
		vec.setElementAt(8, 1);
	}
	
	public static void vectest() {
		Vector<Integer> vec = new Vector<Integer>();
		System.out.println(vec.size());
		vecadd(vec);
		System.out.println(vec.size());
		int i;
		for (i = 0; i < vec.size(); ++i)
			System.out.print(vec.get(i));
		System.out.print("\n");
		vecmodify(vec);
		for (i = 0; i < vec.size(); ++i)
			System.out.print(vec.get(i));
		System.out.print("\n");
	}
	
	public static void teststring(String tmp) {
		tmp = "hello world!";
	}
	
	public static void stringtest() {
		String test = "";
		System.out.println(test);
		System.out.println("aaa");
		teststring(test);
		System.out.println(test);
	}
	
	public static void vectdeptest() {
		Vector<Vector<Integer>> groups = new Vector<Vector<Integer>>();
	    Vector<Integer> vec1 = new Vector<Integer>();
	    Vector<Integer> vec2 = new Vector<Integer>();
	    vec1.add(1); vec1.add(2); vec1.add(3);
	    vec2.add(4); vec2.add(5);
	    groups.add(vec1); groups.add(vec2);
	    Vector<Integer> mod = groups.get(0);
	    System.out.println("before:");
	    for(int i = 0; i < mod.size(); ++i)
	    	System.out.printf("%d ", mod.get(i));	    
	    mod.remove(1);
	    System.out.println("after:");
	    Vector<Integer> prnt = groups.get(0);
	    for(int i = 0; i < prnt.size(); ++i)
	    	System.out.printf("%d ", prnt.get(i));
	}
	
	public static void lexGenerator(String[] args, int argc) {
		Globals.lexDefFileName = args[argc];
		Globals.lexOutFileName = "Lexyy.java";
		Lex lex                = new Lex();
		lex.ProcessLexFile(args, ++argc);
		/*lex.OpenFile();
		RegExpInput input      = new RegExpInput(lex.GetInputStream(), "");
		Thomposon thom         = new Thomposon(input);
		thom.BuildNfa();
		thom.PrintNfa();
		lex.CloseFile();*/
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//System.out.println("Hello Compiler!");
		/*test();
		char c = '0';
		System.out.println(Character.toUpperCase(c));
		int a = 3;
		System.out.println(a << 2);*/
		//cell();
		//cellnew();
		//settest();
		//vectest();
		//vectdeptest();
		lexGenerator(args, 0);
	}

}
