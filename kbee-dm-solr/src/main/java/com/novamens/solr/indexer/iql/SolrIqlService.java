package com.novamens.solr.indexer.iql;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.novamens.dom.Domain;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlLexer;
import com.novamens.indexer.iql.IqlParser;
import com.novamens.indexer.iql.IqlQuery;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.iql.IqlTreeParser;
import com.novamens.indexer.iql.ParserException;
import com.novamens.indexer.iql.PredicateManager;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerException;


public class SolrIqlService implements IqlService {
			
	static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(SolrIqlService.class.getName());

	private PredicateManager predicates;
	private Index index;
	private Domain domain;
	
	public SolrIqlService() {
	}
	
	public SolrIqlService(Index index, Domain domain) {
		this.index = index;
		this.domain = domain;
	}
	
	public ResultSet extecute(Query query) {
		return null;
	}
	
	public ResultSet execute(String statement) {
		return (new SolrIqlQuery(getIndex(), domain, statement)).execute();
	}
	
	public IqlQuery getNewQuery(String statement) {
		return new SolrIqlQuery(getIndex(), domain, statement);
	}
	
	public IqlQuery getNewQuery(String statement, String sort, boolean ascending) {
		return new SolrIqlQuery(getIndex(), domain, statement);
	}
	
	public PredicateManager getPredicateManager() {
		return predicates;
	}
	
	public Expression getExpression(String statement) {
		try {
			IqlLexer lexer = new IqlLexer(new ByteArrayInputStream(statement.getBytes("ISO-8859-1")));
			IqlParser parser = new IqlParser(lexer);
			parser.query();
			antlr.CommonAST ast = (antlr.CommonAST)parser.getAST();
			if (ast==null || parser.errors()) {
				logger.error( parser.errors() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				throw new RuntimeException(new ParserException(parser.getError()));
			}
			IqlTreeParser builder = new IqlTreeParser(getPredicates());
			Expression expression = builder.query(ast);
			return expression;
		}
		catch (IndexerException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
		catch (UnsupportedEncodingException e) {
			logger.error(e);
			throw new RuntimeException(new ParserException(e));
		}
		catch (TokenStreamException e) {
			logger.error(e);
			throw new RuntimeException(new ParserException(e));
		}
		catch (RecognitionException e) {
			logger.error(e);
			throw new RuntimeException(new ParserException(e));
		}
	}
	
	public void setPredicateManager(PredicateManager manager) {
		this.predicates = manager;
	}
	
	public PredicateManager getPredicates() {
		return this.predicates;
	}
	
	public Index getIndex() {
		return index;
	}
}
