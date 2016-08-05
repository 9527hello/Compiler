/*@A (C) 2016 Gachiman                                                      */
/* Lexical analyzer specification for C. This is a somewhat extended version
 * of the one in Appendix D. The main difference is that it passes attributes
 * for some tokens back to the parser, using the yylval mechanism to push
 * the attribute onto the value stack.
 */
%{
package lex;
import java.io.File;
import java.util.HashMap;
import lib.SourceFileInput;
import lib.YyDebug;
%}
let     [_a-zA-Z]			/* Letter 									*/
alnum   [_a-zA-Z0-9]		/* Alphanumeric character					*/
h       [0-9a-fA-F]			/* Hexadecimal digit						*/
o       [0-7]				/* Octal digit								*/
d       [0-9]				/* Decimal digit							*/
suffix  [UuLl]				/* Suffix in integral numeric constant		*/
white   [\x00-\x09\x0b\s]   /* White space: all control chars but \n 	*/
%%
"/*"                {
                        int i;

                        while (0 != (i = sourceInput.Input()))
                        {
                           if (i < 0)
                              sourceInput.FlushBuf();   /* Discard lexeme. */

                           else if ('*' == i &&  '/' == sourceInput.LookAhead(1))
                           {
                                sourceInput.Input();
                                break;          		/* Recognized comment.*/
                           }
                        }

                        if (0 == i)
                           yyDebug.YyError("End of file in comment\n");
                    }
\"(\\.|[^\"])*\"	retVal = Token.STRING;
\"(\\.|[^\"])*\n    yyDebug.YyError("Adding missing \" to string constant\n");
					YyMore();
'.'                 |
'\\.'               |
'\\{o}({o}{o}?)?'	|
'\\x{h}({h}{h}?)?'	|
0{o}*{suffix}?      |
0x{h}+{suffix}?     |
[1-9]{d}*{suffix}?  retVal = Token.ICON ;
({d}+|{d}+\.{d}*|{d}*\.{d}+)([eE][\-+]?{d}+)?[fF]?	retVal = Token.FCON;
"("					retVal = Token.LP;
")" 				retVal = Token.RP;
"{" 				retVal = Token.LC;
"}"					retVal = Token.RC;
"[" 				retVal = Token.LB;
"]" 				retVal = Token.RB;
"->"    |
"."     Globals.yyLval.ascii = Globals.yyText.charAt(0);
					retVal = Token.STRUCTOP;
"++"    |
"--"    Globals.yyLval.ascii = Globals.yyText.charAt(0);
					retVal = Token.INCOP;
[~!]    Globals.yyLval.ascii = Globals.yyText.charAt(0);
					retVal = Token.UNOP;
"*"     			retVal = Token.STAR;
[/%]    Globals.yyLval.ascii = Globals.yyText.charAt(0);
					retVal = Token.DIVOP;
"+"     			retVal = Token.PLUS;
"-"     			retVal = Token.MINUS;
<<|>>   Globals.yyLval.ascii = Globals.yyText.charAt(0);
					retVal = Token.SHIFTOP;
[<>]=?  Globals.yyLval.ascii = Globals.yyText.length() > 1 ? ('>' == Globals.yyText.charAt(0) ? 'G' : 'L') : (Globals.yyText.charAt(0));
					retVal = Token.RELOP;
[!=]=   Globals.yyLval.ascii = Globals.yyText.charAt(0);
					retVal = Token.EQUOP;
[*/%+\-&|^]=	|
(<<|>>)=		Globals.yyLval.ascii = Globals.yyText.charAt(0);
					retVal = Token.ASSIGNOP;
"="             	retVal = Token.EQUAL;
"&"             	retVal = Token.AND;
"^"             	retVal = Token.XOR;
"|"             	retVal = Token.OR;
"&&"            	retVal = Token.ANDAND;
"||"            	retVal = Token.OROR;
"?"             	retVal = Token.QUEST;
":"             	retVal = Token.COLON;
","             	retVal = Token.COMMA;
";"             	retVal = Token.SEMI;
"..."				retVal = Token.ELLIPSIS;
{let}{alnum}*   	retVal = IdOrKeyword(Globals.yyText);
\n		     	WriteToFile(Globals.yyCodeOut, "\t\t\t\t\t\t\t\t\t/*" + Globals.yyLineno + "*/\n");
{white}+        ;	/* ignore other white space */
.               yyDebug.YyError("Illegal character <" + Globals.yyText + ">\n");
					legalChar = false;
					retVal    = Token.OTHER;
%%
}