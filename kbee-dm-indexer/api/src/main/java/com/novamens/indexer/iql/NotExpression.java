package com.novamens.indexer.iql;

public class NotExpression implements Expression {
	private Expression e;
	
	public NotExpression(Expression e) {
		this.e = e;
	}
	
	public void accept(ExpressionVisitor visitor) {
		visitor.visitNot(this);
	}
	
	public Expression getExpression() {
		return e;
	}
	
	public String toString() {
		boolean leaf = getExpression() instanceof PredicateExpression;
		return " NOT " +(!leaf ? "(" : "") + getExpression().toString() + (!leaf ? ")" : "");
	}
}