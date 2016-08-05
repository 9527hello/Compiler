package lib;

import java.util.ArrayList;

public class ComplementSet {
	private ArrayList<Integer> compleList; //�����󲹼���ȫ��
	private ArrayList<Integer> list; //���ϱ���
	private ArrayList<Integer> printList; //�������մ�ӡ�ļ���
	private boolean complement = false; //�Ƿ��󲹼���־
	private int maxSize; //�����Ĵ�С
	private int readIndex = -1;
	
	private void CalculateCompleSet() {
		if (null == compleList) {
			compleList = new ArrayList<Integer>();
			for (int i = 0; i < maxSize; ++i) //����Ҫprint��ʱ�������
				compleList.add(i);            //��ʡNfa�ռ�
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
