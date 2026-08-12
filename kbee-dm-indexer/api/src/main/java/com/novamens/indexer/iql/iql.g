 header{
package com.novamens.indexer.iql;
}

{
import java.io.*;
import java.util.List;
import java.util.ArrayList;
}

class IqlParser extends Parser;

options {
	codeGenMakeSwitchThreshold = 3;
	codeGenBitsetTestThreshold = 4;
	buildAST=true;
	ASTLabelType = "antlr.CommonAST"; // change default of "AST"
}

{
	private List<String> errors = new ArrayList<String>();
	
	public void reportError(RecognitionException e) {
		super.reportError(e);
		errors.add(e.getMessage());
	}
	
	public List<String> getErrors() {
		return errors;
	}
		
	public boolean errors() {
		return !errors.isEmpty();
	}
	
	public String getError() {
		String message = "";
		for (String error : getErrors()) {
			message += ("".equals(message) ? "" : "; ") + error;
		}
		return message;
	}	
}

query	
			: 	logExpr EOF!;

logExpr
			:	postfixExpr ((AND^|OR^) postfixExpr)*
			;


postfixExpr
			:	predicate
			| 	LPAREN! logExpr RPAREN!
			| 	NOT^ postfixExpr
			;
			
predicate
			:	ID^ LPAREN! value RPAREN!
			;

value 		:  (ID|STRING_LITERAL)*
			;
			
class IqlLexer extends Lexer;

options {
	charVocabulary = '\0'..'\377';
	testLiterals = false;    // don't automatically test for literals
	k = 4;                  // four characters of lookahead
	caseSensitive = false;
	caseSensitiveLiterals = false;
}

tokens {
	NOT 				  = "not";
	AND                 = "and";             
	OR                  = "or";         
	ROOT				  = "root";    
}


WS	:	(' '
	|	'\t'
	|	'\n'
	|	'\r')
		{ _ttype = Token.SKIP; }
	;

LPAREN:	'('
	;

RPAREN:	')'
	;


CHAR_LITERAL
	:	'\'' (ESC|~'\'')* '\''
	;
	
STRING_LITERAL
	:	'"' (ESC|~'"')* '"'
	;
	
protected
ESC	:	'\\'
		(	'n'
		|	'r'
		|	't'
		|	'b'
		|	'f'
		|	'"'
		|	'\''
		|	'\\'
		|	('0'..'3')
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:	('0'..'9')
				(	
					options {
						warnWhenFollowAmbig = false;
					}
				:	'0'..'9'
				)?
			)?
		|	('4'..'7')
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:	('0'..'9')
			)?
		)
	;

ID
options {
	testLiterals = true;
}
	:	('a'..'z'|'0'..'9'|'_'|'-'|'%'|'/') ('a'..'z'|'^'|'\u00fc'|'\u00b0'|'.'|','|'*'|'-'|'?'|'%'|'_'|'0'..'9'|'/')*;

{
import java.util.ArrayList;
import java.util.List;
import com.novamens.indexer.iql.AbstractPredicate;
import com.novamens.indexer.iql.Predicate;
import com.novamens.indexer.iql.CalculatedPredicate;
import com.novamens.indexer.iql.PredicateNotFoundException;
import com.novamens.indexer.service.IndexerException;
}

class IqlTreeParser extends TreeParser;	
{
	private List<String> errors = new ArrayList<String>();
	private PredicateManager predicates;
	
	public IqlTreeParser(PredicateManager predicates) {
		this.predicates = predicates;
	}	
	
	public void reportError(RecognitionException e) {
		super.reportError(e);
		String msg = e.getMessage();
		errors.add(msg);
	}
	
	public String getStringArgument(String value) {
		String argument = null;
		String words[] = value.split(" ");
		if (words.length==1) {
			argument = value;
		}
		else {
			if (value.indexOf("%")<0) {
				argument = value.startsWith("\"") ? value : "\"" + value;
				if (!value.endsWith("\"")) argument += "\"";
			}
			else {
				argument = "(";
				for (int w=0; w<words.length; w++) {
					argument += "+";
					if (words[w].indexOf("%")<0) {
						argument += w==0 ? words[w]+"*" : "*"+words[w]+(w==words.length-1?"":"*"); 
					}
					else {
						argument += w==0 ? words[w] : "*"+words[w]; 
					}
					if (w<words.length-1) argument += " ";
				}
				argument += ")";
			}
		}
		argument = argument.replace("%", "*");
		return argument.toLowerCase();
	}
	
	public String getTextArgument(String value) {
		String argument = null;
		String words[] = value.split(" ");
		if (words.length==1) {
			argument = value;
		}
		else {
			if (value.indexOf("%")<0) {
				argument = value.startsWith("\"") ? value : "\"" + value;
				if (!value.endsWith("\"")) argument += "\"";
			}
			else {
				argument = "(";
				for (int w=0; w<words.length; w++) {
					argument += "+";
					String word = words[w].replace("%", "");
					argument += word.trim();
					if (w<words.length-1) argument += " ";
				}
				argument += ")";
			}
		}
		return argument.toLowerCase();
	}
	
	private String getArgument(Predicate predicate, String value) throws IndexerException {
		if (!predicate.validValue(value)) {
			throw new IllegalArgumentException(predicate.getName());
		}	
		if (AbstractPredicate.DATE_TYPE.equals(predicate.getValueType())) {
			value =  DateMath.getArgument(value);
			return value;
		}
		if (AbstractPredicate.TEXT_TYPE.equals(predicate.getValueType())) {
			String pvalue = predicate.getArgument(value);
			value = getTextArgument(pvalue);
			return value;
		}	
		value = getStringArgument(value);
 		return value;
	}	
}

query returns [Expression r] throws IndexerException
{
	String v;
	Expression a, b;
	r=null;
}
		: #(AND a=query b=query) 
			{
				//r = "(" + a +  " AND "+ b +")";
				r = new AndExpression(a, b);
			}
		| #(OR a=query b=query) 
			{
				//r = "(" + a + " OR " + b +")";
				r = new OrExpression(a, b);
			}
		| #(NOT a=query) 
			{
				//r = "!(" + a +")";
				r = new NotExpression(a);
			}
		| l:CHAR_LITERAL 
			{
				v = l.toString();
			}			
		| #(i:ID v=value)
			{
				String name = i.toString().toLowerCase();
				Predicate predicate = predicates.getPredicate(name);
				if (predicate == null) 
					throw new PredicateNotFoundException(name);
				if (v == null) {
					v = predicate.getDefaultValue();
					if (v==null) v = "";
				}
				if (predicate instanceof CalculatedPredicate) {
					//r = ((CalculatedPredicate)predicate).getCode(v);
					r = new PredicateExpression(predicate, v);
				}
				else {
					r = new PredicateExpression(predicate, getArgument(predicate, v));
					//r = predicate.getPath()+":" + getArgument(predicate, v);
				}
			}
		;
value returns [String r] throws IndexerException
{
	r="";
	if (_t==null) return null;
}

	: i:ID
		{
			r = i.toString();
			if (_t!=null) r += " " + value(_t);
		}	
	| s:STRING_LITERAL
		{
			r = s.toString();
			if (_t!=null) r += " " + value(_t);
		}	
	;