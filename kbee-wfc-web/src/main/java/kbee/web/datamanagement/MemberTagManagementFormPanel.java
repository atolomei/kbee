package kbee.web.datamanagement;

import com.novamens.content.command.Command;
import com.novamens.content.model.*;
import com.novamens.kbee.content.command.ReclassifyContentCommand;
import com.novamens.kbee.content.command.ReclassifyMemberCommand;
import com.novamens.kbee.content.command.TagOperation;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.model.ObjectModel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MemberTagManagementFormPanel extends TagManagementFormPanel {


    List<IModel<DataSet>> dataSets = new ArrayList<>();
    IModel<DataSet> dataSet = null;

    private List<IModel<DataSetMember>> dm_list;

    public void setDataSetMemberSelection(List<IModel<DataSetMember>> list) {
        this.dm_list = list;
    }

    public List<IModel<DataSetMember>> getDataSetMemberSelection() {
        return this.dm_list;
    }

    public MemberTagManagementFormPanel(String id, List<IModel<DataSetMember>> list) {
        super(id);
        this.dm_list = list;
    }


    protected boolean isIQLVisible() {
        return (getDataSetMemberSelection() == null);
    }

    protected IModel<String> getTagHelpText() {
        return new StringResourceModel("tag-member.help", this, null);
    }

    public String getHistoryKey() {
        return "member";
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        setDataSets(getContentDao().getDataSets(getDomain()));

        ChoiceField<DataSet> contentTemplateChoiceField = new ChoiceField<DataSet>("dataSet", new PropertyModel<DataSet>(this, "dataSet"), new PropertyModel<List<DataSet>>(this, "dataSets")) {
            private static final long serialVersionUID = 1L;

            public void onUpdate(AjaxRequestTarget target) {
                setDataSet(this.getValue());
                refreshActions(target);
            }

            @Override
            protected String getDisplayValue(DataSet value) {
                return value.getDisplayName();
            }

            @Override
            public boolean isVisible() {
                return getDataSetMemberSelection() == null;
            }

            @Override
            public boolean isRequired() {
                return getDataSetMemberSelection() == null;
            }
        };
        form.add(contentTemplateChoiceField);
    }


    @Override
    protected Command getCommand() {

        Long domainID = (Long) getDomain().getId();
        Long dataSetID = (Long) getDataSet().getId();

        List<ReclassifyMemberCommand.DateSetMemberTagModifier> dateSetMemberModifiers = new ArrayList<>();
        dateSetMemberModifiers.add((ReclassifyMemberCommand.DateSetMemberTagModifier) getTagManagementAction().getModifierInstance());
        ReclassifyMemberCommand reclassifyMemberCommand = new ReclassifyMemberCommand(dataSetID, dateSetMemberModifiers, domainID, getCondition());


        if (getDataSetMemberSelection() != null) {
            List<DataSetMember> list = new ArrayList<DataSetMember>();
            dm_list.forEach(c -> list.add(c.getObject()));
            reclassifyMemberCommand.setSelection(list);
        }
        reclassifyMemberCommand.setUsrId((Long) getSessionUser().getId());
        return reclassifyMemberCommand;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (dataSet != null)
            dataSet.detach();

        if (dataSets != null)
            dataSets.stream().forEach(c -> c.detach());

        if (dm_list != null)
            dm_list.forEach(c -> c.detach());

    }


    public DataSet getDataSet() {
        return dataSet != null ? dataSet.getObject() : null;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = new ObjectModel<>(dataSet);
    }

    public List<DataSet> getDataSets() {
        return this.dataSets.stream().map(o -> o.getObject()).collect(Collectors.toList());
    }

    public void setDataSets(List<DataSet> dataSets) {
        this.dataSets = dataSets.stream().map(o -> new ObjectModel<>(o)).collect(Collectors.toList());
        this.dataSets.sort(Comparator.comparing(p -> p.getObject().getDisplayName()));
    }

    @Override
    protected List<TagManagementAction> getPossibleTagManagementActions() {
        List<TagManagementAction> res = new ArrayList<>();
        TagManagementTagAction tagManagementTagAction = new TagManagementTagAction("tagAction") {
            @Override
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
        final DataSet dataSet = getDataSet();
        if (dataSet != null) {
            List<ModelElement> modelElements = (List<ModelElement>) (List<?>) dataSet.getClassifiers();
            final List<Attribute> attributes = dataSet.getAttributes().stream().map(atr -> atr.getAttribute()).collect(Collectors.toList());
            modelElements.addAll((List<ModelElement>) (List<?>) attributes);

            tagManagementTagAction.setTagElementsTemplates(modelElements);
        }
        res.add(tagManagementTagAction);

        return res;
    }
}
