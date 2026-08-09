package kbee.web.branding;






import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.novamens.kbee.wicket.util.GenericPhoto;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.entity.Person;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.service.datamanagement.KbeeHTMLExporter;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.kbee.wicket.util.GenericPhoto;
import com.novamens.kbee.wicket.util.KBFileSystemResourceReference;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;

import kbee.util.FSUtils;

public class KbeeDefaultBrandingWebService implements BrandingWebService, EventListener {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeDefaultBrandingWebService.class.getName());
											
	private final PackageResourceReference EXPORTICON 	= new PackageResourceReference(KbeeHTMLExporter.class, "kbee.png");
	
										
	private final PackageResourceReference APPLOGO 				= new PackageResourceReference(KbeeDefaultBrandingService.class, "abeja-kbee-vertical.png");
	private final PackageResourceReference APPLOGO_SHADOW 		= new PackageResourceReference(KbeeDefaultBrandingService.class, "abeja-kbee-vertical-sombra.png");
	private final PackageResourceReference BANNER_BACKGROUND 	= new PackageResourceReference(KbeeDefaultBrandingService.class, "hive7.png");
	private final PackageResourceReference BANNER_WITH_BEE_BACKGROUND	= new PackageResourceReference(KbeeDefaultBrandingService.class, "hive7-with-bee.png");
	
	
	
															
	private final PackageResourceReference APPLOGO_DIAG_SHADOW 		= new PackageResourceReference(KbeeDefaultBrandingService.class, "abeja-kbee-diagonal-sombra.png");
	
	
	
	
	private final PackageResourceReference APPICON 		= new PackageResourceReference(KbeeDefaultBrandingService.class, "abeja-novamens.png");

	private final PackageResourceReference FACTORYCON 	= new PackageResourceReference(KbeeDefaultBrandingService.class, "abeja-novamens.png");
	private final PackageResourceReference KBEE_ICON = new PackageResourceReference(KbeeDefaultBrandingService.class, "kbee-login.png");
	
	private final PackageResourceReference LIBRARY_LOGO	= new PackageResourceReference(KbeeDefaultBrandingWebService.class, "kbee-login.png" ); // "aa.png"
	private final PackageResourceReference LIBRARY_INSTITUTIONAL_LOGO	= new PackageResourceReference(KbeeDefaultBrandingWebService.class, "kbee-login.png" ); // "aa-institutional.png" 
			
	private final Calendar cal = Calendar.getInstance();
	
	
	
	private final PackageResourceReference OIL_BCK[] = {
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "aa40.jpg")
			// new PackageResourceReference(KbeeDefaultBrandingWebService.class, "oil2.jpg")
	};
	
	private final PackageResourceReference DEALROOM_BCK[] = {
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "aa40.jpg")
			// new PackageResourceReference(KbeeDefaultBrandingWebService.class, "aa31.jpg"),
			// new PackageResourceReference(KbeeDefaultBrandingWebService.class, "aa32.jpg")
	};
	
	private final PackageResourceReference GENERAL_BCK[] = {
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "aa40.jpg")
			
			/**new PackageResourceReference(KbeeDefaultBrandingWebService.class, "aa1.jpg"), 
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "aa2.jpg"),
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "aa5.jpg"),
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "aa6.jpg"),
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "aa13.jpg"),
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "aa14.jpg"),
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "aa17.jpg"), 
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "aa26.jpg")
			**/
	};

	private final PackageResourceReference LIBRARY_BCK[] = {
			
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "flor1.jpg"),
		 
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "flor3.jpg"),
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "flor4.jpg"),
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "flor5.jpg"),
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "flor6.jpg"),
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "flor7.jpg"),
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "flor8.jpg"),
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "flor9.jpg"),
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "flor10.jpg"),
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "flor11.jpg"),
			new PackageResourceReference(KbeeDefaultBrandingWebService.class, "flor12.jpg")
			
			
			// new PackageResourceReference(KbeeDefaultBrandingWebService.class, "lotus.jpg")
	};
	
