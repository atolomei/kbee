package com.novamens.indexer.iql;

public interface Expression {
	public void accept(ExpressionVisitor visitor);
	public String toString();
}