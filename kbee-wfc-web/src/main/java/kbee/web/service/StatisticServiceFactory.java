package kbee.web.service;

import com.novamens.content.model.DataSetMember;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.ObjectService;
import com.novamens.service.Service;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.service.SpringServiceLocator;
			
public class StatisticServiceFactory extends AbstractServiceFactory<ObjectService>   {
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(KbeeStatisticService.class);
	}
	
	@SuppressWarnings("unchecked")
	public <S extends ObjectService> S getService(Object object) {
		if (!(object instanceof DataSetMember)) return null;
		SpringServiceLocator serviceLocator = (SpringServiceLocator)ServiceLocator.getInstance();
		if (serviceLocator.getContext().containsBean(getBean(object)))
			return (S)serviceLocator.getContext().getBean(getBean(object), object);
		else
			return null;
	}
	
	private String getBean(Object object) {
		return ((DataSetMember)object).getDataSet().getName().toLowerCase()+"-statistic-sevice";
	}
}






