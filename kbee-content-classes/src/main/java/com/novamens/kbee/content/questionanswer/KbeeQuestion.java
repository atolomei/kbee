package com.novamens.kbee.content.questionanswer;
	
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
import org.hibernate.annotations.OrderBy;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.questionanswer.Answer;
import com.novamens.content.questionanswer.Question;
import com.novamens.content.social.Comment;
import com.novamens.kbee.content.document.KbeeDocument;
import com.novamens.kbee.content.social.KbeeComment;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

@Entity
@PrimaryKeyJoinColumn(name="content_id")
@Table(name = "DRB_Question")
public class KbeeQuestion extends KbeeDocument implements Question, Serializable {

	private static final long serialVersionUID = 4836781730348147862L;
	 
	@Column(name = "title")
	private String title;
	
	@Column(name = "text")
	private String text;
	
	@Column(name = "date_submitted")
	private OffsetDateTime date_submitted;

	@Column(name = "date_edited_admin")
	private OffsetDateTime dateEditedByAdmin;
	
	@Column(name = "state")
	private int qstate = OPEN;
	
	@ManyToOne(fetch = FetchType.EAGER, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "user_id", updatable=true)
	private User user;
	
	@Column(name = "votes")
	int votes = 0;
	
	@Column(name = "num_answers")
	int num_answers = 0;
								
	@OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, targetEntity = KbeeComment.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "user_id", updatable=false)
	List<Comment> comments = new ArrayList<Comment>();
	
	@OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, targetEntity = KbeeAnswer.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "question_id",  updatable=true)
	@OrderBy(clause="date_submitted desc")
	List<Answer> answers = new ArrayList<Answer>();
	
	public KbeeQuestion() {
		super();
	}
	
	public KbeeQuestion(String title, String text) {
		super();
		
		setTitle(title);
		setText(text);
	}
	
	public KbeeQuestion(ContentTemplate ct) {
		super(ct);
	}
	
	public void setTitle(String title){
		this.title=title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setQuestionState(int state){
		this.qstate=state;
	}
	
	public int getQuestionState() {
		return qstate;
	}
	
	public int getNumAnswers() 			{return num_answers;}
	public int decreaseNumAnswers() 	{num_answers--; return num_answers;}
									
	public void setText(String text) {
		this.text=text;
	}
	public String getText() {
		return text;
	}
	
	public User getUser() {
		return user;
	}
	public void setUser(User user){
		this.user = user;
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
	public void setAnswers(List<Answer> ans) {
		answers=ans;
		num_answers = (ans!=null?ans.size():0);
	}
	
	@Override
	public void addAnswer(Answer answer) {
		answer.setQuestion(this);
		answers.add(0, answer);
		num_answers++;
	}
	
	 
	public List<Answer> getAnswers() {
		if (answers!=null && answers.size()!=num_answers)
			num_answers=answers.size();
		return answers;
	}
	
	/**
	 *  
	 */
	static public Question createFromMap(Map<String, String> map) throws KbeeRuntimeException {
		Question question = null;
		
		BeansService beans = ServiceLocator.getService(BeansService.class);
		ContentDao dao = (ContentDao) beans.getBean("contentDao");

		if (map.get("domain_id")==null) 
			throw new KbeeRuntimeException("domain is null");
		
		if (map.get("name")==null)
			throw new KbeeRuntimeException("name is null");
		else if (dao.findContentByName(Question.class, map.get("name"), map.get("domain_id"))!=null)
			throw new KbeeRuntimeException("Question already exists");
		
		question = new KbeeQuestion();

		question.setTitle(map.get("title"));
		question.setText(map.get("text"));
		
		if (map.get("domain_id")!=null) 
			question.setDomain(dao.findDomainById(map.get("domain_id")));
		
		if (map.get("classification")!=null) {
			String clasi[] = map.get("classification").split(";");
			
			for( String ci: clasi) {
				String d[] = ci.split(":");
				String classfier_name = d[0];
				Classifier classifier = (Classifier) dao.findModelObjectByName(Classifier.class, classfier_name, map.get("domain_id"));
				if (classifier!=null) {
					if (classifier.getDataSetType()==DataSetType.DATE) {
							Date date;
							try {
								date = new SimpleDateFormat("mm/dd/yyyy", Locale.ENGLISH).parse(d[1]);
								// question.addClassification(classifier, date);
								throw new KbeeRuntimeException("Invalid date (mm/dd/yyyy): " + d[1]);
								
							} catch (ParseException e) {
									throw new KbeeRuntimeException("Invalid date (mm/dd/yyyy): " + d[1]);
							}
					}
					else {
						DataSetMember dm = (DataSetMember) dao.findModelObjectByName(DataSetMember.class, classifier.getDataSet(), d[1].trim());
						if (dm!=null) { 
							question.addClassification(classifier, dm);
						}
						else
							throw new KbeeRuntimeException("DataSetMember does not exist: " + d[1]);
					}
				}
				else
					throw new KbeeRuntimeException("Classifier does not exist " + classfier_name);
			}
		}
		return question;
	}


	@Override
	public boolean isCommentsEnabled() {
		return getQuestionState()==OPEN;
	}
	
	@Override
	public void removeComments() {
		this.comments = new ArrayList<Comment>();
	}
	
	@Override
	public OffsetDateTime  getDateSubmitted() {
		return date_submitted;
	}

	@Override
	public void setDateSubmitted(OffsetDateTime  date) {
		this.date_submitted=date;
	}
	
	
	public boolean wasEdited() {
		return dateEditedByAdmin!=null;
	}

	public OffsetDateTime  getDateEditedByAdmin() {
		return dateEditedByAdmin;
	}

	public void setDateEditedByAdmin(OffsetDateTime  date) {
		this.dateEditedByAdmin=date;
	}
	
	@Override
	public String getMetadataAsString() {
		return "Question. "+ String.valueOf(getNumAnswers())+" Answers. Submitted "+ "EEEEEEE";//elapsed;
	}
	
	@Override
	public double getSemanticDistance(Content co) {
		Map<Long, Classification> xm = getSemanticClassifications();
		int shared = 0, subco = 0;
		for (Classification clasi: co.getClassification()) {
			 	if (clasi.getClassifier().isSemantic()) {
			 			subco++;
			 			if (xm.containsKey(clasi.getDataSetMember().getId()))
			 					shared++;
			 	}
		}
		int max = (xm.size()<subco?xm.size():subco);
		if (max==0)
			return 0;
		return (double) (shared*100/max);
	}
}

