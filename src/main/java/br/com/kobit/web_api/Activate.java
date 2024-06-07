package br.com.kobit.web_api;

import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.fluig.sdk.api.component.activation.ActivationEvent;
import com.fluig.sdk.api.component.activation.ActivationListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Remote
@Singleton(mappedName = "activator/kobitwebapi", name = "activator/kobitwebapi")
public class Activate implements ActivationListener{
	@Override
	public String getArtifactFileName() throws Exception {
		return "kobit_api";
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void install(ActivationEvent event) throws Exception {
	}
	
	@Override
	public void disable(ActivationEvent evt) throws Exception {
	}

	@Override
	public void enable(ActivationEvent evt) throws Exception {
		log.info("Ativando kobit_api");
	}
}
