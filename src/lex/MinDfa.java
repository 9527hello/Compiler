package lex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

class EpsilonUnion {
	public Vector<Nfa> set;
	public int lineno;
	public String accept;
	public int anchor;
}

class UnmarkUnion {
	public Dfa state;
	public int index;
}

public class MinDfa {
	private final int EPSILON  = -1;
	private final int CCL      = -2;
	private final char MAXCHAR = 128;
	private final int DFAMAX   = 255;
	private final int F        = -1;
	private int lastMarked     = 0;
	
	private EpsilonUnion EpsilonClosure(Vector<Nfa> nfaSet) {	
		Stack<Nfa> stack = new Stack<Nfa>();		
		for (int i = 0; i < nfaSet.size(); ++i) 
			stack.push(nfaSet.get(i));
		Nfa curState;
		int lineno    = 0;
		int anchor    = 0;
		String accept = null;
		int acceptNum = Integer.MAX_VALUE;
		
		while (!stack.isEmpty()) {
			curState = stack.pop(); //һ��DFA���ܰ������NFA�Ľ���״̬��ѡ�����б����С����Ϊ����״̬��
			//��һ�����������ͬʱ�����������ʽʶ��ǰ������ȼ�Խ�ߣ��Ƚ���
			if (null != curState.acceptStr && curState.num < acceptNum) { 
				acceptNum = curState.num;
				lineno    = curState.lineno;
				accept    = curState.acceptStr;
				anchor    = curState.anchor;
			}
			if (EPSILON == curState.edge) {
				if (null != curState.next && !nfaSet.contains(curState.next)) {
					nfaSet.add(curState.next);
					stack.push(curState.next);
				}
				if (null != curState.next2 && !nfaSet.contains(curState.next2)) {
					nfaSet.add(curState.next2);
					stack.push(curState.next2);
				}
			}
		}
		EpsilonUnion retUnion = new EpsilonUnion();
		retUnion.lineno       = lineno;
		retUnion.accept       = accept;
		retUnion.anchor       = anchor;
		retUnion.set          = nfaSet;
		return retUnion;
	}
	
	private Vector<Nfa> Move(Vector<Nfa> nfaSet, char c) {
		Vector<Nfa> set = new Vector<Nfa>();
		Nfa curState;
		for (int i = 0; i < nfaSet.size(); ++i) {
			curState = nfaSet.get(i);
			if ((int)c == curState.edge || (CCL == curState.edge && curState.set.InSet((int)c))) 
				set.add(curState.next);
		}
		return set;
	}
	
	private UnmarkUnion GetUnmarkedState(Vector<Dfa> states) {
		Dfa retState = null;
		Dfa tmp;
		for (; lastMarked < states.size(); ++lastMarked) {
			tmp = states.get(lastMarked);
			if (!tmp.mark) {
				retState = tmp;
				break;
			}
		}
		UnmarkUnion retUnion = null;
		if (null != retState) {
			retUnion       = new UnmarkUnion();
			retUnion.state = retState;
			retUnion.index = lastMarked;
		}
		return retUnion;
	}
	
	private boolean NfaSetEqual(Vector<Nfa> first, Vector<Nfa> second) {
		if (first.size() != second.size())
			return false;
		else {
			int i;
			ArrayList<Integer> firstNum  = new ArrayList<Integer>();
			ArrayList<Integer> secondNum = new ArrayList<Integer>();
			for (i = 0; i < first.size(); ++i) {
				firstNum.add(first.get(i).num);
				secondNum.add(second.get(i).num);
			}
			
			Collections.sort(firstNum);
			Collections.sort(secondNum);
			
			for (i = 0; i < firstNum.size(); ++i) 
				if (firstNum.get(i).intValue() != secondNum.get(i).intValue())
					break;
			return i == firstNum.size();
		}
	}
	
	private int InDfaStates(Vector<Dfa> dfaStates, Vector<Nfa> cmp) {
		Dfa state;
		int i;
		for (i = 0; i < dfaStates.size(); ++i) {
			state = dfaStates.get(i);
			if (NfaSetEqual(state.set, cmp))
				break;
		}
		if (i == dfaStates.size()) 
			return -1;
		else
			return i;
	}
	
