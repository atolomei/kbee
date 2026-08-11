package com.novamens.kbee.content.repository;

import org.springframework.stereotype.Component;

import com.novamens.content.user.UserDevice;
import com.novamens.kbee.content.user.KbeeUserDevice;
import com.novamens.kbee.repository.AbstractDomRepository;

@Component
public class UserDeviceRepository extends AbstractDomRepository<KbeeUserDevice, UserDevice> {

}