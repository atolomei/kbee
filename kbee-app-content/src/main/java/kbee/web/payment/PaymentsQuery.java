package kbee.web.payment;

import com.novamens.beans.BeansService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import org.hibernate.SessionFactory;

public class PaymentsQuery extends HibernateQuery {

    private static final long serialVersionUID = 1L;


    @Override
    public String getStatement() {
        Domain domain = ServiceLocator.getService(UserService.class).getDomain();
        UserProfile sessionUserProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
        boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
        boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

        String filter ="p.domain.id = " + domain.getId();

        if(!is_root || !role_admin){
            filter += " and p.payer.id = " + sessionUserProfile.getUser().getId();
        }

        return "from KbeePayment p where " + filter + " order by p.id desc";
    }

    @Override
    public SessionFactory getSessionFactory() {
        return (SessionFactory) ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
    }

}