/**
	private final PackageResourceReference user_images [] = {
		new PackageResourceReference(KbeeDefaultBrandingWebService.class, "user_image_1.jpg"),
		new PackageResourceReference(KbeeDefaultBrandingWebService.class, "user_image_2.jpg"),
		new PackageResourceReference(KbeeDefaultBrandingWebService.class, "user_image_3.jpg"),
		new PackageResourceReference(KbeeDefaultBrandingWebService.class, "user_image_4.jpg"),
		new PackageResourceReference(KbeeDefaultBrandingWebService.class, "user_image_5.jpg"),
		new PackageResourceReference(KbeeDefaultBrandingWebService.class, "user_image_6.jpg")
	};
	
	
	@Override
	public PackageResourceReference[] getUserImages() {
		return user_images;
	}
	
	@Override
	public PackageResourceReference getUserImage(long n) {
		return user_images[ ((int) (n))  % user_images.length];
	}
	**/
	
	
	
	
	
	@Override
	public boolean listen(Event event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onEvent(Event event) {
		// TODO Auto-generated method stub

	}

	@Override
	public PackageResourceReference getExportIcon() {
		return EXPORTICON;
	}
	
	/**
	 */
	@Override
	public PackageResourceReference getApplicationIcon() {
				return KBEE_ICON;
	}

	/**
	 */
	@Override
	public PackageResourceReference getFactoryIcon() {
		return FACTORYCON;
	}

	
	/**
	 * Login Image
	 */
	@Override
	public ResourceReference getLoginLogo() {
				return  APPICON;
	}


	
	/**
	 * Logo
	 */
	@Override
	public ResourceReference getApplicationLogo() {
				return  APPLOGO;
	}

	

	/**
	 * Logo
	 */
	@Override
	public ResourceReference getApplicationLogoShadow() {
				return  APPLOGO_SHADOW;
	}

	public ResourceReference getApplicationBannerBackground() {
		return  BANNER_BACKGROUND;
	}

	public ResourceReference getApplicationBannerWithBeeBackground() {
		return  BANNER_WITH_BEE_BACKGROUND;
	}
	/**
	 * Logo
	 */

	@Override
	public ResourceReference getApplicationLogoDiagonalShadow() {
				return  APPLOGO_DIAG_SHADOW;
	}

	
	
	int nn=0;
	@Override
	public PackageResourceReference getSearchLibraryApplicationLogo() {
		return LIBRARY_LOGO;
	}
	
	@Override
	public PackageResourceReference getSearchLibraryInstitutionalApplicationLogo() {
		return LIBRARY_INSTITUTIONAL_LOGO;
	}
	

	
	
	static int nnn = 0;
	static {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		nnn = cal.get(Calendar.HOUR);
	}
	

	@Override
	public PackageResourceReference getSearchLibraryBckImage(String key) {
		OffsetDateTime now = OffsetDateTime.now();
		int month_of_year = now.getMonthValue();
		
		PackageResourceReference  r = LIBRARY_BCK[ (Math.abs(key.hashCode()) + month_of_year) % LIBRARY_BCK.length];
		logger.debug("getSearchLibraryBckImage: " + key + " -> " + r.getName());
		
				
		return LIBRARY_BCK[ (Math.abs(key.hashCode()) + month_of_year) % LIBRARY_BCK.length];
	}

	@Override
	public PackageResourceReference getSearchLibraryBckImage() {
		//
		// if (ServiceLocator.getService( ApplicationServerService.class).getWicketConfigurationType().equals("DEVELOPMENT")) {
		//	logger.debug(String.valueOf(nnn % LIBRARY_BCK.length));   
		//	return LIBRARY_BCK[nnn++ % LIBRARY_BCK.length];
		// }
		//
		cal.setTimeInMillis(System.currentTimeMillis());
		int base_week_index = cal.get(Calendar.MONTH) + nnn++;
		return LIBRARY_BCK[base_week_index % LIBRARY_BCK.length];
	}


	@Override
	public PackageResourceReference getDealRoomBckImage() {
		if (ServiceLocator.getService( ApplicationServerService.class).getWicketConfigurationType().equals("DEVELOPMENT")) {
			logger.debug(String.valueOf(nnn % DEALROOM_BCK.length));   
			return DEALROOM_BCK[nnn++ % DEALROOM_BCK.length];
		}
		cal.setTimeInMillis(System.currentTimeMillis());
		int base_week_index = cal.get(Calendar.MONTH);
		return DEALROOM_BCK[base_week_index % DEALROOM_BCK.length];
	}
	
	
	@Override
	public PackageResourceReference getGeneralSiteBckImage() {
		if (ServiceLocator.getService( ApplicationServerService.class).getWicketConfigurationType().equals("DEVELOPMENT")) {
			logger.debug(String.valueOf(nnn % GENERAL_BCK.length));   
			return GENERAL_BCK[nnn++ % GENERAL_BCK.length];
		}
		cal.setTimeInMillis(System.currentTimeMillis());
		int base_week_index = cal.get(Calendar.MONTH);
		return GENERAL_BCK[base_week_index % GENERAL_BCK.length];
	}

	

	@Override
	public PackageResourceReference getKBaseSiteBckImage() {
		if (ServiceLocator.getService( ApplicationServerService.class).getWicketConfigurationType().equals("DEVELOPMENT")) {
			logger.debug(String.valueOf(nnn % GENERAL_BCK.length));   
			return GENERAL_BCK[nnn++ % GENERAL_BCK.length];
		}
		cal.setTimeInMillis(System.currentTimeMillis());
		int base_week_index = cal.get(Calendar.DAY_OF_YEAR);
		return GENERAL_BCK[base_week_index % GENERAL_BCK.length];
	}

	@Override
	public PackageResourceReference getOilBckImage() {
		if (ServiceLocator.getService( ApplicationServerService.class).getWicketConfigurationType().equals("DEVELOPMENT")) {
			logger.debug(String.valueOf(nnn % OIL_BCK.length));   
			return OIL_BCK[nnn++ % OIL_BCK.length];
		}
		cal.setTimeInMillis(System.currentTimeMillis());
		int base_week_index = cal.get(Calendar.DAY_OF_YEAR);
		return OIL_BCK[base_week_index % OIL_BCK.length];
	}


	@Override
	public Component getLoginBrandingPanel() {
		return new WebMarkupContainer("branding");
	}

	@Override
	public PackageResourceReference getExcelReportTemplate() {
		return new PackageResourceReference(KbeeDefaultBrandingWebService.class, "ExportTemplate.xlsx");
	}

	
	
	private ResourceReference p_resources [];	
	boolean initiated=false;
	
	
	

		
	@Override
	public ResourceReference getUserAvatarResourceReference(Person person) {

		if (!initiated)
			init();

		if (p_resources.length==0)
			return null;

		ResourceReference ref = p_resources[ (((Long) person.getId()).intValue())  %  p_resources.length];
		
		// logger.debug( person.getDisplayName() + " - " + person.getClass().getName() + " - " + person.getId().toString() + " -> " + ref.getName() );
		
		return ref;
	}



	@Override
	public Image getUserAvatarPhoto( String id, Person person) {
		
		if (!initiated)
			init();

		if (p_resources.length==0)
			return new GenericPhoto(id);

		ResourceReference ref = p_resources[ (((Long) person.getId()).intValue())  %  p_resources.length];
		Image image= new Image(id, ref);
		return image;
	}

	private synchronized void init() {
		List<File> list = getCandidateFiles();
		p_resources  = new ResourceReference [ list.size() ];
		for (int n=0; n<list.size(); n++) {
			p_resources [n] = new KBFileSystemResourceReference(
					list.get(n).getName(), list.get(n).getAbsolutePath()
			);
		}
		initiated=true;
	}
	
	
   private List<File> getCandidateFiles() {
		
		List<File> files = new ArrayList<File>();
		File base = new File(ServiceLocator.getService(ApplicationServerService.class).getAvatarImagesDir());
		
		if (!base.exists() || !base.isDirectory())
			return files;
		
		File arrfiles [] = base.listFiles();
		
		for (File file: arrfiles) {
				if (file.isFile()) {
					if (FSUtils.isImage(file)) {
						files.add(file);
					}
				}
		}
		
		return files;
	}

}
