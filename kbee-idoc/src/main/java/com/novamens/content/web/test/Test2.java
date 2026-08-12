package com.novamens.content.web.test;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.googlecode.wicket.jquery.ui.markup.html.link.AjaxLink;
import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.model.ObjectId;
import com.novamens.content.resource.KBFile;
import com.novamens.indexer.java.FileIndexerService;
import com.novamens.indexer.java.FileTextExtractor;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexProxy;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.kbee.content.script.KbeeClassificableScriptWrapper;
import com.novamens.solr.indexer.service.SolrIndex;
import com.novamens.util.KeyValue;
import com.novamens.wicket.markup.html.tabs.AjaxTabbedPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.idoc.IDocTaskPanelV6;
import kbee.web.page.ApplicationPage;
import kbee.web.resource.ResourceLink;

@SuppressWarnings("serial")
public class Test2 extends ApplicationPage<Content> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(Test2.class.getName());
	
	private static final long serialVersionUID = 1L;

	private IModel<Content> model;
	boolean allparagraphs=false;
	SimilaresTool similarestool;
	Analyzer analyzer;
	
	private class HeaderFragment extends Fragment {
		public HeaderFragment() {
			super("header", "header-fragment", Test2.this);
			add(new Label("titulo", () -> getContent().getTitle()));
			add(new Label("tribunal", () -> getValue("tribunal")));
			add(new Label("fecha", () -> getDate("fecha")));
			add(new ResourceLink<Content>("texto", getTextModel(), getModel()));
		}
		public String getDate(String name) {
			return getModelObject().getDate(name);
		}
		public String getValue(String name) {
			return getModelObject().getLabel(name);
		}
		private KbeeClassificableScriptWrapper getModelObject() {
			return new KbeeClassificableScriptWrapper(getContent());
		}
	}
	
	@SuppressWarnings("unchecked")
	private class SimilaresTool extends Fragment {
		private IModel<Content> selected = null;
		List<KeyValue<String>> paragraphs = new ArrayList<KeyValue<String>>();
		public SimilaresTool(String id) {
			super(id, "similares-fragment", Test2.this);
			
			setOutputMarkupId(true);
			
			add(new ListView<KeyValue<String>>("similar", () -> getSimilars()) {
				
				public void populateItem(ListItem<KeyValue<String>> item) {
					
					AjaxLink<?> link = new AjaxLink<Void>("link") {
						public void onClick(AjaxRequestTarget target) {
							selected = ((IModel<Content>)item.getModelObject().getKey());
							setParagraphs(Test2.this.getParagraphs(selected));
							target.add(SimilaresTool.this);
						}
					};
					
					
					Content content = ((IModel<Content>)item.getModelObject().getKey()).getObject();
					link.add(new Label("title", content.getTitle()));
			
					
					
					
					item.add(link);
					String score = item.getModelObject().getValue();
					item.add(new Label("score", score));
				}
			});
			
			WebMarkupContainer paragraphs = new WebMarkupContainer("paragraphs") {
				public boolean isVisible() {
					return selected!=null;
				}
			};
			
			paragraphs.add(new Label("fallo1", new Model<String>() {
				public String getObject() {
					return getContent().getTitle();
				}
			}));
			paragraphs.add(new Label("fallo2", new Model<String>() {
				public String getObject() {
					return selected!=null ? selected.getObject().getTitle() : "";
				}
			}));
			
			paragraphs.add(new ListView<KeyValue<String>>("paragraph", () -> getParagraphs()) {
				public void populateItem(ListItem<KeyValue<String>> item) {
					item.add(new Label("text", item.getModelObject().getKey()));
					item.add(new Label("snippet", item.getModelObject().getValue()));
				}
			});
			add(paragraphs);
		}
		public void refresh(AjaxRequestTarget target) {
			setParagraphs(Test2.this.getParagraphs(selected));
			target.add(this);
		}
		public void setSelected(IModel<Content> selected) {
			this.selected = selected;
		}
		public List<KeyValue<String>> getParagraphs() {
			return paragraphs;
		}
		public void setParagraphs(List<KeyValue<String>> paragraphs) {
			this.paragraphs = paragraphs;
		}
	}	
	
	private class FullTextTool extends Fragment {
		public FullTextTool(String id) {
			super(id, "fulltext-fragment", Test2.this);
			setOutputMarkupId(true);
			WebMarkupContainer allparagraphs = new WebMarkupContainer("allparagraphs") {
				public boolean isVisible() {
					return Test2.this.allparagraphs;
				}
			};
			
			add(new AjaxLink<Void>("all") {
				public void onClick(AjaxRequestTarget target) {
					Test2.this.allparagraphs = true;
					target.add(FullTextTool.this);
				}
			});
			
			
			allparagraphs.add(new ListView<String>("paragraph", () -> getParagraphs()) {
				public void populateItem(ListItem<String> item) {
					final String paragraph = item.getModelObject();
					Label textlabel = new Label("text", paragraph);
					item.add(textlabel);
					List<String> scores = new ArrayList<String>();
					List<IModel<Content>> contents = getContents(item.getModelObject(), scores);
					if (!contents.isEmpty()) {
						textlabel.add(new AttributeModifier("style", "background-color:yellow; padding:15px 30px;"));
					}
					item.add(new ListView<IModel<Content>>("content", contents) {
						public void populateItem(ListItem<IModel<Content>> item) {
							item.setOutputMarkupId(true);
							WebMarkupContainer textcontainer = new WebMarkupContainer("text-container");
							Model<String> textmodel = new Model<String>() {
								public String getObject() {
									try {
										KeyValue<String> similar = getSimilar(item.getModelObject(), paragraph);
										return similar!=null ? "\""+(String)similar.getValue()+"\"" : "";
									}
									catch (Exception e) {
										return "";
									}
								}
							};
							Label textlabel = new Label("text", textmodel);
							AjaxLink<?> expander = new AjaxLink<Void>("expander") {
								public void onClick(AjaxRequestTarget target) {
									textcontainer.setVisible(!textcontainer.isVisible());
									target.add(item);
								}
							};
							
							
							//textcontainer.add(new Label("metainfo", getContentMetaInfo(item.getModelObject().getObject())));
							
							textcontainer.add(textlabel);
							textcontainer.add(new AjaxLink<Void>("compare-link") {
								public void onClick(AjaxRequestTarget target) {
									onSelect(target, item.getModelObject());
								}
							});
							textcontainer.setVisible(false);
							AjaxLink<?> link = new AjaxLink<Void>("link") {
								public void onClick(AjaxRequestTarget target) {
									onSelect(target, item.getModelObject());
								}
							};
							textcontainer.add(new ResourceLink<Content>("text-link", getTextModel(item.getModelObject()), item.getModelObject()));
							String title = item.getModelObject().getObject().getTitle() + " (" + scores.get(item.getIndex()) + ")";
							link.add(new Label("title", title));
							link.add(new Label("metainfo",  getContentMetaInfo(item.getModelObject().getObject())));
							
							
							item.add(textcontainer);
							item.add(expander);
							item.add(link);
						}
					});
				}
			});
			allparagraphs.setOutputMarkupId(true);
			add(allparagraphs);
			
		}
		public void onSelect(AjaxRequestTarget target, IModel<Content> content) {
			
		}
		
		
	}	

	
	
	public String getContentMetaInfo(Content content) {
		KbeeClassificableScriptWrapper wrapper = new KbeeClassificableScriptWrapper(content);
		String metainfo = wrapper.getLabel("tribunal");
		metainfo += " "+wrapper.getDate("fecha");
		return metainfo;
	}
	
	
	public Test2(PageParameters parameters) {
		
		try {
			
			
			setModel(getContent(parameters));
			
			WebMarkupContainer mainpanel = new WebMarkupContainer("mainpanel");
			
			mainpanel.setOutputMarkupId(true);
			
			mainpanel.add(new HeaderFragment());
			
			List<ITab> tools = new ArrayList<ITab>();
			
			tools.add(new AbstractTab(new Model<String>("Coincidentes")) {
				public WebMarkupContainer getPanel(String panelId) {
					
					if (similarestool==null) 
						similarestool = new SimilaresTool(panelId);
					
					return similarestool;
				}
			});
			
			tools.add(new AbstractTab(new Model<String>("Análisis Texto Completo")) {
				public WebMarkupContainer getPanel(String panelId) {
					
					return new FullTextTool(panelId) {
						
						public void onSelect(AjaxRequestTarget target, IModel<Content> model) {
					
							similarestool.setSelected(model);
							
							((AjaxTabbedPanel<ITab>)mainpanel.get("tools")).setSelectedTab(0);
							similarestool.refresh(target);
							target.add(mainpanel);
						}
					};
				}
			});
			
			mainpanel.add(new AjaxTabbedPanel<ITab>("tools", tools));
			
			add(mainpanel);
		}
		catch (Exception e) {
		logger.error(e);
		}
		
		
	}
	
	public void setModel(Content content) {
		model = content!=null ? new ObjectModel<Content>(content) : null;
	}
	
	public IModel<Content> getModel() {
		return model;
	}
	
	public Content getContent() {
		return  model!=null ? model.getObject() : null;
	}
	
	public com.novamens.content.base.Resource getTextFile() {
		return ((ResourceContainer)getContent()).getResources().get(0);
	}
	
	public com.novamens.content.base.Resource getTextFile(IModel<Content> model) {
		return ((ResourceContainer)model.getObject()).getResources().get(0);
	}
	
	public IModel<com.novamens.content.base.Resource> getTextModel() {
		return new ObjectModel<com.novamens.content.base.Resource>(getTextFile());
	}
	
	public IModel<com.novamens.content.base.Resource> getTextModel(IModel<Content> model) {
		return new ObjectModel<com.novamens.content.base.Resource>(getTextFile(model));
	}

	
	public String getText() {
		FileTextExtractor extractor = new FileTextExtractor();
		KBFile file = (KBFile)((ResourceContainer)getContent()).getResources().get(0);
		String text = (String)extractor.extract(file);
		return text;
	}
	
	public List<KeyValue<String>> getSimilars() {
		SolrQuery query = getMltQuery();
		List<KeyValue<String>> similars = new ArrayList<KeyValue<String>>();
		try {
			QueryResponse response = (((SolrIndex)getIndex()).getServer()).query(query);
			SolrDocumentList resultSet = response.getResults();
			for (int h = 0; h<resultSet.size(); h++) {
				SolrDocument solrdocument = resultSet.get(h);
				Content content = (Content)getContentDao().findObjectById(new ObjectId((String)solrdocument.getFieldValue("id")));
				String score = String.valueOf(solrdocument.getFieldValue("score"));
				similars.add(new KeyValue<String>(new ObjectModel<Content>(content), score));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return similars;
	}
	
	public List<KeyValue<String>> getSimilars2() {
		SolrQuery query = getMltQuery2();
		List<KeyValue<String>> similars = new ArrayList<KeyValue<String>>();
		try {
			QueryResponse response = (((SolrIndex)getIndex()).getServer()).query(query);
			SolrDocumentList resultSet = response.getResults();
			for (int h = 0; h<resultSet.size(); h++) {
				SolrDocument solrdocument = resultSet.get(h);
				Content content = (Content)getContentDao().findObjectById(new ObjectId((String)solrdocument.getFieldValue("id")));
				String score = String.valueOf(solrdocument.getFieldValue("score"));
				similars.add(new KeyValue<String>(new ObjectModel<Content>(content), score));
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
		return similars;
	}

	
	public List<IModel<Content>> getContents(String paragraph, List<String> scores)  {
		List <IModel<Content>> contents = new ArrayList<IModel<Content>>();
		try {
			String words[] = paragraph.split(" ");
			if (words.length<10) return contents; 
			paragraph = ClientUtils.escapeQueryChars(paragraph);
			SolrQuery query = getAllPQuery(paragraph);
			QueryResponse response = (((SolrIndex)getIndex()).getServer()).query(query);
			SolrDocumentList resultSet = response.getResults();
			for (int h = 0; h<resultSet.size(); h++) {
				SolrDocument solrdocument = resultSet.get(h);
				String id = solrdocument.getFieldValue("id").toString();
				Content content = (Content)getContentDao().findObjectById(new ObjectId((String)solrdocument.getFieldValue("id")));
				String score = String.valueOf(solrdocument.getFieldValue("score"));
				if (!content.equals(model.getObject())) {
					Map<String, List<String>> snippetsmap = response.getHighlighting().get(id);
					if (snippetsmap!=null) {
						for (List<String> snippets : snippetsmap.values()) {
							for (String snippet : snippets) {
								String s = bestSnippet(snippet, paragraph);
								if (s!=null) {
									scores.add(score);
									contents.add(new ObjectModel<Content>(content));
								}
							}
						}
					}	
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return contents;
	}
	
	// parrafos similares entre el content de la pagina y el content seleccionado  
	public List<KeyValue<String>> getParagraphs(IModel<Content> selected)  {
		String text = getText();
		
		List<KeyValue<String>> similars = new ArrayList<KeyValue<String>>();
		if (selected==null) return similars;
		
		try {
			List<String>  paragraphs = getParagraphs(text);
			//String paragraphs[] = text.split("\r");
			//for (int i=0; i<paragraphs.length; i++ ) {
			for (String paragraph : paragraphs) {
				String p = paragraph;
				//String paragraph = paragraphs[i];
				try {
					String words[] = paragraph.split(" ");
					
					if (paragraph.contains("Por las razones expuestas")) {
						paragraph=paragraph;
					}
					
					// System.out.println("***********************");
					// System.out.println(paragraph.length()>1000 ? paragraph.substring(0,90) :paragraph);
					
					if (words.length>10) {
						paragraph = ClientUtils.escapeQueryChars(paragraph);
						
						SolrQuery query = getPQuery(selected, paragraph);
						QueryResponse response = (((SolrIndex)getFileIndex()).getServer()).query(query);
						SolrDocumentList resultSet = response.getResults();
						
						if (resultSet.size()>0) {
							for (int h = 0; h<resultSet.size(); h++) {
								SolrDocument solrdocument = resultSet.get(h);
								// System.out.println(solrdocument.getFieldValue("score"));
								String id = solrdocument.getFieldValue("id").toString();
								Map<String, List<String>> snippetsmap = response.getHighlighting().get(id);
								if (snippetsmap!=null) {
									for (List<String> snippets : snippetsmap.values()) {
										for (String snippet : snippets) {
											String s = bestSnippet(snippet, p);
											if (s!=null) {
												similars.add(new KeyValue<String>(p, s));
											}
										}
									}
								}	
							}
						}
					}
				}
				catch (Exception e) {
					// System.out.println(paragraph);
					// System.out.println(paragraph.length());
					e.printStackTrace();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return similars;
	}
	
	public KeyValue<String> getSimilar(IModel<Content> selected, String paragraph) throws Exception  {
		String p = ClientUtils.escapeQueryChars(paragraph);
		
		SolrQuery query = getPQuery(selected, p);
		QueryResponse response = (((SolrIndex)getFileIndex()).getServer()).query(query);
		SolrDocumentList resultSet = response.getResults();
		
		if (resultSet.size()>0) {
			for (int h = 0; h<resultSet.size(); h++) {
				SolrDocument solrdocument = resultSet.get(h);
				// System.out.println(solrdocument.getFieldValue("score"));
				String id = solrdocument.getFieldValue("id").toString();
				Map<String, List<String>> snippetsmap = response.getHighlighting().get(id);
				if (snippetsmap!=null) {
					for (List<String> snippets : snippetsmap.values()) {
						for (String snippet : snippets) {
							String s = bestSnippet(snippet, paragraph);
							if (s!=null) {
								return new KeyValue<String>(paragraph, s);
							}
						}
					}
				}	
			}
		}	
		return null;
	}
	
	public List<String> getParagraphs()  {
//		List<String> paragraphs = new ArrayList<String>();
//		String text = getText();
//		String p[] = text.split("\r");
//		for (int i=0; i<p.length; i++ ) {
//			paragraphs.add(p[i]);
//		}	
//		return paragraphs;
		return getParagraphs(getText());
	}
	
	public List<String> getParagraphs(String text)  {
		List<String> paragraphs = new ArrayList<String>();
		if (text.indexOf("\r")>0) {
			String tokens[] = text.split("\r");
			for (int t=0; t<tokens.length; t++) {
				paragraphs.add(tokens[t]);
			}
		}
		else {
			String lines[] = text.split("\n");
			String p = "";
			for (int i=0; i<lines.length; i++ ) {
				String line = lines[i];
				p += " " + line;
				if (line.trim().endsWith(".") || line.trim().endsWith(":") || line.trim().endsWith("-")) {
					paragraphs.add(p);
					p="";
				}
			}
			paragraphs.add(p);
		}
		return paragraphs;
	}	
	
	public String bestSnippet(String snippet, String paragraph) throws Exception {
		boolean value = false;
		List<String> matchs = new ArrayList<String>();
		List<String> snippetparagraphs = getParagraphs(snippet);
		for (String snippetparagraph : snippetparagraphs) {
			String words[] = snippetparagraph.split(" ");
			if (words.length>10) {
				value = include(snippetparagraph, paragraph);
				if (value) {
					snippetparagraph = snippetparagraph.replace("<em>","");
					snippetparagraph = snippetparagraph.replace("</em>","");
					matchs.add(snippetparagraph);
				}
			}
		}
		String result = null;
		for (String match : matchs) {
			if (result==null || match.length()>result.length()) 
				result = match;
		}
		return result;
	}
	
	public boolean include(String snippet, String paragraph) throws Exception {
		List<String> paragraphtokens = getTokens(paragraph);
		
		snippet = snippet.replace("<em>","");
		snippet = snippet.replace("</em>","");
		
		List<String> snippettokens = getTokens(snippet);
		
		int match = 0;
		for (String token : snippettokens) {
			if (paragraphtokens.contains(token)) 
				match++;
		}
		
		boolean include = match > 0.70*snippettokens.size(); 
		
		return include;
	}

	
	private SolrQuery getMltQuery() {
		SolrQuery query = new SolrQuery();
		query.setMoreLikeThis(true);
		ObjectId id = new ObjectId(getContent());
		//String q = "{!mlt qf=portaltext maxntp=500000 mindf=5}"+id.toString();

		//String q = "{!mlt qf=portaltext mintf=4 mindf=5}"+id.toString();
		String q = "{!mlt qf=portaltext mintf=5 mindf=5}"+id.toString();
		

		query.setQuery(q);
	    query.setMoreLikeThisMinDocFreq(5);
	    query.setIncludeScore(true);
	    query.setMoreLikeThisFields("portaltext");
	    query.addFilterQuery("head:true");
	    query.addFilterQuery("state:1");
	    query.addFilterQuery("domain:"+String.valueOf(getContent().getDomain().getId()));
	    int maxResults = 20;
	    query.setRows(maxResults);
	    return query;
	}
	
	private SolrQuery getMltQuery2() {
		SolrQuery query = new SolrQuery();
		query.setMoreLikeThis(true);
		ObjectId id = new ObjectId(getContent());
		String q = "{!mlt qf=portaltext, metainfo mindf=5}"+id.toString();
		query.setQuery(q);
	    query.setMoreLikeThisMinDocFreq(5);
	    query.setIncludeScore(true);
	    query.setMoreLikeThisFields("portaltext, metainfo");
	    //query.addFilterQuery("id:kbfileimpl#346059528");
	    int maxResults = 20;
	    query.setRows(maxResults);
	    return query;
	}

	
	private SolrQuery getPQuery(IModel<Content> model, String text) {
	    SolrQuery query = new SolrQuery();
	    query.setQuery("text:"+text);
		//query.set("df", "text");
		query.setHighlight(true);
		query.setHighlightFragsize(1000);
		query.set("hl.maxAnalyzedChars", "200000");		
		query.set("defType", "dismax");
		query.set("qf", "text");
		query.set("mm", "50%");
	    query.setIncludeScore(true);
		query.setFields("id", "score");
		KBFile file = (KBFile)((ResourceContainer)model.getObject()).getResources().get(0);
		ObjectId id = new ObjectId(file);
		String q = "id:"+id.toString();
		query.addFilterQuery(q);
		int maxResults = 20;
		query.setRows(maxResults);
		return query;
	}
	
	private SolrQuery getAllPQuery(String text) {
	    SolrQuery query = new SolrQuery();
	    query.setQuery("portaltext:"+text);
		//query.set("df", "text");
		query.setHighlight(true);
		query.setHighlightFragsize(1000);
		query.set("defType", "dismax");
		query.set("qf", "portaltext");
		query.set("mm", "50%");
	    query.setIncludeScore(true);
		query.setFields("id", "score");
	    query.addFilterQuery("head:true");
	    query.addFilterQuery("state:1");
		int maxResults = 20;
		query.setRows(maxResults);
		return query;
	}


	@Override
	public void onDetach() {
		super.onDetach();
		analyzer = null;
		if (model!=null)
			model.detach();
	}
	
	private Content getContent(PageParameters parameters) {
		Content content = null;
		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty()) {
			content = (Content) getContentDao().findContentById(Long.valueOf(id.toString()));
		}	
		return content;
	}
	
	private List<String> getTokens(String text) throws Exception {
		List<String> tokens = new ArrayList<String>();
		TokenStream tokenStream = getAnalyzer().tokenStream("text", text);
		CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
		tokenStream.reset();
		while(tokenStream.incrementToken()) {
			tokens.add(attr.toString());
		}
		tokenStream.close();
		return tokens;
	}
	
	private Analyzer getAnalyzer() throws Exception {
		if (analyzer==null) {
			Resource resource = new ClassPathResource("kbee\\words.csv");
			FileReader wordsreader = new FileReader(resource.getFile());
			analyzer = new StandardAnalyzer(wordsreader);
		}
		return analyzer;
	}
	
	protected Index getIndex() {
		Index index = (JavaIndex)getContent().getDomain().getService(JavaIndexerService.class).getIndex();
		index =  ((IndexProxy)index).getIndex();
		return index;
	}
	
	protected Index getFileIndex() {
		Index index = (JavaIndex)getContent().getDomain().getService(FileIndexerService.class).getIndex();
		index =  ((IndexProxy)index).getIndex();
		return index;
	}

}