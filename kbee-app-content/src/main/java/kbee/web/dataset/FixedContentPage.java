package kbee.web.dataset;

import com.novamens.content.base.Content;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.support.Tip;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.content.console.ContentBaseConsole;
import kbee.web.content.console.MonitorConsole;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import org.apache.wicket.Page;
import org.apache.wicket.model.Model;

@Deprecated
public class FixedContentPage extends ConsolePage<Content> {
    private static final long serialVersionUID = 1L;
    private final String showMonitorConsoleParam = "showMonitorConsole";

    public FixedContentPage() {
        setPageTitle(getLabel("title"));
    }

    public FixedContentPage(Query query, boolean showMonitorConsole) {
        super(query);
        getPageParameters().add(showMonitorConsoleParam, showMonitorConsole);
        setPageTitle(getLabel("title"));
    }

    @Override
    public ApplicationMenuSection getApplicationMenuSection() {
        return ApplicationMenuSection.GENERAL;
    }

	@Override
    @SuppressWarnings("serial")
    public Console<Content> newConsole(Query query) {

        boolean showMonitorConsole = getShowMonitorConsole();
        if(!showMonitorConsole) {
            return new ContentBaseConsole("library", null, query) {
                private static final long serialVersionUID = 1L;

                @Override
                public Page getConsolePage(Query query, long index) {
                    return new FixedContentPage(query, showMonitorConsole);
                }
            };
        }else{
            return new MonitorConsole(query) {
                @Override
                public Page getConsolePage(Query query, long index) {
                    return new FixedContentPage(query, showMonitorConsole);
                }
            };
        }

    }

    @Override
    protected Page getConsolePage(Query query, long index) {
        return new FixedContentPage(query, getShowMonitorConsole());
    }

    public boolean getShowMonitorConsole(){
        return getPageParameters().getNamedKeys().contains(showMonitorConsoleParam) && getPageParameters().get(showMonitorConsoleParam).toBoolean()== true ;
    }


    @Override
    public void onInitialize() {
        super.onInitialize();

        MenuBreadCrumbPanel<?> bc = new MenuBreadCrumbPanel<>();
        PageContentHeaderPanel<Void> panel = new PageContentHeaderPanel<Void>();
      
        panel.setTitle(new Model<String>("Tag tool Selection"));
        panel.setBreadcrumbPanel(bc);
        setSearchPanel(false);
        setAdvancedSearch(false);
        setSuggester(false);
        setPageContentHeader(panel);
    }




    @Override
    public boolean hasPermissions() {
        return true;
    }


    @Override
    protected String getTipCategory() {
        return Tip.MODEL;
    }

}
