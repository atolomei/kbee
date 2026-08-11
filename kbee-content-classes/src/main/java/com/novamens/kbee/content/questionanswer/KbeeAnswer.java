package com.novamens.kbee.content.questionanswer;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.questionanswer.Answer;
import com.novamens.content.questionanswer.Question;
import com.novamens.content.social.Comment;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.content.social.KbeeComment;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

@Entity
@PrimaryKeyJoinColumn(name="content_id")
@Table(name = "DRB_Answer")
public class KbeeAnswer extends KbeeContent implements Answer {
	
	private static final long serialVersionUID = 1143411807363949827L;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "user_id",  updatable=true)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeQuestion.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "question_id", updatable=false)
	private Question question;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = KbeeComment.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "user_id")
	List<Comment> comments = new ArrayList<Comment>();
	
	@Column(name = "text")
	private String text;
	
	@Column(name = "accepted")
	private boolean accepted;
	
	@Column(name = "date_accepted")
	private OffsetDateTime dateaccepted;
	
	@Column(name = "date_submitted")
	private OffsetDateTime date_submitted;
	
	@Column(name = "date_edited_admin")
	private OffsetDateTime dateEditedByAdmin;

	
	@Column(name = "votes")
	int votes = 0;
	
	
	public KbeeAnswer(ContentTemplate ct) {
		super(ct);
	}
	
	public KbeeAnswer(Question question, String text) {
		super();
		this.question = question;
		this.text = text;
	}
	
	
	public KbeeAnswer() {
		super();
	}
	
	
	@Override
	public void setQuestion(Question question) {
		this.question=question;
	}

	@Override
	public Question getQuestion() {
		return question;
	}

	@Override
	public void setText(String text) {
			this.text=text;
	}

	@Override
	public String getText() {
		return text;
	}
	
	@Override
	public String getTitle() {
		return super.getTitle();
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public void setUser(User user) {
		this.user=user;

	}

	@Override
	public void setAccepted(boolean b) {
	this.accepted=b;

	}

	@Override
	public boolean getAccepted() {
		return accepted;
	}

	@Override
	public OffsetDateTime getDateAccepted() {
		return dateaccepted;
	}

	@Override
	public void setDateAccepted(OffsetDateTime date) {
		this.dateaccepted=date;
	}


	@Override
	public boolean wasEdited() {
		return dateEditedByAdmin!=null;
	}

	@Override
	public OffsetDateTime getDateEditedByAdmin() {
		return dateEditedByAdmin;
	}

	@Override
	public void setDateEditedByAdmin(OffsetDateTime date) {
		this.dateEditedByAdmin=date;
	}
	
	@Override
	public OffsetDateTime getDateSubmitted() {
		return date_submitted;
	}

	@Override
	public void setDateSubmitted(OffsetDateTime date) {
		this.date_submitted=date;
	}
	
	public void addVote() {
		votes++;
	}
	public void addVote(int n) {
		votes +=n;
	}
	public int getVotes(){
		return votes;
	}

	public String toString() {
		return text;
	}
	
	@Override
	public List<Comment> getComments() {
		return comments;
	}

	@Override
	public void addComment(Comment comment) {
			comments.add(comment);
	}

	@Override
	public void removeComment(Comment comment) {
		comments.remove(comment);
	}	
	
   @Override
	public boolean isCommentsEnabled() {
	   return getQuestion().isCommentsEnabled();
   }

	
	static public Answer createFromMap(Map<String, String> map) throws KbeeRuntimeException {
		
		Answer answer = null;
		
		BeansService beans = ServiceLocator.getService(BeansService.class);
		ContentDao dao = (ContentDao) beans.getBean("contentDao");
		
		if (map.get("domain_id")==null) 
			throw new KbeeRuntimeException("domain is null");
		
		if (map.get("name")==null)
			throw new KbeeRuntimeException("name is null");
		else if (dao.findContentByName(Answer.class, map.get("name"), map.get("domain_id"))!=null)
			throw new KbeeRuntimeException("Answer already exists");
		
		answer = new KbeeAnswer();
		
		answer.setTitle(map.get("title"));
		answer.setText(map.get("text"));
		answer.setDateAccepted(OffsetDateTime.now());
		
		String username = map.get("username");
		String questionname = map.get("questionname");


		User user = ServiceLocator.getService(SecurityService.class).findUserByUsername(username);
		
		if (user !=null)
			answer.setUser(user);
		
		Question question = (Question) dao.findContentByName(Question.class, questionname, map.get("domain_id"));
		
		if (question!=null)
			answer.setQuestion(question);
		
		return answer;
	}

	

	
}
