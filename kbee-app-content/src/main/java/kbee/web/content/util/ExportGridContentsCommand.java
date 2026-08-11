package kbee.web.content.util;


import java.io.IOException;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hibernate.SessionFactory;

import com.novamens.content.command.CommandState;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.command.AbstractCommand;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;

import kbee.web.console.AbstractConsole;
import kbee.web.console.ExpandedPanel;




public class ExportGridContentsCommand<T> extends AbstractCommand {
	
	static Logger logger = LogManager.getLogger(ExportGridContentsCommand.class.getName());

	public class Tuple {
		public Classifier clasi;
		public DataSetMember member;
		
		public Tuple(Classifier clasi, DataSetMember member) {
			this.clasi=clasi;
			this.member=member;
		}
	}

	
	@SuppressWarnings("unused")
	private class ColumnsProvider implements IDataProvider<ICellPopulator<SearchResult>> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Iterator<ICellPopulator<SearchResult>> iterator(long first, long count) {
			List<ICellPopulator<SearchResult>> populators = new ArrayList<ICellPopulator<SearchResult>>((int)count);
			for (GridColumn<SearchResult, String> column : getColumns()) {
				populators.add(column);
			}
			return populators.iterator(); 
		}
		
		@Override
		public long size() {
			return getColumns().size();
		}
		
		public IModel<ICellPopulator<SearchResult>> model(ICellPopulator<SearchResult> object) {
			return new Model<ICellPopulator<SearchResult>>(object);
		}
		
		@Override
		public void detach() {
		}
	};
	
	
	private AbstractConsole<T> console;
	
	private long total = 0;
	
	private Serializable domainId = null;
	
	@SuppressWarnings("unused")
	private Domain domain = null;
	
	@SuppressWarnings("unused")
	private SessionFactory sf;
	
	private int max_to_export = -1;	


	public ExportGridContentsCommand(AbstractConsole<T> console) {
		setName("Export Grid Contents Command " + console.getName());
		this.console=console;
	}
	
	
	public GridPanel<T> getGrid() {
		return (GridPanel<T>) this.getConsole().getBrowser().getPanel(GridPanel.class);
	}
	
	
	public AbstractConsole<T> getConsole() {
		return this.console;
	}
	
	
	public List<GridColumn<SearchResult, String>> getColumns() {
		return getConsole().getColumns();
	}
	
	
	protected String getExportSubdir() {
		return "gridxp";
	}
	
	protected String getZipFileNamePrefix() {
		return "gridexport-";
	}
	

	@Override
	public void execute() {
		executeTask();
	}
	
	
	public void setDomainId(Serializable id) {
		domainId = id;
	}
	
	
	public Serializable getDomainId() {
		return domainId;
	}
	
	
	public void setDomain(Domain domain) {
		this.domain = domain;
		domainId = domain.getId();
	}
	
	public void setMaxToExpor(int max) {
		this.max_to_export=max;
	}

	
	public int getMaxToExport() {
		return this.max_to_export;
	}

	
	protected void executeTask() {

		setDateStarted(OffsetDateTime.now());
		setProgress(0);
		
		try {
			sf = com.novamens.hibernate.session.Session.open();
			export();
			// compress();
			
		} finally {
			com.novamens.hibernate.session.Session.close();	
			setStatusInfo("DB Session closed.");
		}
	}

	
	protected void export() {
		
		total = 0;
		
		try {

				initExporter();
			
				if (getGrid()==null) {
					logger.error("grid is null");
					this.setResultComments("exporter is null");
					this.setState(CommandState.ERROR);
					return;
				}
			
				DataView<SearchResult> dataview = getGrid().getDataView();
				IDataProvider<SearchResult> provider= dataview.getDataProvider();
				
				total=provider.size();
				
				if (total==0) {
					this.setState(CommandState.COMPLETED);
					this.setProgress(100);
					return;
				}
				
				Iterator it = dataview.iterator();
				
				int progress = 0;
				int counter = 0;
				
				logger.info("Processing: " + String.valueOf(total));
				
				
				while (it.hasNext() && (getMaxToExport()==-1 || (counter<getMaxToExport()))) {
					try {
						Object ob = it.next();
						logger.info(ob.getClass().getName());
						Item<SearchResult> item = (Item<SearchResult>) ob;
						ExpandedPanel<?> panel = new ExpandedPanel("id", getConsole(), item.getModel());
						
					} catch (RuntimeException  e) {
						logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					}
					counter++;
					if (total>0) {
						//progress = 50 * counter/total;
					}
					this.setProgress(progress);
				}
				
				Thread.sleep(1000);
				this.setProgress(50);
				
		}
		catch (Throwable e) {
				logger.error(e.getClass().getName(), e);
				this.setResult(e.getClass().getSimpleName());
				this.setResultDetails(e.getMessage());
				setState(CommandState.ERROR);
				setDateTerminated(OffsetDateTime.now());
				
		
		} finally {
		}
	}
	
	@Override
	public String toString() {
		
		StringBuilder str = new StringBuilder();
		str.append(getClass().getSimpleName());
		if(getConsole()!=null) {
			str.append(" | ");
			str.append(getConsole().getName() + " | "+ getConsole().getQuery().toString());
		}
		return str.toString();	
	}



	
	protected void initExporter() throws IOException {
	}


}
