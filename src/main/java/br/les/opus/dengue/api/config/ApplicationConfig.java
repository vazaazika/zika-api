package br.les.opus.dengue.api.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.vividsolutions.jts.geom.Point;

import br.les.opus.commons.rest.deserializers.JtsPointDeserializer;
import br.les.opus.commons.rest.serializers.IsoSimpleDateSerializer;
import br.les.opus.commons.rest.serializers.JtsPointSerializer;
import br.les.opus.dengue.api.i18n.I18nMappingJackson2HttpMessageConverter;

@Configuration
@EnableWebMvc
@ComponentScan
class ApplicationConfig extends WebMvcConfigurerAdapter {

	@Autowired
	private I18nMappingJackson2HttpMessageConverter converter;

	/* Here we register the Hibernate4Module into an ObjectMapper, then set this custom-configured ObjectMapper
	 * to the MessageConverter and return it to be added to the HttpMessageConverters of our application*/
	public MappingJackson2HttpMessageConverter jacksonMessageConverter(){

		MappingJackson2HttpMessageConverter messageConverter = converter;

		ObjectMapper mapper = new ObjectMapper();

		//Registrando Hibernate4Module para suportar lazy objects
		mapper.registerModule(new Hibernate4Module());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		// yyyy-MM-dd'T'HH:mm:ss'Z'
		SimpleModule dateModule = new SimpleModule("customDateModule");
		dateModule.addSerializer(new IsoSimpleDateSerializer());
		mapper.registerModule(dateModule);

		SimpleModule geopointModule = new SimpleModule("geopointModule");
		geopointModule.addSerializer(Point.class, new JtsPointSerializer());
		geopointModule.addDeserializer(Point.class, new JtsPointDeserializer());
		mapper.registerModule(geopointModule);

		messageConverter.setObjectMapper(mapper);
		return messageConverter;
	}

	@Override 
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(jacksonMessageConverter());
		super.configureMessageConverters(converters);
	}

}