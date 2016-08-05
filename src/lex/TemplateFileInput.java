package lex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import lib.Utils;

public class TemplateFileInput {
	private String templateFileName;
	private BufferedReader inputReader;
	
	public TemplateFileInput(String fileName) {
		templateFileName = fileName;
		inputReader      = null;
	}
	
	public boolean OpenFile() {
		//�������ļ�����LEX�����PAR�ļ�
		File inputFile = new File(templateFileName);
		if (!inputFile.exists()) {
			System.err.println("Can't not open file:" + templateFileName + " " + Utils.GetErrPositon());
			return false;
		}
		else {
			try {
				inputReader = new BufferedReader(new FileReader(templateFileName));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		return true;
	}
	
	public Vector<Integer> GetLine() {
		Vector<Integer> inputVec = new Vector<Integer>();
		int got;
		try {
			while (-1 != (got = inputReader.read())) {
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
}
