package com.novamens.content.user;



import com.novamens.content.entity.Person;
import com.novamens.content.reportsubscription.ReportSubscription;
import com.novamens.content.user.externalLogin.ExternalPlatformId;
import com.novamens.content.user.externalLogin.UserExternalPlatformIdType;
import com.novamens.service.BusinessObjectService;

import java.io.File;
import java.util.List;

public interface UserSelfService extends BusinessObjectService {
	
	/**
	 * 
	 * @param sender the admin person who is sending the email with the link
	 */
	public void sendLinkToResetPassword(Person sender);
	
	public void resetPreferences();
	public void reindex();

	void addLinkLoginPlatform(ExternalPlatformId externalPlatformId, UserExternalPlatformIdType userExternalPlatformIdType, String userIdInPlatform);

	List<ReportSubscription> getUserReportSubscriptions();


	void saveUserReportSubscriptions(List<ReportSubscription> userReportSubscriptions);
    void saveUserReportSubscriptionNoAudit(ReportSubscription userReportSubscription);
    public void saveUserReportSubscription(ReportSubscription userReportSubscription, String reportName, String reportDescription, File file);
    public void sessionFlush();
	
	public void setEmailNotifications(boolean b_enabled);
	
}
