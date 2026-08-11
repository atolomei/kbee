package com.novamens.kbee.content.security;

import com.novamens.content.iql.ClassifierPredicate;
import com.novamens.indexer.iql.AndExpression;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.ExpressionVisitor;
import com.novamens.indexer.iql.NotExpression;
import com.novamens.indexer.iql.OrExpression;
import com.novamens.indexer.iql.PredicateExpression;

public class JavaIqlEvaluator implements ExpressionVisitor {
	private Object object = null;
	private boolean evaluation = false;
	private Expression expression;
	
	public JavaIqlEvaluator(Expression expression) {
		this.expression = expression;
	}
	
	public void visitPredicate(PredicateExpression expression) {
		if (expression.getPredicate() instanceof ClassifierPredicate) {
			Object argument = expression.getArgument();
			if (argument instanceof String && !isDigits((String)argument) && !"null".equals(argument)) {
				expression.setArgument(((ClassifierPredicate)expression.getPredicate()).getMembers((String)argument));
			}
		}
		evaluation = expression.getPredicate().evaluate(object, expression.getArgument());
	}
	
	public void visitAnd(AndExpression expression) {
		expression.getExpressionA().accept(this);
		if (!evaluation) return;
		expression.getExpressionB().accept(this);
	}
	
	public void visitOr(OrExpression expression) {
		expression.getExpressionA().accept(this);
		boolean evaluationA = evaluation;
		expression.getExpressionB().accept(this);
		boolean evaluationB = evaluation;
		evaluation = evaluationA || evaluationB;
	}
	
	public void visitNot(NotExpression expression) {
		expression.getExpression().accept(this);
		boolean evaluation = this.evaluation;
		this.evaluation = !evaluation;
	}
	
	public boolean evaluate(Object object) {
		this.object = object;
		expression.accept(this);
		return evaluation;
	}
	
	private boolean isDigits(String argument) {
		for (int c=0; c<argument.length(); c++) {
			if (!Character.isDigit(argument.charAt(c))) {
				return false;
			}
		}
		return true;
	}

}
