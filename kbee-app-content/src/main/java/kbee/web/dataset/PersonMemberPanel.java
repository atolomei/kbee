package kbee.web.dataset;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;

import com.novamens.content.entity.Person;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.PersonSet;
import com.novamens.content.service.PersonService;
import com.novamens.content.service.UrlService;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.content.form.KbeeEMemMemberData;
import com.novamens.kbee.content.form.KbeeMemberForm;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;

import kbee.web.eform.EFormDataModel;
import kbee.web.eform.EFormViewer;

// Persona en PersonSet
@SuppressWarnings("serial")
public class PersonMemberPanel extends ModelPanel<Void> {
	private static final long serialVersionUID = 1L;
	
	private IModel<PersonSet> datasetmodel;
	private IModel<Person> personmodel;
	
	public PersonMemberPanel(String id, IModel<Person> personmodel, IModel<PersonSet> datasetmodel) {
		super(id);
		setPerson(personmodel);
		setDataSet(datasetmodel);
	}
	
	public PersonSet getDataSet() {
		return datasetmodel.getObject();
	}
	
	public void setDataSet(IModel<PersonSet> model) {
		this.datasetmodel = model;
	}
	
	public Person getPerson() {
		return personmodel.getObject();
	}
	
	public void setPerson(IModel<Person> model) {
		this.personmodel = model;
	}
	
	public PersonMember getMember() {
		for (DataSetMember member : getContentDao().findMembersByEntity(getPerson())) {
			if (getDataSet().equals(member.getDataSet())) {
				return (PersonMember)member;
			}
		}
		return null;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		addOrReplace(new Label("dataset", ()->getDataSet().getDisplayName()));
		if (getMember()!=null) {
			addOrReplace(new EFormViewer("viewer", getFormData(getMember())));
		}
		else {
			addOrReplace(new InvisiblePanel("viewer"));
		}
		AjaxLink<Void> addlink = new AjaxLink<Void>("addlink") {
			public void onClick(AjaxRequestTarget target) {
				addToDataSet();
				target.add(PersonMemberPanel.this);
			}
			public boolean isVisible() {
				
				UserProfile up = PersonMemberPanel.this.getPerson().getProfile(UserProfile.class);
				
				if (up==null)
					return false;
				
				User user = up.getUser();
				
				if (user==null)
					return false;
				
				if (user.isCanonical())
					return false;
				
				return getMember()==null;
			}
		};
		addlink.add(new Label("label", getLabel("addtodataset.label", getDataSet().getDisplayName())));
		addOrReplace(addlink);
		Link<Void> gotolink = new Link<Void>("gotolink") {
			public void onClick() {
				String memberurl = getMember().getService(UrlService.class).getUrl();
				setResponsePage(new RedirectPage(memberurl));
			}
			public boolean isVisible() {
				return getMember()!=null;
			}
		};
		gotolink.add(new Label("label", getLabel("goto.label", getDataSet().getDisplayName())));
		addOrReplace(gotolink);
	}
	
	private void addToDataSet() {
		getPerson().getService(PersonService.class).addTo(getDataSet());
	}
	
	private IModel<EFormData> getFormData(DataSetMember member) {
		EForm form = getForm(member);
		EFormData data = new KbeeEMemMemberData(form, member);
		for (EFormField<?> field : form.getFields()) {
			field.get(member, data);
		}	
		IModel<EFormData> model = new EFormDataModel(data);
		return model;
	}
	
	private EForm getForm(DataSetMember member) {
		return new KbeeMemberForm(member);
	}
}