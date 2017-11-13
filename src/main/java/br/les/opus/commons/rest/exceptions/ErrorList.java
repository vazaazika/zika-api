package br.les.opus.commons.rest.exceptions;

import java.util.ArrayList;
import java.util.List;

public class ErrorList {

	private List<ServiceError> errors;
	
	public ErrorList() {
		this.errors = new ArrayList<ServiceError>();
	}

	public List<ServiceError> getErrors() {
		return errors;
	}

	public void setErrors(List<ServiceError> erros) {
		this.errors = erros;
	}
	
	public void addError(ServiceError erro) {
		this.errors.add(erro);
	}
}
