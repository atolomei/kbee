package kbee.web.search.service;

import java.util.List;

import com.novamens.content.model.ModelElement;
import com.novamens.service.ObjectService;

public interface AdvancedSearchService extends ObjectService {
	List<ModelElement> getElements();
}
