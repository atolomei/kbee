package kbee.aerolineas.service;

import java.util.ArrayList;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.email.EmailData;
import com.novamens.email.EmailService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.SystemService;
import com.novamens.util.PropertiesFactory;

public class AerolineasService implements SystemService {
	
	private List<EntityMember> areas = null;
	private Domain domain = null;
	
	private static String AEROLINEAS = 
		"Aerolíneas Argentinas";
	
	private static String SingUpFrom =
		PropertiesFactory
		.getInstance("kbee")
		.getProperties()
		.getProperty("singup.email.from");
	
	private static String SingUpTo =
		PropertiesFactory
		.getInstance("kbee")
		.getProperties()
		.getProperty("singup.email.to");
	
	public AerolineasService() {
		
	}
	
	public List<EntityMember> getAreas() {
		if (areas==null) {
			loadAreas();
		}
		return areas;
	}
	
	public void singUp(SignUpData data) {
		EmailData emaildata = new EmailData(
				SingUpFrom, 
				SingUpTo, 
				"Sing Up",
				getSignUpMessage(data),
				""); 
		ServiceLocator
			.getService(EmailService.class)
			.send(emaildata, getDomain());	
	}
	
	private String getSignUpMessage(SignUpData data) {
		StringBuffer text = new StringBuffer();
		text.append("Pedido de ingreso de usuario:\n");
		text.append(data.getName()+", "+data.getLastName()+"\n");
		text.append(data.getEmployeeId()+"\n");
		text.append(data.getEmail()+"\n");
		text.append(data.getCommnent()+"\n");
		return text.toString();
	}
	
	private void loadAreas() {
		DataSet datasetArea = getDataSet("area");
		Classifier classifierEmpresa = getClassifier("empresa");
		List<DataSetMember> areas = getContentDao().getMembers(datasetArea, "strvalue");
		this.areas = new ArrayList<EntityMember>();
		for (DataSetMember area : areas) {
			List<Classification> empresas = area.getClassification(classifierEmpresa);
			if (!empresas.isEmpty() 
				&& AEROLINEAS.equalsIgnoreCase(empresas.get(0).getDataSetMember().getDisplayName())
				&& area.getState().equals(ObjectState.ENABLED)) {
				this.areas.add((EntityMember)area);
			}
		}
	}
	
	private DataSet getDataSet(String alias) {
		return  getContentDao().findDataSetByAlias(alias, getDomain().getId());
	}
	
	private Classifier getClassifier(String alias) {
		return getContentDao().findClassifierByAlias(alias, getDomain().getId());
	}
	
	private Domain getDomain() {
		if (domain == null) {
			domain = getContentDao().findDomainByName("aerolineas");
		}
		return domain;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