	private DtranUnion MakeDtran(Nfa start) {
		Vector<Dfa> dfaStates = new Vector<Dfa>();
		int[][] tmpDtran      = new int[DFAMAX][MAXCHAR];
		//�����ʼ�ڵ�
		Vector<Nfa> nfaStates = new Vector<Nfa>();	
		nfaStates.add(start);
		Dfa dfaState            = new Dfa();	
		EpsilonUnion startUnion = EpsilonClosure(nfaStates); 
		dfaState.set            = startUnion.set;
		dfaState.acceptStr      = startUnion.accept;
		dfaState.mark           = false;
		dfaStates.add(dfaState);
		
		Dfa curState, newState;
		UnmarkUnion unmark;
		EpsilonUnion epsilon;
		int nextState;
		int lineno = 0;
		int anchor = 0;
		String accept;
	    
		while (null != (unmark = GetUnmarkedState(dfaStates))) {
			curState      = unmark.state;
			curState.mark = true;
			//��һ������ļ�������c��ѭ��˳��ᵼ�����ɵ�DFA�ڵ㲻ͬ(�����ϵĳ���Ľ����ͬ)����Ҳ�ǶԵ�
			//����Ϊ����֤������Բ����˸�������ͬ��ѭ��˳��
			for (int c = MAXCHAR - 1; c >= 0; --c) {
				accept    = null;
				nextState = F;
				Vector<Nfa> nfaSet = Move(curState.set, (char)c);
				if (!nfaSet.isEmpty()) {
					epsilon = EpsilonClosure(nfaSet);
					nfaSet  = epsilon.set;
					lineno  = epsilon.lineno;
					accept  = epsilon.accept;
					anchor  = epsilon.anchor;
				}
				if (nfaSet.isEmpty())
					nextState = F;
				else if (-1 != (nextState = InDfaStates(dfaStates, nfaSet))) 
					nfaSet.clear();
				else {
					newState           = new Dfa();
					newState.set       = nfaSet;
					newState.lineno    = lineno;
					newState.acceptStr = accept;
					newState.anchor    = anchor;
					newState.mark      = false;
					nextState          = dfaStates.size();
					dfaStates.add(newState);
				}
				tmpDtran[unmark.index][c] = nextState; 
			}
		}
		
		int[][] dtran         = new int[dfaStates.size()][MAXCHAR];
		Accept[] stateAccepts = new Accept[dfaStates.size()];
		Dfa tmpDfa;
		for (int i = 0; i < dfaStates.size(); ++i) {
			dtran[i]                  = tmpDtran[i];   
			tmpDfa                    = dfaStates.get(i);
			stateAccepts[i]           = new Accept();
			stateAccepts[i].lineno    = tmpDfa.lineno;
			stateAccepts[i].acceptStr = tmpDfa.acceptStr;
			stateAccepts[i].anchor    = tmpDfa.anchor;
		}		
		PrintDFAStates(dfaStates); //����ʱʹ��		
		PrintAccept(stateAccepts); //����ʱʹ��		
		PrintDtran(dtran); //����ʱʹ��	
		DtranUnion retUnion = new DtranUnion();
		retUnion.dtran      = dtran;
		retUnion.accepts    = stateAccepts;
		return retUnion;
	}
	
	private boolean TwoAcceptStringEqual(String a, String b) {
		if (null == a) {
			return null == b ? true : false;
		}
		else {
			if (null == b)
				return false;
			else 
				return 0 == a.compareTo(b) ? true : false;
		}
	}
	
	private void InitGroups(Accept[] accepts, Vector<Vector<Integer>> groups, int[] inverseGroup) {
		int i, j;
		for (i = 0; i < inverseGroup.length; ++i)
			inverseGroup[i] = F;		
        //�����ʼ�������Ƚ���ͬacceptString�ֵ�һ�飬��ͬ�Ļ��ֵ���ͬ������
		for (i = 0; i < accepts.length; ++i) {
			for (j = i - 1; j >= 0; --j) 
				if (TwoAcceptStringEqual(accepts[i].acceptStr, accepts[j].acceptStr)) {
					Vector<Integer> curGroup = groups.get(inverseGroup[j]);
					curGroup.add(i);
					inverseGroup[i] = inverseGroup[j];
					break;
				}
			if (j < 0) {
				Vector<Integer> newGroup = new Vector<Integer>();
				newGroup.add(i);	
				inverseGroup[i] = groups.size();
				groups.add(newGroup);	
			}
		}
	}
	
	private DtranUnion FixDtran(Vector<Vector<Integer>> groups, int[] inverseGroup, int[][] dtran, Accept[] accepts) {
		int[][] fixDtran    = new int[groups.size()][MAXCHAR];
		Accept[] fixAccepts = new Accept[groups.size()];
		int curStateNum, nextStateNum; 
		for (int i = 0; i < groups.size(); ++i) {
			curStateNum = groups.get(i).get(0);
			fixAccepts[i] = accepts[curStateNum];
			for (char c = 0; c < MAXCHAR; ++c) { 
				nextStateNum   = dtran[curStateNum][(int)c];
				fixDtran[i][c] = F == nextStateNum ? F : inverseGroup[nextStateNum];
			}
		}
		DtranUnion retUnion = new DtranUnion();
		retUnion.dtran      = fixDtran;
		retUnion.accepts    = fixAccepts;
		return retUnion;
	}
	
