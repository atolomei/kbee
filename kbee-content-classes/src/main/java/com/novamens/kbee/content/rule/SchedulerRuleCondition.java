package com.novamens.kbee.content.rule;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import com.novamens.content.base.RuleCondition;
import com.novamens.kbee.content.multidimensional.ClassificationDisplayNameExtractor;

public class SchedulerRuleCondition implements RuleCondition {
				
	private String statement;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SchedulerRuleCondition.class.getName());
	
	DateTimeFormatter timeformatter = DateTimeFormatter.ofPattern("dd/M/yyyy");

	public SchedulerRuleCondition(String statement) {
		setStatement(statement);
	}

	public String getStatement() {
		return statement;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}
	
	public boolean isTrue(OffsetDateTime time) {
		time = truncate(time);
		for (OffsetDateTime schedule : getSchedule()) {
			OffsetDateTime schedule1 = schedule.plusDays(3);
			if (time.equals(schedule) || (time.isAfter(schedule) && time.isBefore(schedule1)) || time.equals(schedule1)) {
				return true;
			}
		}
		return false;
	}
	
	public List<OffsetDateTime> getSchedule() {
		List<OffsetDateTime> schedule = new ArrayList<OffsetDateTime>();
		if (getStatement()==null) return schedule;
		OffsetDateTime now = truncate(OffsetDateTime.now());
		String thisyear = String.valueOf(now.getYear());
		ZoneId zone = ZoneId.systemDefault();
		StringTokenizer tokenizer = new StringTokenizer(getStatement(), "\r\n");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			try {
				LocalDate localdate;
				if (token.length()>5) {
					localdate = LocalDate.parse(token, timeformatter);
				}
				else {
					localdate = LocalDate.parse(token + "/"+thisyear, timeformatter);
				}
			    OffsetDateTime datetime = localdate.atStartOfDay(zone).toOffsetDateTime();
			    if (token.length()<=5 && datetime.isBefore(now)) {
			    	datetime = datetime.plusYears(1);
			    }
				schedule.add(datetime);
			}
			catch (DateTimeParseException e) {
				logger.error(e);
			}
		}
		Collections.sort(schedule, new Comparator<OffsetDateTime>() {
			@Override
			public int compare(OffsetDateTime a, OffsetDateTime b) {
				return a.compareTo(b);
			}
		});	
		return schedule;
	}
	
	public OffsetDateTime getNextExecution() {
		OffsetDateTime now = OffsetDateTime.now();
		now = truncate(now);
		for (OffsetDateTime time : getSchedule()) {
			if (time.isAfter(now)) {
				return time;
			}
		}
		return null;
	}
	
	private OffsetDateTime truncate(OffsetDateTime time) {
		ZoneId zone = ZoneId.systemDefault();
		String datevalue = timeformatter.format(time);
		LocalDate localdate = LocalDate.parse(datevalue, timeformatter);
	    OffsetDateTime truncated = localdate.atStartOfDay(zone).toOffsetDateTime();
	    return truncated;
	}
}
