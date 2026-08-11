package com.novamens.kbee.content.rule;

import java.io.Serializable;
import java.time.OffsetDateTime;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Proxy;

import com.novamens.content.base.Content;
import com.novamens.content.base.RuleCondition;
import com.novamens.content.iql.ClassifierPredicate;
import com.novamens.content.rule.Action;
import com.novamens.content.rule.ActionRule;
import com.novamens.dom.Json;
import com.novamens.indexer.iql.AndExpression;
import com.novamens.indexer.iql.DateTimePredicate;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.ExpressionVisitor;
import com.novamens.indexer.iql.NotExpression;
import com.novamens.indexer.iql.OrExpression;
import com.novamens.indexer.iql.PredicateExpression;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;

public class ExecutionDateCalculator implements ExpressionVisitor {

	private Object object = null;
	private OffsetDateTime evaluation = null;
	private Expression expression;
	
	public ExecutionDateCalculator(Expression expression) {
		this.expression = expression;
	}
	
	public void visitPredicate(PredicateExpression expression) {
		if (expression.getPredicate() instanceof DateTimePredicate) {
			Object argument = expression.getArgument();
			evaluation = ((DateTimePredicate)expression.getPredicate()).calculateDateTime(object, argument);
			
		}
	}
	
	public void visitAnd(AndExpression expression) {
		expression.getExpressionA().accept(this);
		expression.getExpressionB().accept(this);
	}
	
	public void visitOr(OrExpression expression) {
		expression.getExpressionA().accept(this);
		expression.getExpressionB().accept(this);
		//evaluation = evaluationA || evaluationB;
	}
	
	public void visitNot(NotExpression expression) {
		expression.getExpression().accept(this);
	}
	
	public OffsetDateTime evaluate(Object object) {
		this.object = object;
		expression.accept(this);
		return evaluation;
	}

}
