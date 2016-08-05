package lib;

import java.util.ArrayList;

public class ComplementSet {
	private ArrayList<Integer> compleList; //用来求补集的全集
	private ArrayList<Integer> list; //集合本身
	private ArrayList<Integer> printList; //用来最终打印的集合
	private boolean complement = false; //是否求补集标志
	private int maxSize; //补集的大小
	private int readIndex = -1;
	
	private void CalculateCompleSet() {
		if (null == compleList) {
			compleList = new ArrayList<Integer>();
			for (int i = 0; i < maxSize; ++i) //在需要print的时候输出，
				compleList.add(i);            //节省Nfa空间
		}
	}
	
	public ComplementSet(int size) {
		list       = new ArrayList<Integer>();
		maxSize    = size;
	}
	
	public void Complement() {
		complement = !complement;
	}
	
	public void Add(int item) {
		list.add(item);
	}
	
	public int GetSetItem() {
		int retVal = -1;
		
		if (-1 == readIndex) {
			if (!complement)
				printList = list;
			else {
				CalculateCompleSet();
				compleList.removeAll(list);
				printList = compleList;
			}
			readIndex = 0;
		}

		if (readIndex != printList.size()) {
			retVal = printList.get(readIndex);
			++readIndex;
		}
		return retVal;
	}
	
	public boolean InSet(int item) {
		if (!complement)
			printList = list;
		else {
			CalculateCompleSet();
			compleList.removeAll(list);
			printList = compleList;
		}	
		
		return printList.contains(item);
	}
	
	public void PrintSet() {
		if (!complement) 
			printList = list;
		else {
			CalculateCompleSet();
			compleList.removeAll(list);
			printList = compleList;
		}
		for (int i = 0; i < printList.size(); ++i) {
			System.out.print(printList.get(i));
			System.out.print(" ");
		}
		System.out.print("\n");
	}
}
