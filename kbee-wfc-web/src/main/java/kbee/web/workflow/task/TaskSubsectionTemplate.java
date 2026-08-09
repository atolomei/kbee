package kbee.web.workflow.task;

import java.io.Serializable;

import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelSection;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.model.Subsection;
import com.novamens.content.model.SubsectionTemplate;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.model.KbeeSubsection;

@Deprecated
public class TaskSubsectionTemplate implements SubsectionTemplate, Serializable {
	private static final long serialVersionUID = 1L;

	private String name = null; 
	private int order = 0;
	private transient Subsection subsection;
	
	@Override
	public ModelElement getElement() {
		if (subsection==null) {
			subsection = new KbeeSubsection(getName());
		}
		return subsection;
	}
	
	@Override
	public String getDisplayName() {
		return getName();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public void  setName(String value) {
		this.name = value;
	}
	@Override
	public boolean isMandatory() {
		return false;
	}
	
//	@Override
//	public ModelSection getSection() {
//		return null;
//	}
//	
//	@Override
//	public void setSection(ModelSection section) {
//	}
	
//	@Override
//	public String getSubsection() {
//		return null;
//	}

	@Override
	public Domain getDomain() {
		return null;
	}

	@Override
	public void setDomain(Domain domain) {
	}
	
	public void setOrder(int value)	{
		this.order = value;
	}
	
	@Override
	public int getOrder()	{
		return order;
	}
	
	public ModelElement getParent() {
		return null;
	}
	
	@Override
	public boolean isReverse() {
		return false;
	}
	
	@Override
	public boolean isCanonical() {
		return false;
	}
	
	public void setReverse(boolean value) {
		
	}
	
	@Override
	public Multiplicity getMultiplicity() {
		return Multiplicity.M11;
	}
}
