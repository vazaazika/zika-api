package br.les.opus.commons.rest.serializers;

import java.io.IOException;

import br.les.opus.commons.rest.geo.LatLng;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.vividsolutions.jts.geom.Point;


public class JtsPointSerializer extends StdSerializer<Point> {

	public JtsPointSerializer() {
		super(Point.class);
	}

	@Override
	public void serialize(Point value, JsonGenerator jgen, SerializerProvider provider) 
			throws IOException, JsonGenerationException {
		
		jgen.writeObject(new LatLng(value));
	}
}
