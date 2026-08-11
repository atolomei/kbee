package com.novamens.kbee.content.form;

import com.novamens.content.form.EForm;
import com.novamens.kbee.content.service.KbeeDomService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component @Scope("prototype")
public class KbeeEFormDomService extends KbeeDomService<KbeeEForm, EForm> {

}