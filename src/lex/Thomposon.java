package lex;

import java.util.Stack;
import java.util.Vector;
import lib.ComplementSet;
import lib.Esc;

class NfaUnion {
	public Nfa start;
	public Nfa end;
}

//��ջջ֡
class StackItem {
	public Vector<Integer> vec;
	public int index;
}

//ErrMsg��NFAͨ����ʵ��
public class Thomposon {	
	//������
	private final int EPSILON = -1;
	private final int CCL     = -2;
	private final int CCLSIZE = 128;
	//anchor����
	private final int NONE    = 0;
	private final int START   = 1;
	private final int END     = 2;
	
	private Token curToken; //��ǰ����
	private char curLexeme; //��ǰ����
	private Vector<Integer> inputVec; //���ݶ�ȡ������
	private MacroTable macroTable; //չ�����ӳ���
	private RegExpInput regInput;   //������ʽ������
	private int readIndex = 0; //��ʾinput������λ��(Ŀǰ�����ļ���СΪInteger.MAXVALUE)
	private Vector<Nfa> nfaStates; //NFAģ���еĽڵ㼯��
	private boolean inquote; //�Ƿ��ڴ���'"'
	private Stack<StackItem> macroStack; //��չ���������õ���ջ
	private Vector<Integer> acceptStrings; //LEX�ļ������е�action
	
	//��Ӧascii��
	private Token[] tokenMap = new Token[] {
//  ǰ32���ַ�Ϊ�����ַ�������Ų�û��ʲôʵ�ʹ�ϵ��ֻ��һ�ֱ�ʾ����
//	^@		^A		^B		^C		^D		^E		^F		^G		^H	
	Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,
//  ^I		^J		^K		^L		^M		^N		^O		^P		^Q
	Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,
//	^R		^S		^T		^U		^V		^W		^X		^Y		^Z
	Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,
//	^[		^\		^]		^^		^_
	Token.L,Token.L,Token.L,Token.L,Token.L,
//	SPACE	!		"		#		$				%		&		'	
	Token.L,Token.L,Token.L,Token.L,Token.AT_EOL,   Token.L,Token.L,Token.L,		       
//	(						)						*				+		
	Token.OPEN_PAREN, 		Token.CLOSE_PAREN, 		Token.CLOSURE, 	Token.PLUS_CLOSE,  
//	,		-				.				/	                                       
	Token.L,Token.DASH,		Token.ANY,		Token.L,                                   
//	0		1		2		3		4		5		6		7		8	
	Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,           
//	9	
	Token.L,                                                                           
//	:		;		<		= 		> 		?				@
	Token.L,Token.L,Token.L,Token.L,Token.L,Token.OPTIONAL,	Token.L,                   
//	A		B		C		D		E		F		G		H		I 
	Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,           //9
//	J		K		L		M		N		O		P		Q		R
	Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,           //9
//	S		T		U		V		W		X		Y		Z
	Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,                   //8
//	[				\		]				^				_		`
	Token.CCL_START,Token.L,Token.CCL_END, 	Token.AT_BOL, 	Token.L,Token.L,           //2
//	a		b		c		d		e		f		g		h		i
	Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,
//	j		k		l		m		n		o		p		q		r
	Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,
//	s		t		u		v		w		x		y		z		
	Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,Token.L,
//	{						|				}						~
	Token.OPEN_CURLY,		Token.OR,		Token.CLOSE_CURLY,		Token.L,
//	DEL
	Token.L
	};
	
	private Nfa NewNfa() {
		Nfa state = new Nfa();
		nfaStates.add(state);
		return state;
	}
	
	private void DiscardNfa(int num) {
		Nfa.Discard(num);
		for (int i = 0; i < nfaStates.size(); ++i) {
			Nfa state = nfaStates.get(i);
			if (state.num == num)
				nfaStates.remove(i);
		}			
	}
	
	private boolean Match(Token cmp) {
		return curToken == cmp;
	}
	
	private Vector<Integer> GetMacro(Vector<Integer> inputVec) {
		String key = "";
		++readIndex; //����'{'
		char cur = (char)inputVec.get(readIndex).intValue();
		while (readIndex < inputVec.size()) {
			if ('}' == cur) 
				break;
			key += cur;
			++readIndex;
			cur = (char)inputVec.get(readIndex).intValue();
		}
		++readIndex; //����'}'
		StackItem item = new StackItem();
		item.index     = readIndex;
		item.vec       = inputVec;
		macroStack.push(item);
		inputVec  = macroTable.GetMacro(key);
		readIndex = 0;
		return inputVec;
	}
	