	public DtranUnion Minimize(Nfa start) {
		DtranUnion dtranUnion = MakeDtran(start);
		Vector<Vector<Integer>> groups = new Vector<Vector<Integer>>();
		int[] inverseGroup   = new int[DFAMAX];
		InitGroups(dtranUnion.accepts, groups, inverseGroup);
	    int[][] dtran = dtranUnion.dtran;
		int first, next, tranFirst, tranNext;
		int oldGroupsNum, newGroupsNum = groups.size();
		
		do
		{
			oldGroupsNum = newGroupsNum;		
			//��Ϊֻ�漰�������ĩβ����Ԫ������������û�����⣬groups.size()����֮�ı�
			for (int i = 0; i < groups.size(); ++i) {
				Vector<Integer> curGroup = groups.get(i);
				if (curGroup.size() <= 1)
					continue;
				Vector<Integer> newGroup    = new Vector<Integer>();
				Iterator<Integer> iterGroup = curGroup.iterator();
				first                       = (int)iterGroup.next();
				//Ҫɾ��ָ��Ԫ�أ�����ֻ����iteration������
				while (iterGroup.hasNext()) {
					next = (int)iterGroup.next();
					//��һ������ļ�������c��ѭ��˳��ᵼ�����ɵ�Group�ڵ㲻ͬ(�����ϵĳ���Ľ����ͬ)����Ҳ�ǶԵ�
					//����Ϊ����֤������Բ����˸�������ͬ��ѭ��˳��
					//ע�������ѭ��˳��Ҫ�������MakeDtran��ѭ��˳����ͬ
					for (int c = MAXCHAR - 1; c >= 0; --c) {
						tranFirst = dtran[first][c];
						tranNext  = dtran[next][c];
						if (tranFirst != tranNext && (F == tranFirst || F == tranNext ||
								inverseGroup[tranFirst] != inverseGroup[tranNext])) {
							iterGroup.remove();
							newGroup.add(next);
							inverseGroup[next] = newGroupsNum;
							break;
						}
					}
				}
				if (!newGroup.isEmpty()) {
					groups.add(newGroup);
					++newGroupsNum;
				}
			}
 		} while (oldGroupsNum != newGroupsNum);
		PrintMinDfa(groups); //����ʱʹ��
		DtranUnion fixDtranUnion = FixDtran(groups, inverseGroup, dtranUnion.dtran, dtranUnion.accepts);
		PrintDtran(fixDtranUnion.dtran);
		PrintAccept(fixDtranUnion.accepts);
		return fixDtranUnion;
	}
	
	public void PrintMinDfa(Vector<Vector<Integer>> groups) {
		for (int i = 0; i < groups.size(); ++i) {
			System.out.printf("group %d:", i);
			Vector<Integer> curGroup = groups.get(i);
			for (int j = 0; j < curGroup.size(); ++j) 
				System.out.printf("%d ", curGroup.get(j));
			System.out.print("\n");
		}
	}
	
	public void PrintDFAStates(Vector<Dfa> dfaStates) {
		for (int i = 0; i < dfaStates.size(); ++i) {
			System.out.printf("DFA state %d:", i);			
			for (int j = 0; j < dfaStates.get(i).set.size(); ++j) 
				System.out.printf("%d ", dfaStates.get(i).set.get(j).num);
			System.out.print("\n");
		}
	}
	
	public void PrintDtran(int[][] dtran) {
		System.out.print("state   num");
		for (char c = 0; c < MAXCHAR; ++c) {
			if (c < ' ')
				System.out.printf("%4c ", ' ');
			else
				System.out.printf("%4c ", c);
		}
		System.out.print("\n");
		for (int i = 0; i < dtran.length; ++i) {
			System.out.printf("state %4d:", i);
			for (int j = 0; j < dtran[i].length; ++j)
				System.out.printf("%4d ", dtran[i][j]);
			System.out.print("\n");
		}
	}
	
	public void PrintAccept(Accept[] accepts) {
		int count = 0;
		for (int i = 0; i < accepts.length; ++i) {
			if (null != accepts[i].acceptStr) {
				System.out.printf("state %d:", i);
				System.out.println(accepts[i].acceptStr);
				++count;
			}
		}
		System.out.printf("different accept strings are %d\n", count);
	}
}
