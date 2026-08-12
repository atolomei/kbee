package com.novamens.solr.indexer.iql;


import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.novamens.dom.Domain;
import com.novamens.indexer.iql.IqlLexer;
import com.novamens.indexer.iql.IqlParser;
import com.novamens.indexer.iql.IqlQuery;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.iql.IqlTreeParser;
import com.novamens.indexer.iql.ParserException;
import com.novamens.indexer.iql.PredicateManager;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerException;
import com.novamens.solr.indexer.query.SolrQuery;

public class SolrIqlQuery extends SolrQuery implements IqlQuery {
			
	private static final long serialVersionUID = 1L;
	
 	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SolrIqlQuery.class.getName());
	
	private transient PredicateManager predicates;
	private Domain domain;
	private String statement;

	public SolrIqlQuery(Index index, Domain domain) {
		super(index);
		this.domain = domain;
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "true");
	}

	public SolrIqlQuery(Index index, Domain domain, String statement) {
		super(index);
		this.domain = domain;
		getParameters().put("sort", "modified");
		getParameters().put("ascending", "true");
		setStatement(statement);
	}
	
	public SolrIqlQuery(Index index, Domain domain, String statement, String sort, boolean ascending) {
		super(index);
		this.domain = domain;
		getParameters().put("sort", sort);
		getParameters().put("ascending", Boolean.valueOf(ascending));
		setStatement(statement);
	}
	
	public void setStatement(String statement) {
		this.statement = statement;
	}
	
	public String getStatement() {
		return statement;
	}
	
	public void setPredicates(PredicateManager predicates) {
		this.predicates = predicates;
	}
	
	public PredicateManager getPredicates() {
		if (predicates==null)
			this.predicates = ((SolrIqlService)domain.getService(IqlService.class)).getPredicates();
		return predicates;
	}
	
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		try {
			
			if (domain!=null)
				str.append(domain.getName()+" | ");
			
			if (statement!=null)
				str.append("Statement: " + statement +" | ");
			
			if (getParameters()!=null)
				str.append("Parameters: " + getParameters().toString() +" | ");
			
			String solrs = getSolrStatement();
			str.append(solrs);
		}
		catch (Exception e) {
			logger.error(e);
			str.append("SolR Statement: " + e.getClass().getName());
		}  
		return str.toString();
	}

		
	public String getSolrStatement() {
		try {
			String statement = getStatement();
			
			if (statement==null) {
				logger.error("statement is null");
				return null;
			}
			
			IqlLexer lexer = new IqlLexer(new ByteArrayInputStream(statement.getBytes("ISO-8859-1")));
			IqlParser parser = new IqlParser(lexer);
			parser.query();
			antlr.CommonAST ast = (antlr.CommonAST)parser.getAST();

			if (ast==null || parser.errors()) {
				logger.error( (ast==null?"antlr.CommonAST is null":parser.errors())  + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				throw new RuntimeException(new ParserException(parser.getError()));
			}
			
			IqlTreeParser builder = new IqlTreeParser(getPredicates());
			SolrIqlVisitor visitor = new SolrIqlVisitor(builder.query(ast));
			String solrStatement = visitor.getStatement();
			return solrStatement;
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
}
