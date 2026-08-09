package kbee.web.datamanagement;


import com.novamens.content.base.Content;
import com.novamens.content.command.Command;
import com.novamens.content.model.*;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.command.ReclassifyContentCommand;
import com.novamens.kbee.content.command.TagOperation;
import com.novamens.kbee.content.command.TagTargetDocumentsPlace;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.ConsolePage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.service.ApplicationSiteMapService;

// import kbee.web.objectlist.FixedContentPage;
// import kbee.web.objectlist.ListQuery;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import org.apache.wicket.markup.html.basic.Label;


import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class ContentTagManagementFormPanel extends TagManagementFormPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<IModel<ContentTemplate>> contentTemplates = new ArrayList<>();
    private IModel<ContentTemplate> contentTemplate = null;

    private TagTargetDocumentsPlace tagTargetDocumentsPlace = TagTargetDocumentsPlace.library;
    private IModel<ContentTemplate> newContentTemplate = null;

    private List<IModel<Content>> list;

    
    public void setSelection(List<IModel<Content>> list) {
        this.list = list;
    }

    public List<IModel<Content>> getSelection() {
        return this.list;
    }

    public ContentTagManagementFormPanel(String id, List<IModel<Content>> list) {
        super(id);
        setSelection(list);
    }

    protected boolean isIQLVisible() {
        return (getSelection() == null);
    }


    protected IModel<String> getTagHelpText() {
        return new StringResourceModel("tag-content.help", this, null);
    }


    public String getHistoryKey() {
        return "content";
    }


    @Override
    protected void onInitialize() {
        super.onInitialize();

        super.setOutputMarkupId(true);

        setContentTemplates(getContentDao().getTemplates(getDomain()));
        ChoiceField<ContentTemplate> contentTemplateChoiceField = new ChoiceField<ContentTemplate>("contentTemplate", new PropertyModel<ContentTemplate>(this, "contentTemplate"), new PropertyModel<List<ContentTemplate>>(this, "contentTemplates")) {
            private static final long serialVersionUID = 1L;

            public void onUpdate(AjaxRequestTarget target) {
                setContentTemplate(this.getValue());

                refreshActions(target);
            }

            @Override
            protected String getDisplayValue(ContentTemplate value) {
                return value.getName();
            }

            @Override
            public boolean isVisible() {
                return getSelection() == null;
            }

            @Override
            public boolean isRequired() {
                return getSelection() == null;
            }
        };

        contentTemplateChoiceField.setRequired(true);
        form.add(contentTemplateChoiceField);
        
        form.setEnabled(true);



        final ChoiceField<TagTargetDocumentsPlace> monitorOrLibrary = new ChoiceField<TagTargetDocumentsPlace>("monitorOrLibrary", new PropertyModel<TagTargetDocumentsPlace>(this, "tagTargetDocumentsPlace"), () -> Arrays.asList(TagTargetDocumentsPlace.library, TagTargetDocumentsPlace.monitor), true) {
            private static final long serialVersionUID = 1L;

            

            @Override
            protected String getDisplayValue(TagTargetDocumentsPlace value) {
                return new StringResourceModel("target." + value.toString(), ContentTagManagementFormPanel.this).getString();
            }

            @Override
            public void onUpdate(AjaxRequestTarget target) {
                super.onUpdate(target);
                setTagTargetDocumentsPlace(getValue());
                refreshActions(target);
            }
        };
        monitorOrLibrary.setRequired(true);
        form.add(monitorOrLibrary);

        if (getSelection() != null) {
            Link<Void> selectionLink = new Link<Void>("affectedItemsLink") {
                /**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
                public void onClick() {
                    @SuppressWarnings({ "unchecked", "rawtypes" })
					ListQuery ls = new ListQuery(getSelection());
					@SuppressWarnings("unchecked")
					ConsolePage<Content> page = (ConsolePage<Content>) ServiceLocator.getService(ApplicationSiteMapService.class).getPage("library-fixed-page");
                    page.setQuery(ls);
                    setResponsePage(page);
                }
            };
            
            selectionLink.add(new Label("affectedItemsLinkTitle", () -> getSelection() != null ? "View " + getSelection().size() + " Files " : ""));
            form.addOrReplace(selectionLink);

        } else {
            Link<Void> conditionTestLink = new Link<Void>("affectedItemsLink") {
                private static final long serialVersionUID = 1L;

                //public boolean isEnabled() {
                //	return getContentTemplate()!=null; 
                //}
                
                @SuppressWarnings("unchecked")
				@Override
                public void onClick() {
                    final String condition = getCondition();
                    
                    if (getDomain() != null && getContentTemplate() != null && condition != null) {
                        final Serializable domainId = getDomain().getId();
                        final Serializable contentTemplateId = getContentTemplate().getId();
                        Query query = ReclassifyContentCommand.getQuery(domainId, contentTemplateId, getTagTargetDocumentsPlace(), condition);
                        
                        @SuppressWarnings("unchecked")
						ConsolePage<Content> page = (ConsolePage<Content>) ServiceLocator.getService(ApplicationSiteMapService.class).getPage("library-fixed-page");
                        page.setQuery(query);
                        setResponsePage(page);
                        page.getPageParameters().add("showMonitorConsole", getTagTargetDocumentsPlace() == TagTargetDocumentsPlace.monitor);
                    }
                    else {
                    	setResponsePage( new ApplicationErrorPage( new Model<String>("Test condition"), new Model<String>("<b>Condition</b> nor <b>Content Template</b> can be null")));
                    }
                }
            };
            conditionTestLink.addOrReplace(new Label("affectedItemsLinkTitle",new StringResourceModel("test-condition",ContentTagManagementFormPanel.this, null)));
            form.addOrReplace(conditionTestLink);
        }
    }

    private List<Long> getDistinctContentTemplateIds() {
        Set<Long> contentTemplatesIds = new HashSet<>();
        if (list != null) {
            for (IModel<Content> c : list) {
                Serializable currentId = c.getObject().getContentTemplate().getId();
                contentTemplatesIds.add((Long) currentId);
            }
            return contentTemplatesIds.stream().collect(Collectors.toList());
        }
        final ArrayList<Long> result = new ArrayList<>(1);

        final ContentTemplate contentTemplate = getContentTemplate();
        if (contentTemplate != null) {
            result.add((Long) contentTemplate.getId());
        }
        return result;
    }


    @Override
    protected List<TagManagementAction> getPossibleTagManagementActions() {
        List<TagManagementAction> res = new ArrayList<>();
        final List<Long> distinctContentTemplateIds = getDistinctContentTemplateIds();
        if (!distinctContentTemplateIds.isEmpty())
            res.add(getTagManagementTagAction(distinctContentTemplateIds));

        
        if (distinctContentTemplateIds.size() == 1) {
            final TagManagementContentTemplateAction tagManagementContentTemplateAction = new TagManagementContentTemplateAction("tagAction") {
				private static final long serialVersionUID = 1L;
				@Override
                public Object getModifierInstance() {
                    if (this.getContentTemplate() != null)
                        return new ReclassifyContentCommand.ContentClassModifier((Long) this.getContentTemplate().getId());
                    return null;
                }
            };
            tagManagementContentTemplateAction.setContentTemplates(getContentTemplates());
            res.add(tagManagementContentTemplateAction);
        }
        if (this.getTagTargetDocumentsPlace() == TagTargetDocumentsPlace.monitor) {
            final TagManagementCancelWorkflowAction tagManagementCancelWorkflowAction = new TagManagementCancelWorkflowAction("tagAction") {
				private static final long serialVersionUID = 1L;
				@Override
                public Object getModifierInstance() {
                        return new ReclassifyContentCommand.CancelWorkflowModifier();
                }
            };
            res.add(tagManagementCancelWorkflowAction);
        }

        return res;
    }


    @SuppressWarnings("unchecked")
	public TagManagementTagAction getTagManagementTagAction(List<Long> contentTemplatesIDs) {

        List<Classifier> commonClassifiers = null;
        List<Attribute> commonattributes = null;

        for (Long contentTemplateID : contentTemplatesIDs) {
            final List<ClassifierTemplate> classifiersByContentTemplate = getContentDao().findClassifiersByContentTemplate(contentTemplateID);
            final List<Classifier> classifiers = classifiersByContentTemplate.stream().map(clf -> clf.getClassifier()).collect(Collectors.toList());
            if (commonClassifiers == null) {
                commonClassifiers = classifiers;
            } else {
                commonClassifiers = commonClassifiers.stream().
                        filter(clf -> classifiers.stream().anyMatch(clf2 -> clf.getId().equals(clf2.getId()))).collect(Collectors.toList());
            }

            final List<AttributeTemplate> attributesByContentTemplate = getContentDao().findAttributesByContentTemplate(contentTemplateID);
            final List<Attribute> attributes = attributesByContentTemplate.stream().map(attr -> attr.getAttribute()).collect(Collectors.toList());

            if (commonattributes == null) {
                commonattributes = attributes;
            } else {
                commonattributes = commonattributes.stream().
                        filter(attr -> attributes.stream().anyMatch(attr2 -> attr.getId().equals(attr2.getId()))).collect(Collectors.toList());
            }
        }
   
		List<ModelElement> modelElements = (List<ModelElement>) (List<?>) commonClassifiers;
        modelElements.addAll((List<ModelElement>) (List<?>) commonattributes);

        
        TagManagementTagAction tagManagementTagAction = new TagManagementTagAction("tagAction") {
			private static final long serialVersionUID = 1L;
			@Override
            public Object getModifierInstance() {
                final TagOperation tagOperation = this.getTagOperation();
                if (getTagType() != null) {
                    if (getTagType() == TagType.classifier) {
                        final DataSetMember datasetMember = tagOperation != TagOperation.remove ? getDatasetMember() : null;
                        final String macro = tagOperation != TagOperation.remove ? getMacro() : null;
                        if (getUseMacro())
                            return new ReclassifyContentCommand.ClassifierContentTagModifier((Classifier) getTagElement(), macro, tagOperation);
                        else
                            return new ReclassifyContentCommand.ClassifierContentTagModifier((Classifier) getTagElement(), datasetMember, tagOperation);
                    } else if (getTagType() == TagType.attribute) {
                        final String attributeValue = tagOperation != TagOperation.remove ? getAttributeValue() : null;
                        final String macro = tagOperation != TagOperation.remove ? getMacro() : null;
                        if (getUseMacro())
                            return new ReclassifyContentCommand.ClassifierContentTagModifier((Classifier) getTagElement(), macro, tagOperation);
                        else
                            return new ReclassifyContentCommand.AttributeContentTagModifier((Attribute) getTagElement(), attributeValue, tagOperation);
                    }
                }
                throw new RuntimeException("Invalid Tag Type.");
            }

        };
        tagManagementTagAction.setTagElementsTemplates(modelElements);
        return tagManagementTagAction;
    }

    @Override
    protected Command getCommand() {
        Long domainID = (Long) getDomain().getId();
        Long contentTemplateID = (getContentTemplate() != null) ? (Long) getContentTemplate().getId() : null;

        List<ReclassifyContentCommand.ContentTagModifier> contentTagModifiers = new ArrayList<>();
        contentTagModifiers.add((ReclassifyContentCommand.ContentTagModifier) getTagManagementAction().getModifierInstance());

        ReclassifyContentCommand reclassifyCommand = new ReclassifyContentCommand(contentTemplateID,getTagTargetDocumentsPlace(),  contentTagModifiers, domainID,  getCondition());
        reclassifyCommand.setUpPrivateLogger();

        reclassifyCommand.setUsrId((Long) getSessionUser().getId());

        if (getSelection() != null) {
            List<Content> clist = new ArrayList<Content>();
            getSelection().forEach(item -> clist.add(item.getObject()));
            reclassifyCommand.setSelection(clist);
        }
        return reclassifyCommand;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (list != null)
            list.forEach(item -> item.detach());

        if (contentTemplate != null)
            contentTemplate.detach();

        if (contentTemplates != null)
            contentTemplates.stream().forEach(c -> c.detach());
    }

    public ContentTemplate getContentTemplate() {
        return contentTemplate != null ? contentTemplate.getObject() : null;
    }

    public void setContentTemplate(ContentTemplate contentTemplate) {
        this.contentTemplate = new ObjectModel<>(contentTemplate);
    }


    public ContentTemplate getNewContentTemplate() {
        return newContentTemplate != null ? newContentTemplate.getObject() : null;
    }

    public void setNewContentTemplate(ContentTemplate contentTemplate) {
        this.newContentTemplate = new ObjectModel<>(contentTemplate);
    }

    public List<ContentTemplate> getContentTemplates() {
        return this.contentTemplates.stream().map(o -> o.getObject()).collect(Collectors.toList());
    }

    public void setContentTemplates(List<ContentTemplate> contentTemplates) {
        this.contentTemplates = contentTemplates.stream().map(o -> new ObjectModel<>(o)).collect(Collectors.toList());
        this.contentTemplates.sort(new Comparator<IModel<ContentTemplate>>() {
            @Override
            public int compare(IModel<ContentTemplate> o1, IModel<ContentTemplate> o2) {
                try {
                    return o1.getObject().getName().compareToIgnoreCase(o2.getObject().getName());
                } catch (Exception e) {
                    return 0;
                }
            }
        });
    }

    public TagTargetDocumentsPlace getTagTargetDocumentsPlace() {
        return tagTargetDocumentsPlace;
    }

    public void setTagTargetDocumentsPlace(TagTargetDocumentsPlace tagTargetDocumentsPlace) {
        this.tagTargetDocumentsPlace = tagTargetDocumentsPlace;
    }
}
