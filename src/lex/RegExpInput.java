package lex;

import java.io.BufferedReader;
import java.util.Vector;

//ֻ������������ʽ��صĲ���
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
    
	//���з������Ƿ����Զ����Vector<Byte>������
	public Vector<Integer> GetInput() {
		if (0 == inputFuncName.compareTo("get_expr")) 
			return GetExpr();
		//Ĭ�ϰ��򵥷�ʽ����
		return GetSimpleInput();
	}
	
	//��õ������ʽ���ֽڻ�����,������ʽ��action���ִ��ڶ��У���������Ҳ�����ȥһ�𷵻�
	public Vector<Integer> GetExpr() {
		Vector<Integer> inputVec = new Vector<Integer>();
		Globals.lineno = Globals.actualLineno;
		if ('%' != lookahead) {
			while (true) {
				if (-1 == (lookahead = GetLine(inputVec)))
					break;
				++Globals.actualLineno;	
				if (Character.isWhitespace((char)inputVec.get(0).intValue())) { //�����հ���
					inputVec.clear();
					continue;
				}
				
				if (!Character.isWhitespace((char)lookahead)) 
						break;
			}
			inputVec.add((int)'\n');
		}
		//�������Ի�õı��ʽ
//		for (int i = 0; i < inputVec.size(); ++i) 
//			System.out.print((char)inputVec.get(i).intValue());
		return inputVec;
	}
	
	//ֱ�Ӷ�ȡ��EOF
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
