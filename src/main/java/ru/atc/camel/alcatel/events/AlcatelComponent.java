package ru.atc.camel.alcatel.events;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;

public class AlcatelComponent extends UriEndpointComponent {

	public AlcatelComponent() {
		super(AlcatelEndpoint.class);
	}

	@Override
	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		
		AlcatelEndpoint endpoint = new AlcatelEndpoint(uri, remaining, this);		
		AlcatelConfiguration configuration = new AlcatelConfiguration();
		
		// use the built-in setProperties method to clean the camel parameters map
		setProperties(configuration, parameters);
		
		endpoint.setConfiguration(configuration);		
		return endpoint;
	}
}