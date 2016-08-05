package lex;

import java.io.BufferedReader;
import java.util.Vector;

//只负责处理正则表达式相关的部分
public class RegExpInput {
	private String inputFuncName;
	private BufferedReader inputReader;
	private int lookahead = 0;
	
	private int GetLine(Vector<Integer> inputVec) {
		try {
			int tmp;
			while (true) {
				tmp       = lookahead;
				lookahead = inputReader.read();
				if (-1 == tmp || '\n' == tmp)
					break;
				if (0 != tmp)
					inputVec.add(tmp);
			} 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lookahead;	
	}
	
	public RegExpInput(BufferedReader reader, String functName) {
		inputReader   = reader;
		inputFuncName = functName;
	}
    
	//所有方法均是返回自定义的Vector<Byte>缓冲区
	public Vector<Integer> GetInput() {
		if (0 == inputFuncName.compareTo("get_expr")) 
			return GetExpr();
		//默认按简单方式返回
		return GetSimpleInput();
	}
	
	//获得单个表达式的字节缓冲区,如果表达式的action部分存在多行，则后面的行也添加上去一起返回
	public Vector<Integer> GetExpr() {
		Vector<Integer> inputVec = new Vector<Integer>();
		Globals.lineno = Globals.actualLineno;
		if ('%' != lookahead) {
			while (true) {
				if (-1 == (lookahead = GetLine(inputVec)))
					break;
				++Globals.actualLineno;	
				if (Character.isWhitespace((char)inputVec.get(0).intValue())) { //跳过空白行
					inputVec.clear();
					continue;
				}
				
				if (!Character.isWhitespace((char)lookahead)) 
						break;
			}
			inputVec.add((int)'\n');
		}
		//用来测试获得的表达式
//		for (int i = 0; i < inputVec.size(); ++i) 
//			System.out.print((char)inputVec.get(i).intValue());
		return inputVec;
	}
	
	//直接读取到EOF
	public Vector<Integer> GetSimpleInput() {
		Vector<Integer> inputVec = new Vector<Integer>();
		try {
			int got;
			while (-1 != (got = inputReader.read())) {
				for (int i = 0; i < got; ++i)
					inputVec.add(got);
			}	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
		return inputVec;
	}
}
