package com.novamens.kbee.bulkImport;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.*;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.security.RolesService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.model.KbeeEntityMember;
import com.novamens.kbee.content.user.KbeeUserRole;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.util.KeyValue;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class UserBulkRowImporter implements RowEntityLoader, Serializable {

    private enum FixedFields {
        username("Username"),
        first_name("First Name"),
        last_name("Last Name"),
        email("Email");
        private String label;

        FixedFields(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    @Override
    public List<EntityRowColumnsDefinition> getEntityRowColumnsDefinitions() {
        List<EntityRowColumnsDefinition> columnDefinitions = new ArrayList<>();
        Integer idx = 0;
        columnDefinitions.add(new EntityRowColumnsDefinition(idx++, FixedFields.username.name(), FixedFields.username.getLabel(), EntityRowColumnsDefinition.ColumnType.NATIVE));
        columnDefinitions.add(new EntityRowColumnsDefinition(idx++, FixedFields.first_name.name(), FixedFields.first_name.getLabel(), EntityRowColumnsDefinition.ColumnType.NATIVE));
        columnDefinitions.add(new EntityRowColumnsDefinition(idx++, FixedFields.last_name.name(), FixedFields.last_name.getLabel(), EntityRowColumnsDefinition.ColumnType.NATIVE));
        columnDefinitions.add(new EntityRowColumnsDefinition(idx++, FixedFields.email.name(), FixedFields.email.getLabel(), EntityRowColumnsDefinition.ColumnType.NATIVE));

        UserSet userSet = ServiceLocator.getService(ObjectFactoryService.class).getUserSet();

        for (Classifier clf : userSet.getClassifiers()) {
            if (ObjectState.ENABLED.equals(clf.getState())) {
                String columnKey = EntityRowColumnsDefinition.ColumnType.CLASSIFIER.getColumnPrefix() + clf.getId();
                EntityRowColumnsDefinition cd = new EntityRowColumnsDefinition(idx++, columnKey, clf.getName(), getClassifierValues(clf), EntityRowColumnsDefinition.ColumnType.CLASSIFIER);
                columnDefinitions.add(cd);
            }
        }

        for (AttributeTemplate atrTemp : userSet.getAttributes()) {
            Attribute atr = atrTemp.getAttribute();
            columnDefinitions.add(new EntityRowColumnsDefinition(idx++, EntityRowColumnsDefinition.ColumnType.ATTRIBUTE.getColumnPrefix() + atr.getId(), atr.getName(), EntityRowColumnsDefinition.ColumnType.ATTRIBUTE));
        }

        for (Role role : getSecurityDao().getRoles(getDomain())) {
            if (ObjectState.ENABLED.equals(role.getState())) {
                List<KeyValue<String>> ps;

                if (role instanceof EntityRole) {
                    Classifier clf = ((EntityRole) role).getClassifier();
                    ps = getClassifierValues(clf);
                } else {
                    ps = new ArrayList<>();
                    ps.add(new KeyValue<String>("False", "0"));
                    ps.add(new KeyValue<String>("True", "1"));
                }
                columnDefinitions.add(new EntityRowColumnsDefinition(idx++, EntityRowColumnsDefinition.ColumnType.ROLE.getColumnPrefix() + role.getId(), role.getDisplayName(), ps, EntityRowColumnsDefinition.ColumnType.ROLE));
            }
        }

        return columnDefinitions;
    }

    private List<KeyValue<String>> getClassifierValues(Classifier clf) {
        List<DataSetMember> members = getContentDao().getMembers(clf.getDataSet(), "strvalue");
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

    private ContentSecurityDao getSecurityDao() {
        return (ContentSecurityDao) ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
    }


    @Override
    public void create(List<RowEntityValues> rowValues) throws BulkImportException {

        final Optional<RowEntityValues> usernameNoDomain = findByKey(rowValues, FixedFields.username.name());
        final Set<String> reservedUserNames = ServiceLocator.getService(SecurityContentMgmtService.class).getReservedUserNames();

        if (!usernameNoDomain.isPresent() || reservedUserNames.contains(usernameNoDomain.get().getValue()) || !usernameNoDomain.get().getValue().matches("^[a-zA-Z0-9]+$")) {
            throw new BulkImportException("Invalid username.", usernameNoDomain.get().getColumnIdx());
        }

        String userName = usernameNoDomain.get().getValue() + "@" + getDomain().getName();
        Optional<RowEntityValues> firstName = findByKey(rowValues, FixedFields.first_name.name());
        Optional<RowEntityValues> lastName = findByKey(rowValues, FixedFields.last_name.name());
        Optional<RowEntityValues> email = findByKey(rowValues, FixedFields.email.name());

        UserProfile profile = null;
        Person person = null;

        User user = ServiceLocator.getService(SecurityService.class).findUserByUsername(userName);
        if (user != null) {
            profile = getContentDao().findUserProfileByUser(user);
            person = profile.getPerson();
        }
        if (person == null) {
            person = (Person) ServiceLocator.getService(ObjectFactoryService.class).createUser(userName);
            profile = person.getProfile(UserProfile.class);
        }

        if (firstName.isPresent())
            person.setFirstName(firstName.get().getValue());

        if (lastName.isPresent())
            person.setLastName(lastName.get().getValue());

        if (email.isPresent())
            person.setEmail(email.get().getValue());


        final List<DataSetMember> userMemberSets = getContentDao().findMembersByEntity(profile.getEntity());
        if (userMemberSets.size() != 1) {
            throw new KbeeRuntimeException("Found none or more than one entity for user");
        }
        final DataSetMember userMemberSet = userMemberSets.get(0);
        final List<String> clfUpdates = loadClassifiersValues(rowValues, userMemberSet);
        final List<String> atrUpdates = loadAttributeValues(rowValues, userMemberSet);
        final List<String> rolesUpdated = loadRoles(rowValues, person);

        List<String> updateParts = new ArrayList<>();
        updateParts.add("(Bulk import)");
        updateParts.addAll(clfUpdates);
        updateParts.addAll(atrUpdates);
        updateParts.addAll(rolesUpdated);
        ServiceLocator.getService(SecurityContentMgmtService.class).update(profile, updateParts);
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

    private List<String> loadRoles(List<RowEntityValues> bulkRowValues, Person person) throws BulkImportException {
        List<String> updatedRoles = new ArrayList<>();

        final String prefix = EntityRowColumnsDefinition.ColumnType.ROLE.getColumnPrefix();
        final Map<String, List<RowEntityValues>> valuesGrouped = groupValuesByPrefix(prefix, bulkRowValues);
        final UserProfile profile = person.getProfile(UserProfile.class);

        for (Map.Entry<String, List<RowEntityValues>> stringListEntry : valuesGrouped.entrySet()) {
            final String roleIdStr = stringListEntry.getKey().substring(prefix.length());
            Long roleId = Long.parseLong(roleIdStr);
            Role role = getContentSecurityDao().findRoleById(roleId);

            if(role == null){
                throw new BulkImportException("Invalid role.", stringListEntry.getValue().get(0).getColumnIdx());
            }


            for (RowEntityValues rowEntityValues : stringListEntry.getValue()) {
                if (rowEntityValues.getValue() != null) {
                    boolean add = true;
                    boolean remove = false;
                    DataSetMember memberByValue = null;

                    if (role.isEntity()) {
                        final String memberStrValue = rowEntityValues.getValue();
                        final EntityRole entityRole;
                        if (role instanceof HibernateProxy)
                            entityRole = EntityRole.class.cast(((HibernateProxy) role).getHibernateLazyInitializer().getImplementation());
                        else
                            entityRole = EntityRole.class.cast(role);

                        memberByValue = getContentDao().findMemberByValue(entityRole.getClassifier().getDataSet(), memberStrValue);
                        if(memberByValue == null){
                            throw new BulkImportException("Invalid role entity.", rowEntityValues.getColumnIdx());
                        }
                    } else if (!"true".equals(rowEntityValues.getValue().toLowerCase())) {
                        add = false;
                        if ("false".equals(rowEntityValues.getValue().toLowerCase())) {
                            remove = true;
                        }
                    }

                    DataSetMember finalMemberByValue = memberByValue;
                    Optional<UserRole> prevRole = profile.getRoles().stream()
                            .filter(r -> (r.getRole().getId().equals(role.getId()) && (!role.isEntity() || r.getEntity().getId().equals(finalMemberByValue.getId())))).findFirst();
                    if (add) {

                        if (!prevRole.isPresent()) {
                            KbeeUserRole k_ur = new KbeeUserRole(role, profile.getUser(), (EntityMember) memberByValue);
                            profile.getRoles().add(k_ur);
                            updatedRoles.add("Add Role " + k_ur.getDisplayName());
                        }
                    }else{
                        if(remove){
                            if (prevRole.isPresent()) {
                                profile.getRoles().remove(prevRole.get());
                                updatedRoles.add("Remove Role " + prevRole.get().getDisplayName());
                            }
                        }
                    }
                }
            }
        }
        return updatedRoles;
    }

    private String getRoleDisplayName(UserRole role){
        if(role.getEntity() != null){
            return String.format("%s (%s)", role.getRole().getName(), role.getEntity().getStrValue());
        }else{
            return role.getDisplayName();
        }

    }

    private Map<String, List<RowEntityValues>> groupValuesByPrefix(String prefix, List<RowEntityValues> bulkRowValues) {
        final List<RowEntityValues> atrValues = bulkRowValues.stream().filter(r -> r.getColumnKey().startsWith(prefix)).collect(Collectors.toList());
        return atrValues.stream().collect(Collectors.groupingBy(RowEntityValues::getColumnKey, Collectors.toList()));
    }

    protected ContentSecurityDao getContentSecurityDao() {
        return (ContentSecurityDao) ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
    }
}
