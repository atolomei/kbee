package com.novamens.kbee.wicket.markup.html.console.grid;

import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.model.Model;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateFormatModel extends Model<String> {
    private static final long serialVersionUID = 1L;
    private final OffsetDateTime xd;
    private final boolean show_user;
    private final String date_format;
    private String nullValue;

    private static Logger logger = LogManager.getLogger(DateFormatModel.class.getName());

    public DateFormatModel(OffsetDateTime xd, boolean show_user, String date_format, String nullValue) {
        this.xd = xd;
        this.show_user = show_user;
        this.date_format = date_format;
        this.nullValue = nullValue;
    }

    public String getObject() {
        
    	DateTimeService service = ServiceLocator.getService(DateTimeService.class);
        
    	
    	User user = getUser();
        String zid = service.getMapZoneIds().get(user.getTimeZone());
        if (zid == null) {
            logger.warn("ZoneId: " + user.getTimeZone() + " is not in the database");
            zid = ZoneId.systemDefault().getId();
        }
        if (xd != null) {
            ZonedDateTime zd = ZonedDateTime.ofInstant(xd.toInstant(), ZoneId.of(zid));
            String tst;

            if (show_user) {
                if (date_format == null)
                    tst = service.timeElapsed(zd, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
                else if (date_format.equals(DateTimeService.COLlOQUIAL_AGO_LABEL))
                    tst = service.timeElapsed(zd, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
                else if (date_format.equals(DateTimeService.COLlOQUIAL_LABEL))
                    tst = service.timeElapsed(zd, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL, null);
                
                else if (date_format.equals(DateTimeService.MONTH_DAY_YEAR_LABEL)) {
                	
                	
                	
                    tst = service.format(xd, zid, user.getLocale(), DateTimeService.Month_Day_Year);
                }
                else if (date_format.equals(DateTimeService.MONTH_DAY_YEAR_GMT_LABEL)) {
                	tst = service.getDomainInOriginalGMTDateDisplayString(xd, user.getLocale());
                			
                }
                
                else if (date_format.equals(DateTimeService.TIMESTAMP_LABEL))
                    tst = service.format(xd, zid, user.getLocale(), DateTimeService.Month_Day_Year_hh_mm_ss_zzz);
                else
                    tst = service.format(xd, zid, user.getLocale(), DateTimeService.Month_Day_Year_hh_mm);
                return tst;
                
                
                
            } else {
                if (date_format == null)
                    tst = service.timeElapsed(zd, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
                else if (date_format.equals(DateTimeService.COLlOQUIAL_AGO_LABEL))
                    tst = service.timeElapsed(zd, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
                else if (date_format.equals(DateTimeService.COLlOQUIAL_LABEL))
                    tst = service.timeElapsed(zd, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL, null);
                
                
                else if (date_format.equals(DateTimeService.MONTH_DAY_YEAR_LABEL)) {
                	tst = service.format(xd, zid, user.getLocale(), DateTimeService.Month_Day_Year);
                }
                
                else if (date_format.equals(DateTimeService.MONTH_DAY_YEAR_GMT_LABEL)) {
                
                	// ZoneId z_d = null;
                	// try {
                	// 	z_d=ZoneId.of(getDomain().getTimeZone());
                	// } catch (Exception e) {
                	// 	logger.error(e);
                	// 	z_d=ZoneId.of(zid);
                	// }
                	//  We can not convert to the user's timezone because the date info is ambigous, we do not know if it
                	//  refers to 0.00 or 24.00
                	// tst = service.getDomainZoneIDDateDisplayString(xd, z_d, user.getLocale());
                	
                	tst = service.getDomainInOriginalGMTDateDisplayString(xd, user.getLocale());
                	
                	
                }
                
                else if (date_format.equals(DateTimeService.TIMESTAMP_LABEL))
                    tst = service.format(xd, zid, user.getLocale(), DateTimeService.Month_Day_Year_hh_mm_ss_zzz);
                else
                    tst = service.format(xd, zid, user.getLocale(), DateTimeService.Month_Day_Year_hh_mm);
             
                return tst;
            }
        }
        return nullValue;
    }

    protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
    
    protected KbeeUser getUser() {
        return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
    }
}
