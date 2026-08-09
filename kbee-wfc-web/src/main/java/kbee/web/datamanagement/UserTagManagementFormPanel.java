package kbee.web.datamanagement;

import com.novamens.content.command.Command;
import com.novamens.content.model.*;
import com.novamens.kbee.content.command.ReclassifyContentCommand;
import com.novamens.kbee.content.command.ReclassifyMemberCommand;
import com.novamens.kbee.content.command.TagOperation;
import com.novamens.wicket.model.ObjectModel;
import org.apache.wicket.model.IModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserTagManagementFormPanel extends TagManagementFormPanel {

	private static final long serialVersionUID = 1L;
	
	IModel<DataSet> dataSet = null;

    public UserTagManagementFormPanel(String id) {
        super(id);
    }


    public String getHistoryKey() {
    	return "user";
    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();
    }

    @Override
    protected List<TagManagementAction> getPossibleTagManagementActions() {
        List<TagManagementAction> res = new ArrayList<>();
        TagManagementTagAction tagManagementTagAction = new TagManagementTagAction("tagAction") {
            public Object getModifierInstance() {
                final TagOperation tagOperation = this.getTagOperation();
                if (getTagType() != null) {
                    if (getTagType() == TagType.classifier) {
                        final DataSetMember datasetMember = tagOperation != TagOperation.remove ? getDatasetMember() : null;
                        final String macro = tagOperation != TagOperation.remove ? getMacro() : null;
                        if (getUseMacro())
                            return new ReclassifyMemberCommand.DataSetClassifierTagModifier((Classifier) getTagElement(), macro, tagOperation);
                        else
                            return new ReclassifyMemberCommand.DataSetClassifierTagModifier((Classifier) getTagElement(), datasetMember, tagOperation);
                    } else if (getTagType() == TagType.attribute) {
                        final String attributeValue = tagOperation != TagOperation.remove ? getAttributeValue() : null;
                        final String macro = tagOperation != TagOperation.remove ? getMacro() : null;
                        if (getUseMacro())
                            return new ReclassifyMemberCommand.DataSetAttributeTagModifier((Attribute) getTagElement(), macro, tagOperation);
                        else
                            return new ReclassifyMemberCommand.DataSetAttributeTagModifier((Attribute) getTagElement(), attributeValue, tagOperation);
                    }
                }
                throw new RuntimeException("Invalid Tag Type.");
            }
        };

        final List<DataSet> usersDSs = getContentDao().getDataSets("User", (Long) getDomain().getId());
        if(!usersDSs.isEmpty()) {
            setDataSet(usersDSs.get(0));

            List<ModelElement> modelElements = (List<ModelElement>) (List<?>) dataSet.getObject().getClassifiers();
            final List<Attribute> attributes = dataSet.getObject().getAttributes().stream().map(atr -> atr.getAttribute()).collect(Collectors.toList());
            modelElements.addAll((List<ModelElement>) (List<?>) attributes);
            tagManagementTagAction.setTagElementsTemplates(modelElements);
        }

        res.add(tagManagementTagAction);
        return res;
    }


    @Override
    protected Command getCommand() {

        Long domainID = (Long) getDomain().getId();
        Long dataSetID = (Long) getDataSet().getId();

        List<ReclassifyMemberCommand.DateSetMemberTagModifier> dateSetMemberModifiers  = new ArrayList<>();
        dateSetMemberModifiers.add((ReclassifyMemberCommand.DateSetMemberTagModifier) getTagManagementAction().getModifierInstance());
        ReclassifyMemberCommand reclassifyMemberCommand = new ReclassifyMemberCommand(dataSetID, dateSetMemberModifiers, domainID, getCondition());

        reclassifyMemberCommand.setUsrId((Long) getSessionUser().getId());
        return reclassifyMemberCommand;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (dataSet != null)
            dataSet.detach();
    }


    public DataSet getDataSet() {
        return dataSet != null ? dataSet.getObject() : null;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = new ObjectModel<>(dataSet);
    }

}
