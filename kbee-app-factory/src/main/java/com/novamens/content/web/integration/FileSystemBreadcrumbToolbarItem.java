package com.novamens.content.web.integration;

import java.io.File;

import org.apache.wicket.model.Model;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.kbee.wicket.markup.html.console.browser.BreadcrumbToolbarItem;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.BaseBrowser;

/**
 *
 */
public class FileSystemBreadcrumbToolbarItem extends BreadcrumbToolbarItem {

	private static final long serialVersionUID = 1L;

	public FileSystemBreadcrumbToolbarItem(BaseBrowser<?> browser, Align align) {
		super(browser, align);
		setOutputMarkupId(true);
		
		LocalFSQuery q = (LocalFSQuery) browser.getQuery();
		String r_path=
		q.getRootDir().getParentFile() != null ?
		q.getRootDir().getParentFile().getAbsolutePath() :
			q.getRootDir().getAbsolutePath();
		String f_path=q.getDirectory().getAbsolutePath();
		if (!isDomainKbee())
			setBreadcrumb(f_path.replace(r_path,  ""));
		else
			setBreadcrumb(f_path);
		
		 addListeners();
	}
	
	
	protected void addListeners() {
		
		add(new WicketEventListener<LocalFSDirClickEvent<File>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(LocalFSDirClickEvent<File> event) {
				File file = event.getModelObject();
				if (file.exists() && file.isDirectory()) {
					if (event.getRequestTarget()!=null) {
						String r_path=
								event.getQuery().getRootDir().getParentFile() != null ?
								event.getQuery().getRootDir().getParentFile().getAbsolutePath() :
									event.getQuery().getRootDir().getAbsolutePath();
						String f_path=event.getQuery().getDirectory().getAbsolutePath();
						
						if (!isDomainKbee())
							setBreadcrumb(f_path.replace(r_path,  ""));
						else
							setBreadcrumb(f_path);
						
						//((LocalFSQuery) getQuery()).setDirectory(file);
						event.getRequestTarget().add(FileSystemBreadcrumbToolbarItem.this);
					}
				}
			}
		});
	}
	
	protected void setBreadcrumb(String path) {
		MenuBreadCrumbPanel bc =new MenuBreadCrumbPanel("breadcrumb");
		String escaped_separator=File.separator+File.separator; 
		String arr[] = path.split(escaped_separator);
		for (String s:arr)
			bc.addElement(new BCElement(new Model<String>(s)));
		super.setPanel(bc);
	}
	
	
	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}

	protected boolean isDomainKbee() {
		return	getPerson().getDomain().getName().toLowerCase().trim().equals("kbee");
	}

}
