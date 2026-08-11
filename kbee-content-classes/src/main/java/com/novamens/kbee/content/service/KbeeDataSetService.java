package com.novamens.kbee.content.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetElementTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntitySet;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.DataSetService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeDataSetElementTemplate;
import com.novamens.kbee.content.model.KbeeEntitySet;
import com.novamens.kbee.content.repository.MemberRepository;
import com.novamens.repository.DomRepository;
import com.novamens.security.User;
import com.novamens.service.LanguageService;
import com.novamens.service.ServiceLocator;

public class KbeeDataSetService implements DataSetService {

    private DataSet dataset;

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeDataSetService.class.getName());

    @Autowired
    private DomRepository<DataSetMember> memberRepository;

    public KbeeDataSetService() {
    }

    public KbeeDataSetService(DataSet dataset) {
        this.dataset = dataset;
    }

    /**
     * @param
     * @return
     */
    @Override
    public Classifier getClassifier(DataSet parent_ds) {

        if (parent_ds == null)
            return null;

        if (parent_ds.isAggregation()) {
            logger.debug(parent_ds.getName() + " can not be a built-in DataSet");
            return null;
        }

        for (Classifier c : getDataSet().getClassifiers()) {
            if (c.getDataSet().equals(parent_ds)) {
                return c;
            }
        }
        logger.debug(parent_ds.getName() + "  ->  is not an Aggregator DataSet of " + getDataSet().getName());
        return null;

    }

    /**
     *
     */
    @Override
    public DataSet getAggregatorDataSet() {
        if (!getDataSet().isAggregation()) {
            logger.debug(getDataSet().getName() +  " -> is not a built in DataSet");
            return null;
        }

        // el unico canonico es el agregador
        for (ModelElementTemplate template : getDataSet().getStructure()) {
            if (template.isCanonical() && template instanceof ClassifierTemplate) {
                return ((ClassifierTemplate) template).getClassifier().getDataSet();
            }
        }
        logger.warn(getDataSet().getName() + " has no Aggregator DataSet.");
        return null;
    }

    /**
     *
     */
    @Override
    public Classifier getAggregatorClassifier() {
        if (!getDataSet().isAggregation()) {
            logger.debug(getDataSet().getName() + " is not a built in DataSet");
            return null;
        }
        // el unico canonico es el agregador
        for (ModelElementTemplate template : getDataSet().getStructure()) {
            if (template.isCanonical() && template instanceof ClassifierTemplate) {
                return ((ClassifierTemplate) template).getClassifier();
            }
        }
        logger.warn(getDataSet().getName() + " has no Aggregator DataSet.");
        return null;
    }

    /**
     *
     */
    @Override
    @Transactional
    public DataSet createAggregation(String name) {

        KbeeEntitySet aggregation = new KbeeEntitySet();
        aggregation.setName(name);
        aggregation.setAlias(parseAlias(name));
        aggregation.setSuggester(false);
        aggregation.setCanonical(false);
        aggregation.setAccessStrategy(AccessStrategy.All);
        aggregation.setAggregation(true);

        aggregation.setDomain(getDataSet().getDomain());
        aggregation.setLastModifiedUser(getSessionUser());
        aggregation.setCreationOffsetDateTime(OffsetDateTime.now());
        aggregation.setLastModifiedOffsetDateTime(OffsetDateTime.now());
        aggregation.setEnabled(true);

        getContentDao().save(aggregation);

        KbeeClassifier classifier = (KbeeClassifier) ServiceLocator.getService(ObjectFactoryService.class).createClassifier(aggregation);
        classifier.setMultiplicity(Multiplicity.M11);

        getContentDao().save(classifier);

        List<DataSetElementTemplate> structure = new ArrayList<DataSetElementTemplate>();
        KbeeDataSetElementTemplate template = new KbeeDataSetElementTemplate();
        template.setClassifier(getMainClassifier());
        template.setMultiplicity(Multiplicity.M11);
        template.setReadOnly(true);
        template.setDomain(getDataSet().getDomain());

        template.setCanonical(true);
        structure.add(template);

        aggregation.setStructure(structure);

        getContentDao().save(aggregation);

        return aggregation;
    }

    /**
     *
     */
    @Override
    @Transactional
    public void deleteAggregation(DataSet dataset) {
        try {
            Classifier classifier = getMainClassifier(dataset);
            DOMObjectService objectService = classifier.getService(DOMObjectService.class);
            objectService.delete();
        } catch (DataIntegrityViolationException | ConstraintException e) {
            logger.error(e);
        } catch (Exception e) {
            logger.error(e);
        }
        try {
            DOMObjectService objectService = dataset.getService(DOMObjectService.class);
            objectService.delete();
        } catch (DataIntegrityViolationException | ConstraintException e) {
            logger.error(e);
        } catch (Exception e) {
            logger.error(e);
        }
    }


    /**
     *
     */
    @Override
    public List<Object> getReferences() {

        List<Object> references = new ArrayList<Object>();

//        try {
//            for (ContentTemplate template : getContentDao().getTemplates(getDataSet().getDomain())) {
//
//                if (template.getSections() != null) {
//                    for (ModelSection section : template.getSections()) {
//                        if (section != null && section.getStructure() != null) {
//                            for (ModelElementTemplate elementTemplate : section.getStructure()) {
//                                if (elementTemplate instanceof ClassifierTemplate &&
//                                        (((ClassifierTemplate) elementTemplate).getClassifier() != null) &&
//                                        (((ClassifierTemplate) elementTemplate).getClassifier().getDataSet() != null) &&
//                                        getDataSet().equals(((ClassifierTemplate) elementTemplate).getClassifier().getDataSet())) {
//                                    references.add(template);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } 
//        catch (Exception e) {
//            logger.error(e);
//        }

        try {

            for (DataSet dataset : getContentDao().getDataSets(getDataSet().getDomain())) {
                if (dataset.getStructure() != null) {
                    logger.debug(dataset.getName() + " | id -> " + dataset.getId().toString());
                    for (ModelElementTemplate elementTemplate : dataset.getStructure()) {
                        if (elementTemplate != null &&
                                elementTemplate instanceof ClassifierTemplate &&
                                ((ClassifierTemplate) elementTemplate).getClassifier() != null &&
                                getDataSet().equals(((ClassifierTemplate) elementTemplate).getClassifier().getDataSet())) {
                            references.add(dataset);

                        }
                    }
                }
            }
        } 
        catch (Exception e) {
            logger.error(e);
        }

        try {
            if (getDataSet().isAggregation()) {
                if (getDataSet().getStructure() != null) {
                    for (ModelElementTemplate elementTemplate : getDataSet().getStructure()) {
                        if (
                                (elementTemplate != null) &&
                                        (elementTemplate instanceof ClassifierTemplate) &&
                                        (((ClassifierTemplate) elementTemplate).getClassifier() != null) &&
                                        (!((ClassifierTemplate) elementTemplate).getMultiplicity().isMultiple())) {
                            references.add(((ClassifierTemplate) elementTemplate).getClassifier().getDataSet());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return references;
    }

    @Override
    public Classifier getMainClassifier() {
        return getMainClassifier(getDataSet());
    }


    /**
     *
     */
    @Override
    public long getTotalMembers() {
        return getContentDao().getTotalMembers(getDataSet());
    }


    Classifier main_classifier = null;


    /**
     * clasificador univoco de referencia al dataset
     **/

    private Classifier getMainClassifier(DataSet dataset) {

        if (main_classifier != null)
            return main_classifier;

        Classifier mainClassifier = null;
        for (Classifier classifier : getContentDao().getClassifiers(getDataSet().getDomain())) {
            if (classifier.getDataSet().equals(dataset)) {
                if (mainClassifier != null) {
                    throw new ContentCreationException("ambiguous classifier");
                }
                mainClassifier = classifier;
            }
        }
        if (mainClassifier == null) {
            throw new ContentCreationException("classifier not found");
        }

        main_classifier = mainClassifier;

        return mainClassifier;
    }


    public DataSet getDataSet() {
        return dataset;
    }

    @Override
    public List<DataSet> getAggregations() {
        return getAggregations(getDataSet());
    }


    public DataSetMember getAggregator(DataSetMember value) {
        if (!getDataSet().isAggregation()) {
            logger.debug(getDataSet().getName() + " must be aggregation");
            return null;
        }

        if (!value.getDataSet().equals(getDataSet())) {
            return null;
        }

        Classifier classifier = getAggregatorClassifier();

        if (classifier == null) {
            return null;
        }

        List<Classification> classification = value.getClassification(classifier);

        if (classification.size() != 1) {
            return null;
        }

        DataSetMember aggregator = classification.get(0).getDataSetMember();

        return aggregator;
    }

    /**
     * <p>Returns a list of DataSetMember of the built-in DataSet.
     * Example:
     * <p>
     * DataSetMember 					-> Southern Cross
     * <p>
     * DataSet buitIn  					-> Unit
     * List<DataSetMember>				-> Unit 1A, Unit 1B,...
     * </p>
     */
    @Override
    public List<DataSetMember> getAggregatedValues(DataSetMember aggregator) {

        if (aggregator == null) {
            logger.debug("aggregator is null");
            return null;
        }

        if (!getDataSet().isAggregation()) {
            logger.debug(getDataSet().getName() + " must be built in");
        }

        if (aggregator.getDataSet().isAggregation()) {
            logger.debug(aggregator.getDataSet().getName() + " can not be built in");
            return null;
        }

        List<DataSetMember> values = getMemberRepository().findAggregationValues(aggregator, getDataSet());

        // DataSetMember member = getMemberRepository().findAggregationByValue(aggregator, getDataSet(), "200A");

//		Classifier aggregatorclassifier = getClassifier(aggregator.getDataSet());
//		
//		if (aggregatorclassifier==null) {
//			logger.debug(getDataSet().getName() +" has no Classifier for Aggregator " + aggregator.getDataSet().getName());
//			return null;
//		}
//		
//		List<Classification> aggregation = getContentDao().getBuiltInClassification(aggregatorclassifier, getDataSet(), aggregator);
//		
//		for (Classification classification : aggregation) {
//			if (classification instanceof KbeeMemberClassification)
//				values.add(((KbeeMemberClassification)classification).getSource());
//			else {
//				logger.debug(" received " + classification.getClass().getName() +" | expected -> " +  KbeeMemberClassification.class.getName());
//			}
//		}

        return values;
    }

    @Override
    public DataSetMember getAggregatedValues(DataSetMember aggregator, String value) {
        if (aggregator == null) {
            logger.debug("aggregator is null");
            return null;
        }

        if (!getDataSet().isAggregation()) {
            logger.debug(getDataSet().getName() + " must be built in");
        }

        if (aggregator.getDataSet().isAggregation()) {
            logger.debug(aggregator.getDataSet().getName() + " can not be built in");
            return null;
        }

        return getMemberRepository().findAggregationByValue(aggregator, getDataSet(), value);
    }
    
    
    @Override
	public List<Role> getRoles() {
		List<Role> roles = getSecurityDao().getRolesByEntitySet((EntitySet)getDataSet());
		return roles;
	}

    /**
     * @param s
     * @return
     */
    protected String parseAlias(String s) {

        if (s == null)
            return null;

        String a0 = s.toLowerCase().replace("ñ", "enie").replace(" de ", "");
        String a1 = StringUtils.stripAccents(ServiceLocator.getService(LanguageService.class).removeStopWords(a0, getDomain().getLocale()));
        String a2 = a1.replaceAll("[ |\\t|\\s|(|)]", "");
        return a2.toLowerCase().trim();
    }

    /**
     *
     */
    protected Domain getDomain() {
        return (Domain) ServiceLocator.getService(UserService.class).getDomain();
    }

    /**
     *
     */
    private ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }


    /**
     * @param da
     * @return
     */
    private List<DataSet> getAggregations(DataSet da) {

        List<DataSet> datasets = new ArrayList<DataSet>();

        if (da == null)
            return datasets;

        List<DataSet> t_datasets = getContentDao().getDataSets(da.getDomain());

        if (t_datasets == null)
            return datasets;

        for (DataSet dataset : t_datasets) {
            if (dataset != null && dataset.isAggregation() && !dataset.equals(da)) {
                try {
                    if (dataset.getStructure() != null) {
                        for (ModelElementTemplate datasettemplate : dataset.getStructure()) {
                            if (datasettemplate instanceof ClassifierTemplate &&
                                    ((ClassifierTemplate) datasettemplate).getClassifier() != null &&
                                    !((ClassifierTemplate) datasettemplate).getMultiplicity().isMultiple() &&
                                    da.equals(((ClassifierTemplate) datasettemplate).getClassifier().getDataSet())) {
                                datasets.add(dataset);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }
        return datasets;
    }

    protected MemberRepository getMemberRepository() {
        return (MemberRepository) memberRepository;
    }

    protected User getSessionUser() {
        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
    }
    
	protected ContentSecurityDao getSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}