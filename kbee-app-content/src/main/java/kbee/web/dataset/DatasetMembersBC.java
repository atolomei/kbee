package kbee.web.dataset;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetType;
import com.novamens.content.user.UserService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.DataSetMembersBC;
import kbee.web.nav.DataSetMembersSectionBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.SeparatorBC;
import kbee.web.nav.SettingsDropDownBC;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.List;

public class DatasetMembersBC extends MenuBreadCrumbPanel<Void> {
	private static final long serialVersionUID = 1L;
	
	IModel<DataSet> datasetmodel;

    public DatasetMembersBC(IModel<DataSet> datasetmodel) {
        this.datasetmodel = datasetmodel;
        this.addElement(new SettingsDropDownBC());
        DropDownMenuBC<?> dd = new DropDownMenuBC<>();
        dd.addElement(new BCElement("bc.dataset.members"), true);
        dd.addElement(new DataSetMembersSectionBC());
        dd.addElement(new SeparatorBC());
        for (DataSet ds : getDataSets())
            dd.addElement(new DataSetMembersBC( new ObjectModel<DataSet>(ds)));

        this.addElement(dd);
        this.addElement(new BCElement(new Model<String>(datasetmodel.getObject().getName())));
    }

    @Override
    public void onDetach() {
    	super.onDetach();
    	if (datasetmodel!=null)
    		datasetmodel.detach();
    	
    }
    public List<DataSet> getDataSets() {
        ArrayList<DataSet> datasetlist = new ArrayList<DataSet>();
        for (DataSet dataset : getContentDao().getDataSets(ServiceLocator.getService(UserService.class).getDomain())) {
            if (dataset.getDataSetType() == DataSetType.STRING ||
                    dataset.getDataSetType() == DataSetType.EXTERNAL ||
                    dataset.getDataSetType() == DataSetType.ENTITY ||
                    dataset.getDataSetType() == DataSetType.LABEL ||
                    dataset.getDataSetType() == DataSetType.SECURED ||
                    dataset.getDataSetType() == DataSetType.PEOPLE)
                datasetlist.add(dataset);
        }
        return datasetlist;
    }

    protected ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }
}
