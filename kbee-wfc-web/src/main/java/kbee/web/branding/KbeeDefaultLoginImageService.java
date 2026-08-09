package kbee.web.branding;

 
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;

import kbee.util.FSUtils;

import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.FileSystemResourceReference;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/***
 * 
 */

public class KbeeDefaultLoginImageService implements LoginImageService {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeDefaultLoginImageService.class.getName());

	private ResourceReference p_resources [];
	private LoginImageWrapper images[];
	
	private static final PackageResourceReference resources [] =  {
			
			
			new PackageResourceReference(KbeeDefaultLoginImageService.class, "log13.jpg"),
			new PackageResourceReference(KbeeDefaultLoginImageService.class, "log48.jpg"),
			new PackageResourceReference(KbeeDefaultLoginImageService.class, "log73.jpg"),
			new PackageResourceReference(KbeeDefaultLoginImageService.class, "log-118.jpg"),
			new PackageResourceReference(KbeeDefaultLoginImageService.class, "log-158.jpg")
	};
	
	int nnn = 0;
	Calendar cal = Calendar.getInstance();
	boolean initiated = false;
	
	@Override
	public LoginImageWrapper getTodayLoginImageWrapper() {
								
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		
		if (ServiceLocator.getService( ApplicationServerService.class).getWicketConfigurationType().equals("DEVELOPMENT")) {
			int minute=cal.get(Calendar.MINUTE);
			return getLoginImageWrapper(minute);
		}

		int doy = cal.get(Calendar.DAY_OF_YEAR);
		return getLoginImageWrapper(doy);
	}
	
	private void init() {
		
		List<File> list = getCandidateFiles();

		p_resources  = new ResourceReference [ list.size() ];
		
		
		if (list.size()<1)
			images = new LoginImageWrapper [list.size()+ resources.length];
		else
			images = new LoginImageWrapper [list.size()];
		
		for (int n=0; n<list.size(); n++) {
			p_resources [n] = new FileSystemResourceReference(list.get(n).getName(), Paths.get(list.get(n).getAbsolutePath()) );
			images[n]	 = new LoginImageWrapper(p_resources [n]);
		}
		

		int offset = list.size();
		
		if (offset<1) {
			for (int n=0; n<resources.length; n++) {
				images[offset+n]	 = new LoginImageWrapper(resources[n]);
			}
		}

		initiated= true;
	}
	
	
	@Override
	public LoginImageWrapper getLoginImageWrapper(int index) {
	 	if (!initiated)
			init();
		return images[index % images.length];
	}
	
	
	private List<File> getCandidateFiles() {
		
		List<File> files = new ArrayList<File>();
		File base = new File(ServiceLocator.getService(ApplicationServerService.class).getLoginImagesDir());
		
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

