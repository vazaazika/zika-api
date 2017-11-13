package br.les.opus.dengue.api.i18n;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.les.opus.dengue.core.i18n.Internationalizer;
import br.les.opus.dengue.core.i18n.InternationalizerFactory;
import br.les.opus.dengue.core.i18n.providers.LanguageProvider;

@Component
public class I18nMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

	@Autowired
	private InternationalizerFactory factory;
	
	@Autowired
	private LanguageProvider provider;
	
	@Autowired
	private Environment env;
	
	private Logger logger = Logger.getLogger(I18nMappingJackson2HttpMessageConverter.class); 
	
	private void internationalize(Object object) {
		String lang = provider.getUserLanguage();
		String defaultLanguage = env.getProperty("i18n.default.language");
		if (lang != null && !lang.equals(defaultLanguage)) {
			Internationalizer internationalizer = factory.create(object);
			if (internationalizer != null) {
				logger.info("Internacionalizando " + object);
				internationalizer.internationalize(object);
			} else {
				logger.warn("Retorno sem possibilidade de internacionalização " + object);
			}
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	protected void writeInternal(Object object, HttpOutputMessage outputMessage)  throws IOException, HttpMessageNotWritableException {
		if (object != null) {
			internationalize(object);
		}
		if (object instanceof MappingJacksonValue) {
			MappingJacksonValue responseWrapper = (MappingJacksonValue)object;
			if (responseWrapper.getValue() == null) {
				return;
			}
		}
		super.writeInternal(object, outputMessage);
	}
}
