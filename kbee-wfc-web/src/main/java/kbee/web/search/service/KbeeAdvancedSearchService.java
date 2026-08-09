package kbee.web.search.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.service.ServiceLocator;

public class KbeeAdvancedSearchService implements AdvancedSearchService {
	
	private Domain domain;

	
	public KbeeAdvancedSearchService() {
	}
	
	public KbeeAdvancedSearchService(Domain domain) {
		this.domain = domain;
	}
	
	public Domain getDomain() {
		return domain;
	}
	
	public List<ModelElement> getElements() {
		
		List<ModelElement> elements = new ArrayList<>();
		
		for (Classifier classifier : getContentDao().getClassifiers(getDomain().getId(), ObjectState.ENABLED)) {
			if (classifier.isSearchable()) {
				elements.add(classifier);
			}
		}
		
		for (Attribute attribute : getContentDao().getAttributes(getDomain())) {
			if (attribute.isSearchable() && attribute.getState()==ObjectState.ENABLED) {
				elements.add(attribute);
			}
		}
		
		HashMap<String, Integer> map = new HashMap<>();
		
		for (ModelElement element : elements) {
			String name = element.getName();
			for (ContentTemplate template : getContentDao().getTemplates()) {
				int i = 0;
				boolean found = false;
				for (ModelElementTemplate e :  template.getStructure()) {
					if (name.equals(e.getName())) {
						found = true;
						break;
					}
					else {
						i++;
					}
				}
				if (found) {
					map.put(name,  i);
					break;
				}
			}
		}
		
		elements.sort((a, b) -> {
			Integer aval = map.get(a.getName());
			if (aval==null) aval = 0;
			Integer bval = map.get(b.getName());
			if (bval==null) bval = 0;
		    return Integer.compare(aval, bval);
		});
		
		return elements;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
