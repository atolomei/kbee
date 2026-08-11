package com.novamens.kbee.content.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novamens.content.user.UserSignature;
import com.novamens.kbee.content.user.KbeeUserSignature;

@Component @Scope("prototype")
public class KbeeUserSignatureDomService extends KbeeDomService<KbeeUserSignature, UserSignature> {

}