	//ֻ����ĳ��rule��expr���ֵ�token������lexeme��ʣ�µ�action��saveȥ����
	public Token Advance() {	
		char cur;
		if (Token.EOS == curToken) {
			do {
					inputVec = regInput.GetInput();
					readIndex = 0;
					if (inputVec.isEmpty()) {
						curToken  = Token.END_OF_INPUT;
						curLexeme = '\0';
						return curToken;
					}
					while (Character.isWhitespace((char)inputVec.get(readIndex).intValue())) 
						++readIndex;
			} while (inputVec.size() == readIndex);
		}
	
		while (inputVec.size() == readIndex) {
			if (!macroStack.isEmpty()) {
				StackItem item = macroStack.pop();
				inputVec       = item.vec;
				readIndex      = item.index;
				continue;
			}
			
			curToken  = Token.EOS;
			curLexeme = '\0';
			return curToken;
		}
		
		cur = (char)inputVec.get(readIndex).intValue();
		
		if (!inquote) {
			while ('{' == cur) {
				inputVec = GetMacro(inputVec);
				cur      = (char)inputVec.get(readIndex).intValue();
			}
		}
		
		if ('"' == cur) {//˫������������ÿ���ַ�����Ϊ����ͨ�ַ� 
			inquote = !inquote;
			++readIndex;
			if (inputVec.size() == readIndex) {
				curToken  = Token.EOS;
				curLexeme = '\0';
				return curToken;
			}
			else 
				cur = (char)inputVec.get(readIndex).intValue();
		}
		
		boolean sawEsc = '\\' == cur;
		
		if (!inquote) {
			if (Character.isWhitespace(cur)) {
				curToken  = Token.EOS;
				curLexeme = '\0';
				return curToken;
			}
			Esc esc   = new Esc(this);
			curLexeme = esc.ESC(inputVec, readIndex);
		}
		else {
			if (sawEsc && '"' == inputVec.get(1)) {//'\"'ֻ��������ײ�
				readIndex += 2;
				curLexeme = '"';
			}
			else {
				curLexeme = cur;
				++readIndex;
			}
		}
		curToken = (inquote || sawEsc) ? Token.L : tokenMap[curLexeme];
		return curToken;
	}
	
	private Nfa Save(Nfa state) {
		state.acceptStr = "";
		state.lineno    = Globals.lineno;
		int cur;
		if (inputVec.size() != readIndex) {
			state.acceptStart = acceptStrings.size();
			if ('|' != inputVec.get(readIndex)) {
				while (readIndex < inputVec.size()) { 
					cur = inputVec.get(readIndex);
					//ĩβ��'\r'��'\n'�����뵽acceptString��
					if (('\r' == cur || '\n' == cur) && 
						((readIndex + 1) == inputVec.size() || 
						 (readIndex + 2) == inputVec.size() ||
						 (readIndex + 3) == inputVec.size() ||
						 (readIndex + 4) == inputVec.size())) {
						++readIndex;
						continue;
					}
					else {
						acceptStrings.add(cur);
						++readIndex;
					}
				}
				//���ڶ��rule���ܹ���һ��action��acceptStart��־��ǰ��ѡ��
				//��action����ʼλ�ã��������ǰ����'|'����acceptStartΪ��һ
				//��action��λ�ã�����'\0'����־��ǰaction�Ľ���
				acceptStrings.add((int)'\0');
			}
		}
		return state;
	}
	
	//�ǰ�����first���ϵ��㷨ȥ�����
	//DASH �ھ���ı��ʽ�����д���
	//OPEN_CURLY,CLOSE_CURLY�Ǻ��滻�����ú�ջʵ�֣�����������ʱ����
	private boolean FirstInCat(Token tok) { //ErrMsg�ݲ�����
		switch (tok) {
		case EOS: //�ַ���ȥ������ʱ������־���������˿ո�
		case END_OF_INPUT: //�ļ���ȡҲ�ǽ�����־
		case CCL_END:
		case CLOSE_PAREN:
		case CLOSURE:
		case OPTIONAL:
		case OR:
		case PLUS_CLOSE:
		case AT_BOL:
		case AT_EOL:
			 				return false;
		default:			return true;
		}
	}
	
	private ComplementSet DoDash(ComplementSet set) {
		char first = 0; //������if��һ����֧��ErrMsg�ݲ�����
		
		while (!Match(Token.EOS) && !Match(Token.CCL_END)) {
			if (!Match(Token.DASH)) { //���û������'-'
				first = curLexeme; //�����һ���ַ�
				set.Add((int)curLexeme); //���뼯��
			}  
			else {
				Advance(); //����'-';
				if (!Match(Token.CCL_END)) {
					for (int i = first + 1; i <= curLexeme; ++i)
						set.Add(i);
				}
			}
			Advance();
		}
		return set;
	}
	
