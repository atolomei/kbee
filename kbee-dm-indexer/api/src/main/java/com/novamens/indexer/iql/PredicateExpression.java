package com.novamens.indexer.iql;

public class PredicateExpression implements Expression {
	private Predicate predicate;
	private Object argument;
	
	public PredicateExpression(Predicate predicate, String argument) {
		setPredicate(predicate);
		setArgument(argument);
	}
	
	public void accept(ExpressionVisitor visitor) {
		visitor.visitPredicate(this);
	}
	
	public void setArgument(Object argument) {
		this.argument = argument;
	}
	
	public Object getArgument() {
		return this.argument;
	}
	
	public void setPredicate(Predicate predicate) {
		this.predicate = predicate;
	}
	
	public Predicate getPredicate() {
		return this.predicate;
	}
	
	public String toString() {
		return getPredicate().getName() + "(" + argument + ")";
	}
}
