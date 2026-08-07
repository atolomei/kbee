package com.novamens.kbee.timer;

 
import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import com.novamens.security.Identifiable;
import com.novamens.timer.CallBack;
import com.novamens.timer.Timer;

@Entity
@Table(name = "kb_timer")
public class KbeeTimer implements Timer, Identifiable  {
	
	@Id
	@GenericGenerator(
		name = "timer_sequencer",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@Parameter(name = "sequence_name", value = "timer_sequence"),
			@Parameter(name = "increment_size", value = "50"),
			@Parameter(name = "optimizer", value = "hilo")
		}
	)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "timer_sequencer")
	@Column(name = "id")
	private Long id;
	
	@Column(name = "creationDate")
	private OffsetDateTime creationDate;
	
	@Column(name = "dueDate")
	private OffsetDateTime dueDate;
	
	@Type(type = "com.novamens.kbee.timer.CallBackType")
	@Column(name = "callBack")
	private CallBack callBack;
	
	@Column(name = "attemps")
	private int attemps;
	
	@Column(name = "error_message")
	private String errorMessage;
	
	public KbeeTimer() {
		
	}
	
	public KbeeTimer(OffsetDateTime dueDate, CallBack callBack) {
		setDueDate(dueDate);
		setCallBack(callBack);
		setCreationDate(OffsetDateTime.now());
		setAttemps(0);
	}
	
	@Override
	public Long getId() {
		return id;
	}
	
	@Override
	public String getDisplayName() {
		return "Timer";
	}
	
	public OffsetDateTime getDueDate() {
		return dueDate;
	};
	
	public void setDueDate(OffsetDateTime time) {
		this.dueDate = time;
	};
	
	public CallBack getCallBack() {
		return (CallBack)callBack;
	};
	
	public void setCallBack(CallBack callBack) {
		this.callBack = callBack;
	};
	
	public OffsetDateTime getCreationOffsetDateTime() {
		return this.creationDate;
	}
	
	public int getAttemps() {
		return attemps;
	}
	
	public void setAttemps(int value) {
		this.attemps = value;
	}
	
	public void setError(Exception e) {
		this.attemps++;
		this.errorMessage = e.getMessage()!=null && e.getMessage().length()>256 ? e.getMessage().substring(1, 255) : e.getMessage();
	}
	
	protected void setCreationDate(OffsetDateTime time) {
		this.creationDate = time;
	};
}
