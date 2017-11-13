package br.les.opus.commons.rest.controllers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.lang.reflect.ParameterizedType;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import br.les.opus.commons.persistence.IdAware;
import br.les.opus.commons.rest.serializers.IsoSimpleDateSerializer;

public class AbstractController<T extends IdAware<Long>> {
	
	@SuppressWarnings("unchecked")
	protected Class<T> getEntityClass() {
		return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		/*
		 * Cria um DateEditor customizado para que as datas sejam dadas no formato
		 * ISO8601
		 */
		SimpleDateFormat simpleIsoDateFormat = IsoSimpleDateSerializer.DATE_ISO_FORMATTER;
		simpleIsoDateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(simpleIsoDateFormat, true));
	}
	
	protected <K extends IdAware<Long>> PagedResources<Resource<K>> toPagedResources(Page<K> page, PagedResourcesAssembler<K> assembler) {
		PagedResources<Resource<K>> resources = assembler.toResource(page);
		Collection<Resource<K>> collection = resources.getContent();
		for (Resource<K> object : collection) {
			Resource<K> resource = (Resource<K>)object;
			K t = (K)resource.getContent();
			Link detail = linkTo(this.getClass()).slash(t.getId()).withSelfRel();
			resource.add(detail);
		}
		return resources;
	}
	
	
	/**
	 * Método chamado em cada objeto carregado como resposta. Pode
	 * ser usado por classes filhas para alterar alguns atributos
	 * dos objetos antes do resultado ser retornado para os usuários
	 * @param t objeto encontrado como resultado
	 * @return o objeto filtrado. Na implementação padrão, nada é feito.
	 */
	protected T doFiltering(T t) {
		return t;
	}
	
}
