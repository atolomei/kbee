package com.novamens.content.web.console.markup;


import java.util.ArrayList;
import java.util.List;

import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.grid.*;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.document.TreeFile;
import com.novamens.content.model.Attribute;
import com.novamens.content.web.console.markup.searchselector.AdvancedSearchEmailSelectorPanel;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.logging.SendEmailEvent;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.AuditConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;

import kbee.web.query.EmailHibernateLogQuery;


import org.danekja.java.util.function.serializable.SerializableSupplier;



public abstract class AuditEmailConsole extends AbstractFacetedConsole<SendEmailEvent> implements AuditConsole {
					
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static transient kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AuditEmailConsole.class.getName());

 	private List<GridColumn<SearchResult,String>> columns;
 	
	public AuditEmailConsole(Query query) {
		super("emaillog", query);
		setOutputMarkupId(true);
	}
	
	
	@Override
	 protected  IModel<SendEmailEvent> getModel(SendEmailEvent object) {
			return new ObjectModel<SendEmailEvent>(object, true);
	}
	

	protected String getIcon(IModel<SendEmailEvent> model) {
		return null;
	}
	
	protected boolean isDefaultTopPanelVisible() {
		return true;
	}
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
	}

	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
	}

	protected boolean isSavedQueriesEnabled() {
		return false;
	}

	/**
	 * 
 	 */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (columns!=null)
			return columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();

		SerializableSupplier<String> formatSupplier = () -> this.getBrowser().getPanel(GridPanel.class).getDateFormat();
		DateKbeeColumn<SendEmailEvent> executedColumn = new DateKbeeColumn<SendEmailEvent>("executed", getLabel("executedcolumn"), (obj)-> obj.getTime(), formatSupplier);
		columns.add(executedColumn);

		LinkPredicateKbeeGridColumn<SendEmailEvent> titleColumn =
				new LinkPredicateKbeeGridColumn<SendEmailEvent>("title",getLabel("titlecolumn"), obj->obj.getSubject(), obj->getModel(obj));
		titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
		columns.add(titleColumn);



		KbeePredicateGridColumn<SendEmailEvent> resultColumn = new KbeePredicateGridColumn<SendEmailEvent>("result", getLabel("resultcolumn"), (obj) -> obj.getResult() ) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				String res = super.getLabelModel(object).getObject();
				if (res!=null && !(res.toLowerCase().equals("succesful") || res.toLowerCase().equals("ok"))) {
					res = "<span class=\"no\">" + res + "</span>";
				}
				else if (res!=null) {
					res = "<span class=\"yes\">" + res + "</span>";
				}
				return new Model<String>(res);
			}

			@Override
			public IModel<String> getCellAsString(SearchResult result) {
				return super.getLabelModel(result);
			}
		};
		resultColumn.setContextKey(this.getName() + resultColumn.getContextKey());
		columns.add(resultColumn);

		KbeePredicateGridColumn<SendEmailEvent> fromColumn = new KbeePredicateGridColumn<>("from", getLabel("fromcolumn"), (obj) -> obj.getFrom());
		fromColumn.setContextKey(this.getName() + fromColumn.getContextKey());
		columns.add(fromColumn);

		KbeePredicateGridColumn<SendEmailEvent> toColumn = new KbeePredicateGridColumn<>("to", getLabel("tocolumn"), (obj) -> obj.getTo());
		toColumn.setContextKey(this.getName() + toColumn.getContextKey());
		columns.add(toColumn);

		KbeePredicateGridColumn<SendEmailEvent> textColumn = new KbeePredicateGridColumn<>("text", getLabel("textcolumn"), (obj) -> obj.getHTMLText());
		textColumn.setContextKey(this.getName() + textColumn.getContextKey());
		textColumn.setPreferred(false);
		textColumn.setOnlyForExpandedHitPanel(true);
		columns.add(textColumn);

		KbeePredicateGridColumn<SendEmailEvent> userColumn = new KbeePredicateGridColumn<>("user", getLabel("usercolumn"), (obj) -> getUserColumnModel(obj).getObject());
		userColumn.setContextKey(this.getName() + userColumn.getContextKey());
		columns.add(userColumn);


		KbeePredicateGridColumn<SendEmailEvent> auditRColumn = new KbeePredicateGridColumn<>("audit-resource", getLabel("audit-resource"),  (obj) -> obj.getAuditResourceKBFileId()!=null? obj.getAuditResourceKBFileId().toString():"");
		auditRColumn.setPreferred(false);
		auditRColumn.setContextKey(this.getName() + auditRColumn.getContextKey());
		columns.add( auditRColumn);

		KbeePredicateGridColumn<SendEmailEvent> gColumn = new KbeePredicateGridColumn<>("generator-action", new Model<String>("Generated by"), (obj) -> obj.getGeneratorAction());
		gColumn.setContextKey(this.getName() + gColumn.getContextKey());
		gColumn.setPreferred(true);
		columns.add(gColumn);
		
		
		KbeePredicateGridColumn<SendEmailEvent> idColumn = new KbeePredicateGridColumn<>("id", getLabel("idcolumn"), (obj) -> String.valueOf(obj.getId()));
		idColumn.setContextKey(this.getName() + idColumn.getContextKey());
		idColumn.setPreferred(false);
		columns.add(idColumn);
  		

		KbeePredicateGridColumn<SendEmailEvent> attachmentsColumn = new KbeePredicateGridColumn<SendEmailEvent>("attachments", getLabel("attachmentscolumn"), (obj) -> obj.getAttachments());
		attachmentsColumn.setContextKey(this.getName() + attachmentsColumn.getContextKey());
		attachmentsColumn.setOnlyForExpandedHitPanel(true);
		attachmentsColumn.setVisible(false);
		columns.add(attachmentsColumn);
		

  		if (isDomainKbee()) {
			KbeePredicateGridColumn<SendEmailEvent> domainColumn = new KbeePredicateGridColumn<>("domain", getLabel("domaincolumn"), (obj) -> obj.getDomain().getName());
			domainColumn.setContextKey(this.getName() + domainColumn.getContextKey());
			domainColumn.setPreferred(false);
			columns.add(domainColumn);
		}
 		return this.columns;
	}


	private IModel<String> getUserColumnModel(SendEmailEvent object) {
		if (object.getEventUser()!=null)
			return new Model<String>(object.getEventUser().getFirstLastName());
		return new Model<String>("");
	}

	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<SendEmailEvent> browser) {
		return new ArrayList<>();
	}

	@Override
	protected boolean hasExpander() {
		return true;
	}
 
	@Override
	protected boolean hasTopPanel() {
		return true;
	}

	@Override
	protected Panel getTopPanel() {
		return new AdvancedSearchEmailSelectorPanel("top");
	}
	
	@Override
	protected Panel getMenu(IModel<SendEmailEvent> model) {
		return null;
	}
	
	protected Panel getPanel(IModel<SendEmailEvent> model) {
		return new ExpandedPanel<SendEmailEvent>("editor", this, model);
	}

	protected Panel getPanel(IModel<SendEmailEvent> model, List<String> snippets) {
		return new ExpandedPanel<SendEmailEvent>("editor", this, model, snippets);
	}
	
	
	@Override
	protected boolean isMyListsEnabled() {
		return false;
	}
	
	@Override
	protected boolean isFiltersEnabled() {
		return false;
	}

	@Override
	protected boolean isSelectionEnabled() {
		return false;
	}

	@Override
	public Query newQuery() {
		return new EmailHibernateLogQuery();
	}

}
