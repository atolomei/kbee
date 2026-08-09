package kbee.web.console;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

import com.novamens.indexer.query.Query;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.repeater.util.Searcher;

public abstract class Console<T> extends KBPanel {
	private static final long serialVersionUID = 1L;
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(Console.class.getName());
	
	private Query query;
	private Searcher searcher = new Searcher();
	private final String name;
	private IModel<String> display_name = null;
	
	public Console(String id, String name, Query query) {
		super(id);
		this.name=name;
		setOutputMarkupId(true);
		setQuery(query);
	}

	public Console(String name, Query query) {
		super("console");
		this.name=name;
		setOutputMarkupId(true);
		setQuery(query);
	}
	
	protected abstract String getDefaultUserPreference(String key);
	
	public abstract BaseBrowser<T> getBrowser();
	
	public String getName() {
		return name;
	}
	
	public Searcher getSearcher() {
		return searcher;
	}
	
	public Query getQuery() {
		return query;
	}
	
	public void setQuery(Query query) {
		this.getSearcher().setQuery(query);
		this.query = query;
	}

	public IModel<String> getDisplayName() {
		if (this.display_name!=null)
			return this.display_name;
		this.display_name = new StringResourceModel("console.displayname", Console.this, null);
		return this.display_name; 
	}

	public String getDownloadFileName(){
		return this.getDisplayName().getObject().replaceAll("[ |\\t|\\s|(|)]", "-").toLowerCase()+"-"+(new SimpleDateFormat("YYYY-MM-dd").format(new Date()));
	}
	
	@Override
	public void onDetach() {
		try {
			if (this.searcher!=null)
				this.searcher.detach();
		} 
		catch (Exception e) {
			logger.error(e);
		}
		super.onDetach();
	}


	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
	
	protected void addListeners() {
	}

	/**
	 * 
	 * URL from HTTP Request received
	 * Wicket based
	 * 
	 * @return
	 */
	protected String getServerUrl() {
		String protocol =((WebRequest)RequestCycle.get().getRequest()).getUrl().getProtocol();
		String host =((WebRequest)RequestCycle.get().getRequest()).getUrl().getHost();
		Integer iport =((WebRequest)RequestCycle.get().getRequest()).getUrl().getPort(); 
		String port = (iport.equals(80) || iport.equals(443) ? "":  ( ":" + iport.toString()) );
		return protocol +"://" + host + port;
	}
}
