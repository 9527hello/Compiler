package lex;

//词法记号
public enum Token {
	EOS, //这个不是在LexFileInput中处理得到的，是在Advance遇到空白符时生成，此时curLexeme赋值为'\0'
	ANY, //*
	CCL_START, //[
	CCL_END, //]
	OPEN_CURLY, //{
	CLOSE_CURLY, //}
	OPEN_PAREN, //(
	CLOSE_PAREN, //)
	CLOSURE, //*
	DASH, //-
	END_OF_INPUT, //文件结束标志
	L, //字符
	OPTIONAL, //?
	OR, //|
	PLUS_CLOSE, //+
	AT_BOL, //^
	AT_EOL;	//$
}
