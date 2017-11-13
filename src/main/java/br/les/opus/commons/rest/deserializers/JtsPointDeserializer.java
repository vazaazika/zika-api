package br.les.opus.commons.rest.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class JtsPointDeserializer extends JsonDeserializer<Point>{
	
	private GeometryFactory factory;
	
	public JtsPointDeserializer() {
		factory = new GeometryFactory();
	}

	@Override
	public Point deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		
		JsonNode node = jp.getCodec().readTree(jp);
		Double lat = (Double) ((DoubleNode) node.get("lat")).numberValue();
		Double lng = (Double) ((DoubleNode) node.get("lng")).numberValue();
		
		return factory.createPoint(new Coordinate(lng, lat));
	}

}