	private NfaUnion Term() {
		Nfa start, end;
		start = end = null;
		if (Match(Token.OPEN_PAREN)) {
			Advance();
			NfaUnion exprUnion = Expr();
			start              = exprUnion.start;
			end                = exprUnion.end;
			if (Match(Token.CLOSE_PAREN)) 
				Advance();
		}
		else {
			start      = NewNfa();
			end        = NewNfa();
			start.next = end;
			if (!(Match(Token.ANY) || Match(Token.CCL_START))) {
				start.edge = (int)curLexeme;
				Advance();
			}
			else {
				start.edge = CCL;
				start.set  = new ComplementSet(CCLSIZE);
				if (Match(Token.ANY)) {					
					start.set.Add((int)'\r');
					start.set.Add((int)'\n');
					start.set.Complement();
				}
				else {
					Advance(); //����[
					if (Match(Token.AT_BOL)) {
						Advance();
						start.set.Add((int)'\r');
						start.set.Add((int)'\n');
						start.set.Complement();						
					}
					if (!Match(Token.CCL_END))
						start.set = DoDash(start.set);
					else //�Ž�ȥ����հ��ַ�����ascii��ǰ���32���հ��ַ�
						for (int i = 0; i < ' '; ++i)
							start.set.Add(i);
				}
				Advance(); //����'.'��']'
			}
		}
		NfaUnion retUnion = new NfaUnion();
		retUnion.start    = start;
		retUnion.end      = end;
		return retUnion;		
	}
	
	private NfaUnion Factor() {
		NfaUnion termUnion = Term();
		Nfa start, end;
		start = end = null;
		if (Match(Token.CLOSURE) || Match(Token.PLUS_CLOSE) || Match(Token.OPTIONAL)) {
			start              = NewNfa();
			end                = NewNfa();
			start.next         = termUnion.start;
			start.edge         = EPSILON;
			termUnion.end.next = end;
			termUnion.end.edge = EPSILON;	
			
			if (Match(Token.CLOSURE) || Match(Token.PLUS_CLOSE)) 
				termUnion.end.next2 = termUnion.start;
			if (Match(Token.CLOSURE) || Match(Token.OPTIONAL))
				start.next2 = end;
			Advance();
		}

		NfaUnion retUnion;
		if (null == start) 
			retUnion = termUnion; 
		else {
			retUnion       = new NfaUnion();
			retUnion.start = start;
			retUnion.end   = end;
		}
		return retUnion;
	}
	
	private NfaUnion CatExpr() {
		NfaUnion firstUnion, secondUnion;
		firstUnion = Factor();
		
		while (FirstInCat(curToken)) {
			secondUnion              = Factor();
			//����һ��Factor��β�ڵ�������һ��Factor���׽ڵ����һ���ڵ�
			//��������һ��Factor���׽ڵ�
			firstUnion.end.next      = secondUnion.start.next;
			firstUnion.end.next2     = secondUnion.start.next2;
			firstUnion.end.edge      = secondUnion.start.edge;
			firstUnion.end.set       = secondUnion.start.set;
			firstUnion.end.lineno    = secondUnion.start.lineno;
			firstUnion.end.acceptStr = secondUnion.start.acceptStr;
			//ɾ��secondUnion.start
			DiscardNfa(secondUnion.start.num);
			//����endΪ��һ��Factor��end
			firstUnion.end           = secondUnion.end;
		}
		NfaUnion retUnion = firstUnion;
		return retUnion;
	}
	
	//ÿ�����ʽ�����ӱ��ʽ(����factor, term֮��)Ӧ�ö���һ����ʼ�ڵ��һ�������ڵ�
	private NfaUnion Expr() {
		NfaUnion firstUnion, secondUnion;
		Nfa start, end;
		firstUnion = CatExpr();
		start      = end = null;
		
		while (Match(Token.OR)) {
			Advance();
			start                = NewNfa();
			secondUnion          = CatExpr();
			start.edge           = EPSILON;
			start.next           = firstUnion.start;
			start.next2          = secondUnion.start;
			firstUnion.start     = start;
			end                  = NewNfa();
			firstUnion.end.next  = end;
			firstUnion.end.edge  = EPSILON;
			secondUnion.end.next = end;
			secondUnion.end.edge = EPSILON;
			firstUnion.end       = end;
		}
		NfaUnion retUnion;
		if (null == start) 
			retUnion = firstUnion;
		else {
			retUnion       = new NfaUnion();
			retUnion.start = start;
			retUnion.end   = end;
		}
		return retUnion;
	}
	
