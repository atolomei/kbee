package com.novamens.kbee.content.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.content.iql.AttributePredicate;
import com.novamens.content.iql.ClassifierPredicate;
import com.novamens.indexer.iql.AndExpression;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.ExpressionVisitor;
import com.novamens.indexer.iql.NotExpression;
import com.novamens.indexer.iql.OrExpression;
import com.novamens.indexer.iql.PredicateExpression;

public class PredicatesIqlEvaluator implements ExpressionVisitor {
	private Map<String, List<String>> predicates = new HashMap<String, List<String>>();
	private Expression expression;
	
	public PredicatesIqlEvaluator(Expression expression) {
		this.expression = expression;
	}
	
	public void visitPredicate(PredicateExpression expression) {
		if (expression.getPredicate() instanceof ClassifierPredicate ||
				expression.getPredicate() instanceof AttributePredicate) {
			Object argument = expression.getArgument();
			if (argument instanceof String) {
				List<String> arguments = predicates.get(expression.getPredicate().getName());
				if (arguments == null) {
					arguments = new ArrayList<String>();
					predicates.put(expression.getPredicate().getName(), arguments);
				}
				arguments.add((String)argument);
			}
		}
	}
	
	public void visitAnd(AndExpression expression) {
		expression.getExpressionA().accept(this);
		expression.getExpressionB().accept(this);
	}
	
	public void visitOr(OrExpression expression) {
		expression.getExpressionA().accept(this);
		expression.getExpressionB().accept(this);
	}
	
	public void visitNot(NotExpression expression) {
		expression.getExpression().accept(this);
	}
	
	public Map<String, List<String>> evaluate() {
		expression.accept(this);
		return predicates;
	}
}
