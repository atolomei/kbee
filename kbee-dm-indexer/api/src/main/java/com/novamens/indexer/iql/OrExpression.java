package com.novamens.indexer.iql;

public class OrExpression implements Expression {
	private Expression a, b;
	
	public OrExpression(Expression a, Expression b) {
		this.a = a;
		this.b = b;
	}
	
	public void accept(ExpressionVisitor visitor) {
		visitor.visitOr(this);
	}
	
	public Expression getExpressionA() {
		return a;
	}
	
	public Expression getExpressionB() {
		return b;
	}
	
	public String toString() {
		boolean leafA = getExpressionA() instanceof PredicateExpression;
		boolean leafB = getExpressionB() instanceof PredicateExpression;
		return (!leafA ? "(" : "") + getExpressionA().toString() + (!leafA ? ")" : "") + " OR " + (!leafB ? "(" : "") + getExpressionB().toString() + (!leafB ? ")" : "");
	}
}