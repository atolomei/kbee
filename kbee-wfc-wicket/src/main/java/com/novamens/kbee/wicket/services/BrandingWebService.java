package com.novamens.kbee.wicket.services;



import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.entity.Person;
import com.novamens.service.BusinessSystemService;

public interface BrandingWebService extends BusinessSystemService {

	public ResourceReference getLoginLogo();
	
	public PackageResourceReference getExportIcon();
	public PackageResourceReference getApplicationIcon();
	public PackageResourceReference getFactoryIcon();
	
	public PackageResourceReference getSearchLibraryApplicationLogo();
	public PackageResourceReference getSearchLibraryBckImage();
	public PackageResourceReference getSearchLibraryInstitutionalApplicationLogo();

	PackageResourceReference getDealRoomBckImage();
	PackageResourceReference getGeneralSiteBckImage();
	PackageResourceReference getKBaseSiteBckImage();
	PackageResourceReference getOilBckImage();

	public PackageResourceReference getExcelReportTemplate();

	public Component getLoginBrandingPanel();

	public Image getUserAvatarPhoto(String id, Person person);

	public ResourceReference getUserAvatarResourceReference(Person person);

	public PackageResourceReference getSearchLibraryBckImage(String key);

	/**
	 * Logo
	 */
	public ResourceReference getApplicationLogo();
	public ResourceReference getApplicationLogoShadow();
	public ResourceReference getApplicationLogoDiagonalShadow();
	
	public ResourceReference getApplicationBannerBackground();
	public ResourceReference getApplicationBannerWithBeeBackground();
	
	 
	
}

