package com.novamens.content.service;

import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Query;
import com.novamens.service.BusinessSystemService;

public interface ContentFilteringService extends BusinessSystemService {

	List<? extends Content> getRecent(String contentClassName, Domain domain, int max);

	long increment(String contentClassName, Domain domain);

	long decrement(String contentClassName, Domain domain);

	long getTotalUsers(Domain domain);

	long getTotalContents(String contentClassName, Domain domain);

	List<? extends Content> getFeatured(String contentClassName, Domain domain, int max);

	Query getQuestionsUnanswered(Domain domain);

}
