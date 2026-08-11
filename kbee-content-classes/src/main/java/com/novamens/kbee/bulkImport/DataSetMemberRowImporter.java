package com.novamens.kbee.bulkImport;


import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.*;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.DataSetService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.util.KeyValue;
import org.apache.wicket.validation.ValidationError;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class DataSetMemberRowImporter implements RowEntityLoader, Serializable {

    private enum FixedFields {
        name("Name",0);

        private String label;
        private int index;

        FixedFields(String label, int index) {
            this.label = label;
            this.index = index;
        }

        public String getLabel() {
            return label;
        }

        public int getIndex() {
            return index;
        }
    }

    Long dataSetId;

    public DataSetMemberRowImporter(DataSet dataSet) {
        this.dataSetId = (Long) dataSet.getId();
    }

    @Override
    public List<EntityRowColumnsDefinition> getEntityRowColumnsDefinitions() {
        List<EntityRowColumnsDefinition> columnDefinitions = new ArrayList<>();
        Integer idx = 0;
        columnDefinitions.add(new EntityRowColumnsDefinition(idx++, DataSetMemberRowImporter.FixedFields.name.name(), DataSetMemberRowImporter.FixedFields.name.getLabel(), EntityRowColumnsDefinition.ColumnType.NATIVE));
        DataSet dataSet = getDataSet();
        idx = fillClassifierColumns(dataSet, columnDefinitions, idx);
        idx = fillAttributeColumns(dataSet, columnDefinitions, idx);

        return columnDefinitions;
    }

    private Integer fillAttributeColumns(DataSet dataSet, List<EntityRowColumnsDefinition> columnDefinitions, Integer idx) {
        for (AttributeTemplate atrTemp : dataSet.getAttributes()) {
            Attribute atr = atrTemp.getAttribute();
            columnDefinitions.add(new EntityRowColumnsDefinition(idx++, EntityRowColumnsDefinition.ColumnType.ATTRIBUTE.getColumnPrefix() + atr.getId(), atr.getName(), EntityRowColumnsDefinition.ColumnType.ATTRIBUTE));
        }
        return idx;
    }

    private Integer fillClassifierColumns(DataSet dataSet, List<EntityRowColumnsDefinition> columnDefinitions, Integer idx) {
        for (Classifier clf : dataSet.getClassifiers()) {
            if (ObjectState.ENABLED.equals(clf.getState())) {
                String columnKey = EntityRowColumnsDefinition.ColumnType.CLASSIFIER.getColumnPrefix() + clf.getId();
                EntityRowColumnsDefinition cd = new EntityRowColumnsDefinition(idx++, columnKey, clf.getName(), getClassifierValues(clf), EntityRowColumnsDefinition.ColumnType.CLASSIFIER);
                columnDefinitions.add(cd);
            }
        }
        return idx;
    }

    private List<KeyValue<String>> getClassifierValues(Classifier clf) {
        List<DataSetMember> members = getContentDao().getMembers(clf.getDataSet(), null);
        List<KeyValue<String>> possibleValues = null;
        if (!members.isEmpty()) {
            possibleValues = members.stream().filter(mem -> ObjectState.ENABLED.equals(mem.getState()))
                    .map(mem -> new KeyValue<String>(mem.getDisplayName(), mem.getId().toString())).collect(Collectors.toList());
        }
        return possibleValues;
    }

    protected Domain getDomain() {
        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
    }


    protected ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }

    @Override
    public void create(List<RowEntityValues> rowValues) throws BulkImportException {

        Optional<RowEntityValues> name = findByKey(rowValues, FixedFields.name.name());
        DataSet dataSet = getDataSet();

        if(!name.isPresent()){
            throw new BulkImportException("Invalid DataSet Member value.",FixedFields.name.getIndex());
        }

        DataSetMember member = null;
        if(dataSet.isAggregation()){
            final DataSetService dataSetService = dataSet.getService(DataSetService.class);
            Classifier aggregatorClassifier = dataSetService.getAggregatorClassifier();
            if(aggregatorClassifier != null) {
                final String prefix = EntityRowColumnsDefinition.ColumnType.CLASSIFIER.getColumnPrefix();

                Optional<RowEntityValues> aggregatorValue = findByKey(rowValues, prefix + aggregatorClassifier.getId());
                if (aggregatorValue.isPresent()) {
                    DataSetMember aggregatorValueMember = getContentDao().findMemberByValue(aggregatorClassifier.getDataSet(), aggregatorValue.get().getValue());
                    if(aggregatorValueMember == null){
                        throw new BulkImportException("Could not find aggregator dataset member value", aggregatorValue.get().getColumnIdx());
                    }
                    member = dataSetService.getAggregatedValues(aggregatorValueMember, name.get().getValue());
                }else{
                    throw new BulkImportException("Could not find aggregator dataset member value column", -1);
                }
            }else{
                throw new BulkImportException("Could not find aggregator classifier", -1);
            }
        }else{
            member = getContentDao().findMemberByValue(dataSet, name.get().getValue());
        }



        if (member == null) {
            member= dataSet.createMember();
        }
        member.setStrValue(name.get().getValue());

        final List<String> clfUpdates = loadClassifiersValues(rowValues, member);
        final List<String> atrUpdates = loadAttributeValues(rowValues, member);

        List<String> updateParts = new ArrayList<>();
        updateParts.add("(Bulk import)");
        updateParts.addAll(clfUpdates);
        updateParts.addAll(atrUpdates);

        member.getService(DOMObjectService.class).update(updateParts);
    }


    private Optional<RowEntityValues> findByKey(List<RowEntityValues> bulkRowValues, String key) {
        return bulkRowValues.stream().filter(r -> key.equals(r.getColumnKey())).findFirst();
    }

    private List<String> loadClassifiersValues(List<RowEntityValues> bulkRowValues, Classificable classificable) throws BulkImportException {
        List<String> updatedMembers = new ArrayList<>();
        final String prefix = EntityRowColumnsDefinition.ColumnType.CLASSIFIER.getColumnPrefix();
        final Map<String, List<RowEntityValues>> valuesGrouped = groupValuesByPrefix(prefix, bulkRowValues);

        for (Map.Entry<String, List<RowEntityValues>> stringListEntry : valuesGrouped.entrySet()) {
            final String clfIdStr = stringListEntry.getKey().substring(prefix.length());
            Long clfId = Long.parseLong(clfIdStr);
            Classifier clf = (Classifier) getContentDao().findModelObjectById(Classifier.class, clfId);
            List<DataSetMember> values = new ArrayList<>();
            for (RowEntityValues rowEntityValues : stringListEntry.getValue()) {
                final String memberStrValue = rowEntityValues.getValue();
                final DataSetMember memberByValue = getContentDao().findMemberByValue(clf.getDataSet(), memberStrValue);
                if (memberByValue == null)
                    throw new BulkImportException("Invalid classifierValue.", rowEntityValues.getColumnIdx());
                values.add(memberByValue);
            }
            classificable.setClassification(clf, values);
            updatedMembers.add(clf.getDisplayName());
        }
        return updatedMembers;
    }

    private List<String> loadAttributeValues(List<RowEntityValues> bulkRowValues, Classificable classificable) {
        List<String> updatedAtr = new ArrayList<>();

        final String prefix = EntityRowColumnsDefinition.ColumnType.ATTRIBUTE.getColumnPrefix();
        final Map<String, List<RowEntityValues>> valuesGrouped = groupValuesByPrefix(prefix, bulkRowValues);

        for (Map.Entry<String, List<RowEntityValues>> stringListEntry : valuesGrouped.entrySet()) {
            final String AtrIdStr = stringListEntry.getKey().substring(prefix.length());
            Long atrId = Long.parseLong(AtrIdStr);
            Attribute atr = (Attribute) getContentDao().findModelObjectById(Attribute.class, atrId);
            List<String> values = new ArrayList<>();
            for (RowEntityValues rowEntityValues : stringListEntry.getValue()) {
                values.add(rowEntityValues.getValue());
            }
            classificable.setAttributeValues(atr, values);
            updatedAtr.add(atr.getDisplayName());
        }
        return updatedAtr;
    }



    private DataSet getDataSet(){
        return getContentDao().findDataSetById(this.dataSetId);
    }

    private Map<String, List<RowEntityValues>> groupValuesByPrefix(String prefix, List<RowEntityValues> bulkRowValues) {
        final List<RowEntityValues> atrValues = bulkRowValues.stream().filter(r -> r.getColumnKey().startsWith(prefix)).collect(Collectors.toList());
        return atrValues.stream().collect(Collectors.groupingBy(RowEntityValues::getColumnKey, Collectors.toList()));
    }

    protected ContentSecurityDao getContentSecurityDao() {
        return (ContentSecurityDao) ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
    }

}
