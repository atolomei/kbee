package kbee.web.service;

import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.beans.BeansService;
import com.novamens.content.model.DataSetMember;
import com.novamens.service.ServiceLocator;

/**
 *
 */
public class KbeeStatisticService implements StatisticService {
				
	private DataSetMember member;
	private String bean;
	
	public KbeeStatisticService() {
	}
	
	public KbeeStatisticService(DataSetMember member) {
		this.member = member;
	}
	
	@Override
	public Panel getReport(String id) {
		return (Panel)ServiceLocator.getService(BeansService.class).getBean(getBean(), id, getMember());
	}
	
	
	public void setBean(String beanname) {
		this.bean = beanname;
	}
	
	public String getBean() {
		return bean;
	}
	
	@Override
	public DataSetMember getMember() {
		return member;
	}
}
