package br.les.opus.commons.rest.serializers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.les.opus.commons.persistence.date.IsoDateFormat;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;


public class IsoSimpleDateSerializer extends DateSerializer {

	public static SimpleDateFormat DATE_ISO_FORMATTER;
	
	static {
		DATE_ISO_FORMATTER = new IsoDateFormat();
	}

	@Override
	public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException  {
		synchronized (DATE_ISO_FORMATTER) {
			jgen.writeString(DATE_ISO_FORMATTER.format(value));
		}
	}

}
