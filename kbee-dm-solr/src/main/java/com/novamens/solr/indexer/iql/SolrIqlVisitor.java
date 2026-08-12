package com.novamens.solr.indexer.iql;

import com.novamens.indexer.iql.AndExpression;
import com.novamens.indexer.iql.CalculatedPredicate;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.ExpressionVisitor;
import com.novamens.indexer.iql.NotExpression;
import com.novamens.indexer.iql.OrExpression;
import com.novamens.indexer.iql.PredicateExpression;

public class SolrIqlVisitor implements ExpressionVisitor {
	private StringBuffer buffer = new StringBuffer();
	private String statement = null;
	private Expression expression;
	
	public SolrIqlVisitor(Expression expression) {
		this.expression = expression;
	}
	
	public void visitPredicate(PredicateExpression expression) {
		if (expression.getPredicate() instanceof CalculatedPredicate)
			buffer.append(((CalculatedPredicate)expression.getPredicate()).getCode((String)expression.getArgument()));
		else
			buffer.append(expression.getPredicate().getPath()+":"+expression.getArgument());
	}
	
	public void visitAnd(AndExpression expression) {
		buffer.append("(");
		expression.getExpressionA().accept(this);
		buffer.append(" AND ");
		expression.getExpressionB().accept(this);
		buffer.append(")");
	}
	
	public void visitOr(OrExpression expression) {
		buffer.append("(");
		expression.getExpressionA().accept(this);
		buffer.append(" OR ");
		expression.getExpressionB().accept(this);
		buffer.append(")");
	}
	
	public void visitNot(NotExpression expression) {
		buffer.append("NOT ");
		expression.getExpression().accept(this);
	}
	
	public String getStatement() {
		if (statement==null) {
			expression.accept(this);
			statement = buffer.toString();
		}
		return statement;
	}
}
