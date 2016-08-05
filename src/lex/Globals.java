package lex;

public class Globals {
	public static int actualLineno = 1; //实际的行号
	public static int lineno       = 1; //多行rule的首行行号
	public static String lexDefFileName; //lex定义文件名
	public static String lexOutFileName; //lex输出文件名，即lexyy.c
}
