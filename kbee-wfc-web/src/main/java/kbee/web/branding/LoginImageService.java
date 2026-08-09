package kbee.web.branding;


import com.novamens.service.BusinessSystemService;

public interface LoginImageService extends BusinessSystemService {

	LoginImageWrapper getLoginImageWrapper(int index);
	LoginImageWrapper getTodayLoginImageWrapper();
	
}
