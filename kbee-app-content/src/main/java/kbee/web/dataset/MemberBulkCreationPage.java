package kbee.web.dataset;

import com.novamens.content.base.Content;
import com.novamens.content.model.DataSet;
import com.novamens.kbee.bulkImport.DataSetMemberRowImporter;
import com.novamens.kbee.bulkImport.RowEntityLoader;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;

import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.security.user.BulkCreationPanel;

import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

public class MemberBulkCreationPage extends ApplicationPage<DataSet> {

	private static final long serialVersionUID = 1L;

//	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MemberBulkCreationPage.class.getName());

    private final boolean admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
    final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());


    public MemberBulkCreationPage(DataSet ds) {
        this.setModel(new ObjectModel<>(ds));
    }

    public MemberBulkCreationPage(PageParameters parameters) {
        DataSet ds = getDataSet(parameters);
        this.setModel(new ObjectModel<>(ds));
    }

    private DataSet getDataSet(PageParameters parameters) {
        DataSet dataset = null;
        StringValue id = parameters.get("id");
        if (!id.isNull() && !id.isEmpty()) {
            dataset = (DataSet) getContentDao().findModelObjectById(DataSet.class, id.toLong());
            if (dataset!=null && !dataset.getDomain().equals(getDomain())) {
                dataset = null;
            }
        }
        return dataset;
    }


    @Override
     public void onInitialize() {
    	super.onInitialize();

		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());

        
		PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
		panel.setTitle(new StringResourceModel("batchcreation", this, null));

        final DatasetMembersBC bc = new DatasetMembersBC(this.getModel());
        bc.addElement(new BCElement(new StringResourceModel("batchcreation", this, null)));
        panel.setBreadcrumbPanel(bc);

		setSearchPanel(false);
		setAdvancedSearch(false);
		setSuggester(false);
		
		setPageContentHeader(panel);
		
        if (hasPermissions()) {
        	add(new BulkCreationPanel("editor"){
				private static final long serialVersionUID = 1L;
				@Override
                public RowEntityLoader getRowLoader() {
                    return new DataSetMemberRowImporter(MemberBulkCreationPage.this.getModel().getObject());
                }
            });
        }
        else
            addOrReplace(new ErrorNotAuthorizedPanel<>("editor"));
     }
    
    @Override
    protected boolean hasPermissions() {
        final boolean has_permission = is_root || admin || is_support;
        return has_permission;

    }

}
