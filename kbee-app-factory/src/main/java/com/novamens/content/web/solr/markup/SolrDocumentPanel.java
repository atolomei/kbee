package com.novamens.content.web.solr.markup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.indexer.service.IndexerDocument;

@SuppressWarnings("serial")
public class SolrDocumentPanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	private IndexerDocument document;

	public SolrDocumentPanel(String id, IndexerDocument document) {
		super(id);
		
		setDocument(document);
		
		List<String> fieldnames = new ArrayList<String>();
		fieldnames.addAll(document.getFieldNames());
		
		Collections.sort(fieldnames, new Comparator<String>() {
			public int compare(String s1, String s2) {
				try {
					return s1.compareTo(s2);
				} catch (Exception e) {
					return 0;
				}
			}
		});
		
		add(new ListView<String>("field", fieldnames) {
			public void populateItem(ListItem<String> item) {
				item.add(new Label("name", item.getModelObject()));
				String value = getDocument().getFieldValue(item.getModelObject()).toString();
				if (value.length()>256) value = value.substring(0, 255)+"...";
				item.add(new Label("value", value));
			}
		});
	}
	
	public void setDocument(IndexerDocument document) {
		this.document = document;
	}
	
	public IndexerDocument getDocument() {
		return document;
	}

}
