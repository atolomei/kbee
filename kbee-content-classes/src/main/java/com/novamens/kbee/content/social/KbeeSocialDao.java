package com.novamens.kbee.content.social;



import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;


import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.novamens.content.base.Content;
import com.novamens.content.social.Comment;
import com.novamens.content.social.Report;
import com.novamens.content.social.SocialDao;
import com.novamens.content.social.Vote;
import com.novamens.dom.ObjectState;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/**
 *   Social interaction: Comment, Vote, Report
 */
public class KbeeSocialDao implements SocialDao {

	private SessionFactory sessionFactory;
	
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}
	
	public void save(Comment comment) {
	
		if (comment.getLastModifiedOffsetDateTime()==null)
			comment.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		if (comment.getLastModifiedUser()==null)
			comment.setLastModifiedUser(getSessionUser());
		
		if (comment.getState()==null)
			comment.setState(ObjectState.ENABLED);
		
		if (comment.getTitle()!=null && comment.getTitle().length()>256) {
			comment.setTitle(comment.getTitle().substring(0, 256));
		}
		getSessionFactory().getCurrentSession().save(comment);
	};
	
	
	public void save(Vote vote) {
		getSessionFactory().getCurrentSession().save(vote);
	};
	
	
	/**
	 * Devuelve todos comentarios de todas las versiones de un contenido 
	 * Se asume que los comentarios se agregan sobre versiones "head" 
	 * publicadas, y por lo tanto son válidos aunque el contenido se versione.
	 * 
	 * Los comentarios que son respuesta de un comentario viene dentro del comment parent
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Comment> findCommentsByContent(Content content) {
		String hql = "FROM KbeeComment C WHERE C.content.oid = :contentid AND isfirstlevel=true order by C.date_submitted desc";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("contentid", content.getOId());
		List results = query.list();
		return results;
	}
	
	
	/**
	 * 
	 * Devuelve todos los votos de todas las versiones de un contenido 
	 * Se asume que los votos se agregan sobre versiones "head" 
	 * publicadas, y por lo tanto son válidos aunque el contenido se versione.
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Vote> findVotesByUser(Content content, User user) {
		String hql = "FROM KbeeVote V WHERE V.content.oid = :contentid AND V.user.id=:userid";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("contentid", content.getOId());
		query.setParameter("userid", user.getId());
		List results = query.list();
		return results;
	}

	public User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	@Override
	public void save(Report report) {
		getSessionFactory().getCurrentSession().save(report);
	}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<Report> findReportsByUser(Content content, User user) {
		String hql = "FROM KbeeReport V WHERE V.content.oid = :contentid AND V.user.id=:userid";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("contentid", content.getOId());
		query.setParameter("userid", user.getId());
		List results = query.list();
		if (results==null)
			return new ArrayList<Report>();
		return results;
	}

	
	/** 
	 * up to 100 comments
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Comment> findRecentCommentsBySiteOId(Long site_oid) {
		String hql = "FROM KbeeComment C WHERE C.site_oid = :site_oid order by C.date_submitted desc";
		Query<Comment> query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setMaxResults(100);
		query.setParameter("site_oid", site_oid);
		@SuppressWarnings("rawtypes")
		List results = query.list();
		if (results==null)
			return new ArrayList<Comment>();
		return results;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Comment> findCommentsResponses(Comment comment) {
		String hql = "FROM KbeeComment C WHERE C.parent_comment.oid = :commentid order by C.date_submitted desc";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("commentid", comment.getOId());
		List results = query.list();
		if (results==null)
			return new ArrayList<Comment>();
		return results;
	}

	
	/** 
	 *  Counts all comments ever published on all versions of the content.  
	 */
	@Override
	public int getTotalComments(Content  content) throws IOException {
		String hql = "select count(*) FROM KbeeComment C where C.content.oid= :content_id"; 
		Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("content_id", content.getOId());
		query.setCacheable(true);
		query.setCacheRegion("query");
		return ((Long) query.uniqueResult()).intValue();
	}
	
}
