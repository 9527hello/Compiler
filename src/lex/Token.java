package lex;

//�ʷ��Ǻ�
public enum Token {
	EOS, //���������LexFileInput�д���õ��ģ�����Advance�����հ׷�ʱ���ɣ���ʱcurLexeme��ֵΪ'\0'
	ANY, //*
	CCL_START, //[
	CCL_END, //]
	OPEN_CURLY, //{
	CLOSE_CURLY, //}
	OPEN_PAREN, //(
	CLOSE_PAREN, //)
	CLOSURE, //*
	DASH, //-
	END_OF_INPUT, //�ļ�������־
	L, //�ַ�
	OPTIONAL, //?
	OR, //|
	PLUS_CLOSE, //+
	AT_BOL, //^
	AT_EOL;	//$
}
