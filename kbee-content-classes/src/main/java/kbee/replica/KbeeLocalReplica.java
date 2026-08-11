package kbee.replica;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.beans.BeansService;
import com.novamens.dom.Domain;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.service.ServiceLocator;

import kbee.api.service.ApiService;

@Entity
@DiscriminatorValue(value="3")
public class KbeeLocalReplica extends KbeeReplicaStandBy implements ReplicaStandBy {

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeDomain.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "localdomain_id", updatable=false)
	private Domain localDomain;
		
	public KbeeLocalReplica() {
		setType(ReplicaType.LOCAL);
	}
	
	public Domain getLocalDomain() {
		return localDomain;
	}
	
	public void setLocalDomain(Domain localDomain) {
		this.localDomain = localDomain;
	}

	@Override
	public ApiService getApi() {
		ApiService api = (ApiService)ServiceLocator.getService(BeansService.class).getBean("ApiServiceLocalWrapper", getLocalDomain());
		return api;
	}
}