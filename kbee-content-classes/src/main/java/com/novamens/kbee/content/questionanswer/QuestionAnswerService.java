package com.novamens.kbee.content.questionanswer;

import java.util.List;

import com.novamens.content.model.Classification;
import com.novamens.content.questionanswer.Answer;
import com.novamens.content.questionanswer.Question;
import com.novamens.content.social.Comment;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.service.BusinessSystemService;

public interface QuestionAnswerService extends BusinessSystemService {

	Question sendQuestion(String title, List<Classification> classification, String text);

	boolean hasPermission(UserProfile up, QuestionAnswerPermission action);

	void update(Answer answer);

	void delete(Answer answer);

	void update(Question question);

	void delete(Comment comment);

	void delete(Question question);

	void update(Comment comment);

	long wipeOut(Domain domain);

	Answer addAnswer(Question question, String text);

	

}