	private Nfa Rule() {
		Nfa start  = null;
		Nfa end    = null;
		int anchor = NONE;
		NfaUnion retUnion;
		if (Match(Token.AT_BOL)) {
			start      = NewNfa();
			start.edge = '\n';
			anchor    |= START;
			Advance();
			retUnion   = Expr();
			start.next = retUnion.start;
			end        = retUnion.end;
		}
		else {
			retUnion = Expr();
			start    = retUnion.start;
			end      = retUnion.end;
		}
		
		if (Match(Token.AT_EOL)) {
			Advance();
			end.next = NewNfa();
			end.edge = CCL;
			end.set  = new ComplementSet(CCLSIZE);
			end.set.Add((int)'\n');
			end.set.Add((int)'\r');
			end     = end.next;
			anchor |= END;
		}
		
		char cur = (char)inputVec.get(readIndex).intValue();
		while (inputVec.size() != readIndex && Character.isWhitespace(cur)) {
			cur = (char)inputVec.get(readIndex).intValue();
			++readIndex;
		}
		--readIndex; //�ָ�����һ���ǿհ��ַ�
		end        = Save(end);
		end.anchor = anchor;
		Advance(); //�����µ�rule,����EOS
		return start;
	}
		
	private Nfa Machine() {
		Nfa start, tmp;
		tmp      = start = NewNfa(); 
		tmp.next = Rule();
		tmp.edge = EPSILON;
		
		while (!Match(Token.END_OF_INPUT)) {
			tmp.next2 = NewNfa();
			tmp       = tmp.next2;
			tmp.edge  = EPSILON;
			tmp.next  = Rule();
		}
		return start;
	}
	
	private void PrintCCL(ComplementSet set) {
		System.out.print("[");
		int item;
		while (-1 != (item = set.GetSetItem())) {
			if (item < ' ')
				System.out.printf("^%c", item + '@');
			else
				System.out.printf("%c", item);
		}
		System.out.print("]");
	}
	
	private String GetNumString(Nfa state) {
		if (null == state)
			return "--";
		else 
			return Integer.toString(state.num);
	}
	
	//����������ܵ�action
	private void GetAcceptString() {
		Nfa state;
		int cur, index;
		for (int i = 0; i < nfaStates.size(); ++i) {
			state = nfaStates.get(i);
			index = state.acceptStart;
			if (-1 != state.acceptStart) {
				//��acceptStart��ʼ��'\0'��־Ϊ��ǰrule���ܵ�action
				while ('\0' != (cur = acceptStrings.get(index))) {
					state.acceptStr += (char)(cur);
					++index;
				}
			}
		}		
	}
	
	public char GetCurLexeme() {
		return curLexeme;
	}
	
	public Token GetCurToken() {
		return curToken;
	}
	
	public void IncreaseReadIndex() {
			++readIndex;
	}
	
	public Nfa BuildNfa() {
		curToken  = Token.EOS;
		inputVec  = new  Vector<Integer>();
		curLexeme = 0;
		Advance();
		Nfa start = Machine();
		GetAcceptString();
		return start;
	}
	
	public void PrintNfa() {
		System.out.printf("\n------------- NFA -------------\n");
		Nfa state;
		for (int i = 0; i < nfaStates.size(); ++i) {
			state = nfaStates.get(i);
			System.out.printf("NFA states %s ", Integer.toString(state.num));
			
			if (0 == state.num)
				System.out.print("(START STATE)");

			if (null == state.next) 
				System.out.print("(TERMINAL)");
			else {
				System.out.printf("--> %s ", GetNumString(state.next));
				System.out.printf("(%s) on ", GetNumString(state.next2));
				
				switch (state.edge) {
				case CCL:		PrintCCL(state.set);System.out.print(" ");	break;
				case EPSILON:	System.out.print("EPSILON ");	    		break;
				default:		System.out.print((char)(state.edge) + " ");			break;
				}
			}
			
			if (null != state.acceptStr)
				System.out.print(state.acceptStr);
			System.out.println(" ");
		}
		System.out.print("-------------------------------\n");
	}
	
	public Thomposon(RegExpInput input, MacroTable table) {
		regInput      = input;
		nfaStates     = new Vector<Nfa>();
		macroTable    = table;
		macroStack    = new Stack<StackItem>();
		//������������Advance����ص�get_exprʱ��ʼ��ʹ��
		curToken      = Token.EOS; 
		inputVec      = new Vector<Integer>();
		acceptStrings = new Vector<Integer>();
	}
}
