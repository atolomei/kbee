package com.novamens.content.web.deployManagement;

import com.novamens.content.document.TreeFile;
import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.web.integration.LocalFSQuery;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.report.Row;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BackupsConsole extends AbstractFacetedConsole<Person> {
    private List<GridColumn<SearchResult, String>> columns;

    public BackupsConsole(String name, Query query) {
        super(name, query);
    }

	protected  IModel<Person> getModel(Person object) {
		return new ObjectModel<Person>(object, true);
	}

	@Override
	protected String getIcon(IModel<Person> model) {
		return null;
	}

	
    @Override
    public List<GridColumn<SearchResult, String>> getColumns() {
        if (this.columns != null)
            return this.columns;

        this.columns = new ArrayList<GridColumn<SearchResult, String>>();

        {
            KbeePredicateGridColumn<Row> totalApprovalDaysComplianceColumn = new KbeePredicateGridColumn<>("fileName", getLabel("column.fileName"), (row) -> row.get("fileName"));
            totalApprovalDaysComplianceColumn.setContextKey(this.getName() + totalApprovalDaysComplianceColumn.getContextKey());
            columns.add(totalApprovalDaysComplianceColumn);
        }


        return columns;
    }

    @Override
    public Page getConsolePage(Query query, long index) {
        return getConsolePage(query, -1);
    }

    @Override
    protected Panel getMenu(IModel<Person> model) {
        ContextMenuPanel<Person> menu = new ContextMenuPanel<Person>(model);
        menu.setOutputMarkupId(true);
        return menu;
    }
    private String getBackupDirectory(){
        return "C:\\novamens";
    }

    @Override
    public Query newQuery() {
        final LocalFSQuery localFSQuery = new LocalFSQuery();
        localFSQuery.setDirectory(new File(getBackupDirectory()));
        return localFSQuery;
    }
}
