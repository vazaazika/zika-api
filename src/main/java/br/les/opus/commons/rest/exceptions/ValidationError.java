package br.les.opus.commons.rest.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ValidationError extends ServiceError {
	
	private String field;
	
	private String rejectedValue;
	
	private Boolean objectLevelError;

	public String getField() {
		return field;
	}

	public void setField(String campo) {
		this.field = campo;
	}

	public String getRejectedValue() {
		return rejectedValue;
	}

	public void setRejectedValue(String valorRejeitado) {
		this.rejectedValue = valorRejeitado;
	}

	public Boolean getObjectLevelError() {
		return objectLevelError;
	}

	public void setObjectLevelError(Boolean objectLevelError) {
		this.objectLevelError = objectLevelError;
	}
}
