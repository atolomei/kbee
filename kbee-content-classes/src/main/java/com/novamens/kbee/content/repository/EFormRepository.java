package com.novamens.kbee.content.repository;

import org.springframework.stereotype.Component;

import com.novamens.content.form.EForm;
import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.kbee.repository.AbstractDomRepository;

@Component 
public class EFormRepository extends AbstractDomRepository<KbeeEForm, EForm> {

}
