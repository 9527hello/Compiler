package lex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Vector;
import lib.BinToAsc;
import lib.Utils;

public class Lex {
	private final char MAXCHAR     = 128;
	private final int F            = -1;
	private final int NONE         = 0;
	private final String dtranName = "yyNxt";
	
	private BufferedReader lexDefReader;
	private BufferedWriter lexOutWriter;
	private MacroTable table;
	private boolean inComment  = false;
	private boolean noHeader   = false;
	private boolean noLines    = false;
	private String templateFileName;
	private int templateLineno;
	private TemplateFileInput templateInput;
	
	private Vector<Integer> GetLine() {
		Vector<Integer> inputVec = new Vector<Integer>();
		int got;
		try {
			while (-1 != (got = lexDefReader.read())) {
				if ('\n' == got) 
					break;
				inputVec.add(got);
			}	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return inputVec;
	}
	
	private void WriteChar(int c) {
		try {
			lexOutWriter.write(c);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String GetCommentString(String str) {
		String commentStr = str.replace("/*", "//").replace("*/", "");
		return commentStr;
	}
	
	private void WriteString(String str, boolean comment) {
		try {
			String printStr = str;
			if (comment)
				printStr = GetCommentString(printStr);
			lexOutWriter.write(printStr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private void PrintYynext(int[][] dtran, int row, int col) {
		WriteString("\t{\n", false);
		String str;
		int i, j;
		for (i = 0; i < row; ++i) {
			str = String.format("\t/* %02d */  {\n       ", i);
			WriteString(str, false);
			for (j = 0; j < col; ++j) {
				str = String.format("%3d", dtran[i][j]);
				WriteString(str, false);
				if (j < col - 1)
					WriteString(", ", false);
				
				if (9 == (j % 10) && j != col - 1)
					WriteString("\n       ", false);
			}
			if (j > 10)
				WriteString("\n       ", false);
			str = String.format(" }%c\n", i < row - 1 ? ',' : ' ');
			WriteString(str, false);
		}
		WriteString("\t};\n", false);
	}
	
	//输出基于表驱动的词法分析器的第一部分，即LEX.PAR在Ctrl+F的第一部分
	private boolean TemplateFirstPart() {
		//打开输入文件，即LEX定义文件
		templateInput = new TemplateFileInput(templateFileName);
		if (!templateInput.OpenFile()) {
			System.err.println("Can't not open file:" + templateFileName + " " + Utils.GetErrPositon());
			return false;
		}
		templateLineno = 0;
		TemplateInside();
		return true;
	}
	
	private void TemplateInside() {
		String str;
		if (!noLines) {
			str = String.format("\n//line %d \"%s\"\n", templateLineno + 1, templateFileName);
			WriteString(str, false);
		}
		int index, i;
		int processingComment = 0;
		try {
			Vector<Integer> inputVec;
			while (true) {
				inputVec = templateInput.GetLine();
				if (!inputVec.isEmpty()) 
					inputVec.add((int)'\n');
				else
					break;
				++templateLineno;
				if ('\f' == inputVec.get(0)) 
					break;
				index = 0;
				while (index < inputVec.size() && ' ' == (char)inputVec.get(index).intValue())
					++index;
				if (index == inputVec.size()) 
					continue;
				if ('@' == inputVec.get(index)) {
					processingComment = 1;
					continue;
				}
				else if (1 == processingComment) { //前一行是注释，但当前行不是
					processingComment = 0;
					if (!noLines) {
						str = String.format("\n//line %d \"%s\"\n", templateLineno, templateFileName);
						WriteString(str, false);
					}
				}
				for (i = index; i < inputVec.size(); ++i) 
					WriteChar(inputVec.get(i));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//去除注释，即替换为空格
	private void StripComments(Vector<Integer> inputVec) {
		for (int i = 0; i < inputVec.size(); ++i) {
			if (inComment) {
				if ('*' == inputVec.get(i) && '/' == inputVec.get(i + 1)) {
					inComment = false;
					inputVec.setElementAt((int)' ', i);
					++i;
				}
				
				if (!Character.isWhitespace((char)inputVec.get(i).intValue()))
					inputVec.setElementAt((int)' ', i);
			}
			else {
				if ('/' == inputVec.get(i) && '*' == inputVec.get(i + 1)) {
					inComment = true;
					inputVec.setElementAt((int)' ', i);
					++i;
					inputVec.setElementAt((int)' ', i);
				}
			}
		}
	}
	
	//输出头部注释，描述没有压缩的DFA
	private void PrintHeader(int[][] dtran, Accept[] accepts) {
		int i, j;
		int lastTransition;
		int printedLength = 0;
		BinToAsc asc = new BinToAsc();
		String ascString;
	
		WriteString("/*-----------------------------------\n", false);
		WriteString(" * DFA (start state is 0) is:\n", true);
		
		for (i = 0; i < dtran.length; ++i) {
			if (null == accepts[i].acceptStr)
				WriteString(" * State " + i + " [nonaccepting]", true);
			else {
				WriteString(" * State " + i + "[accepting, line " + accepts[i].lineno + " <", true);
				WriteString(accepts[i].acceptStr + ">]", true);
			}
			lastTransition = -1;
			for (j = 0; j < MAXCHAR; ++j) {
				if (F != dtran[i][j]) {
					if (lastTransition != dtran[i][j]) {
						WriteString("\n * goto " + dtran[i][j] + " on ", true);
						printedLength = 0;
					}
					
					ascString = asc.BinToASCII(j, true);
					WriteString(ascString, true);
					
					if ((printedLength += ascString.length()) > 56) {
						WriteString("\n *               ", true);
						printedLength = 0;
					}
					
					lastTransition = dtran[i][j];
				}
			}
			WriteString("\n", true);
		}
		WriteString(" */\n\n", false);
	}
	
	//输出%{到%}之间的部分
	private boolean ProcessHead() {
		if (false == OpenFile())
			return false;
		boolean include = false;
		Vector<Integer> inputVec;
		if (!noLines)
			WriteString("//line 1 \"" + Globals.lexDefFileName + "\"\n", false);
		while (true) {
			inputVec = GetLine();
			if (!inputVec.isEmpty()) 
				inputVec.add((int)'\n');
			else
				break;
			if (!include) 
				StripComments(inputVec);
			++Globals.actualLineno;
			if ('%' == inputVec.get(0)) {
				if ('%' == inputVec.get(1)) { //lex定义文件头部#include和宏定义结束
					WriteString("\n", false);
					break;
				}
				else {
					if ('{' == inputVec.get(1)) //#include部分
						include = true;
					else if ('}' == inputVec.get(1)) //#include部分结束
						include = false;
					else
						System.err.printf("Ignoring illegal %%%c directive\n" + Utils.GetErrPositon(), inputVec.get(1));
				}
			}
			else if (include || Character.isWhitespace((char)inputVec.get(0).intValue())) {		
				for (int i = 0; i < inputVec.size(); ++i) 
					WriteChar(inputVec.get(i));
			}
			else { //处理宏定义部分，添加宏转换到hash表中
				NewMacro(inputVec);
				WriteString("\n", false);
			}
		}
		return true;
	}
	
	private void DefNext() {
		WriteString("\n//YyNext(state,c) is given the current state and input character and", true);
		WriteString("\n//evaluates to the next state.\n", true);
		WriteString("\tprivate int YyNext(int state, int c) {\n", false);
		String str = String.format("\t\treturn %s[state][c];\n", dtranName);
		WriteString(str, false);
		WriteString("\t}\n", false);
	}
	
	private void PrintYyAccept(int row, Accept[] accepts) {
		WriteString("\n//The yyAccept array has two purposes. If yyAccept[i] is 0 then state", true);
		WriteString("\n//i is nonaccepting. If it's nonzero then the number determines whether", true);
		WriteString("\n//the string is anchored, 1=anchored at start of line, 2=at end of", true);
		WriteString("\n//line, 3=both, 4=line not anchored\n", true);
		WriteString("\tprivate int[] yyAccept =\n", false);
		WriteString("\t{\n", false);
		String str;
		for (int i = 0; i < row; ++i) {
			if (null == accepts[i].acceptStr)
				WriteString("\t\t0  ", false);
			else {
				str = String.format("\t\t%-3d", NONE != accepts[i].anchor ? accepts[i].anchor : 4);
				WriteString(str, false);
			}
			str = String.format("%c    /* State %-3d */\n", row - 1 == i ? ' ' : ',', i);
			WriteString(str, false);
		}
		WriteString("\t};\n\n", false);	
		DefNext();
		TemplateInside();
		for (int i = 0;  i < row; ++i) {
			if (null != accepts[i].acceptStr) {
				str = String.format("\t\t\t\t\tcase %d:\t\t\t\t\t/* State %-3d */\n", i, i);
				WriteString(str, false);
				if (!noLines) {
					str = String.format("//line %d \"%s\"\n", accepts[i].lineno, Globals.lexDefFileName);
					WriteString(str, false);
				}
				WriteString("\t\t\t\t\t" + accepts[i].acceptStr + "\n", false);
				WriteString("\t\t\t\t\tbreak;\n", false);
			}
		}
		TemplateInside();
	}
	
	private boolean ProcessBody() {
		RegExpInput regInput = new RegExpInput(lexDefReader, "get_expr");
		Thomposon thom       = new Thomposon(regInput, table);
		//调试Advance使用
//		Token tok;
//		while (Token.END_OF_INPUT != (tok = thom.Advance())) {
//			System.out.print(tok);
//			System.out.println(thom.GetCurLexeme());
//		}
		Nfa start           = thom.BuildNfa();
		thom.PrintNfa();
		MinDfa min          = new MinDfa(); 
		DtranUnion dfaUnion = min.Minimize(start);	
		if (!noHeader)
			PrintHeader(dfaUnion.dtran, dfaUnion.accepts);		
		if (!TemplateFirstPart())
			return false;			
		String str = String.format("\tprivate int[][] %s =\n", dtranName);
		WriteString(str, false);
		PrintYynext(dfaUnion.dtran, dfaUnion.accepts.length, MAXCHAR);		
		PrintYyAccept(dfaUnion.accepts.length, dfaUnion.accepts);
		return true;
	}
	
	private void ProcessTail() {
		Vector<Integer> inputVec = GetLine();
		String str;
		if (!noLines) {
			str = String.format("//line %d \"%s\"\n", Globals.actualLineno + 1, Globals.lexDefFileName);
			WriteString(str, false);
		}
		while (true) {
			inputVec = GetLine();
			if (!inputVec.isEmpty()) 
				inputVec.add((int)'\n');
			else
				break;
			for (int i = 0; i < inputVec.size(); ++i)
				WriteChar(inputVec.get(i));
		}
	}
	
	private void NewMacro(Vector<Integer> inputVec) {
		table.InsertMacro(inputVec);
	}
	
	public void ProcessLexFile(String[] args, int argc) {
		int index = 0;
		char cur;
		for (int i = argc; i < args.length && '-' == args[argc].charAt(index); ++i) {
			++index;
			while (index < args[argc].length()) {
				cur = args[argc].charAt(index);
				switch (cur) {
				case 'h':	noHeader   = true;	
							break;
				case 'l': 	noLines = true;
							break;
				case 'm':	templateFileName = args[argc + 1];
							break;
				default:	System.err.printf("-%c illegal argument\n", cur);
							break;
				}
				++index;
			}
			index = 0;
		}
		if (!ProcessHead())
			return;
		if (!ProcessBody())
			return;		
		ProcessTail();
		CloseFile();
	}
	
	public boolean OpenFile() {
		//打开输入文件，即LEX定义文件
		File inputFile = new File(Globals.lexDefFileName);
		if (!inputFile.exists()) {
			System.err.println("Can't not open file:" + Globals.lexDefFileName + " " + Utils.GetErrPositon());
			return false;
		}
		else {
			try {
				lexDefReader = new BufferedReader(new FileReader(Globals.lexDefFileName));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}		
		}
		//打开输出文件，LEX输出文件Lexyy.java
		try {
			lexOutWriter = new BufferedWriter(new FileWriter(Globals.lexOutFileName));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void CloseFile() {
		try {
			lexDefReader.close();
			lexOutWriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Lex() {
		table = new MacroTable();
	}
}
