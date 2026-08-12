package com.novamens.indexer.iql;

public interface ExpressionVisitor {
	public void visitPredicate(PredicateExpression expression);
	public void visitAnd(AndExpression expression);
	public void visitOr(OrExpression expression);
	public void visitNot(NotExpression expression);
}
