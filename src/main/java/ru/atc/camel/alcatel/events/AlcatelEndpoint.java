package ru.atc.camel.alcatel.events;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultPollingEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;

@UriEndpoint(scheme="alcatel", title="Alcatel", syntax="alcatel://operationPath", consumerOnly=true, consumerClass=AlcatelConsumer.class, label="alcatel")
public class AlcatelEndpoint extends DefaultPollingEndpoint {

	public AlcatelEndpoint(String uri, String operationPath, AlcatelComponent component) {
		super(uri, component);
		this.operationPath = operationPath;
	}
	
	private String operationPath;

	@UriParam
	private AlcatelConfiguration configuration;

	public Producer createProducer() throws Exception {
		throw new UnsupportedOperationException("Alcatelroducer is not implemented");
	}

	@Override
	public Consumer createConsumer(Processor processor) throws Exception {
		AlcatelConsumer consumer = new AlcatelConsumer(this, processor);
        return consumer;
	}

	public boolean isSingleton() {
		return true;
	}

	public String getOperationPath() {
		return operationPath;
	}

	public void setOperationPath(String operationPath) {
		this.operationPath = operationPath;
	}

	public AlcatelConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(AlcatelConfiguration configuration) {
		this.configuration = configuration;
	}
	
}