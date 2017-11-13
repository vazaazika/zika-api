package br.les.opus.commons.rest.exceptions;

public class ServiceError {
	
	private String message;
	
	private String type;
	
	private Long code;

	public String getMessage() {
		return message;
	}

	public void setMessage(String mensagem) {
		this.message = mensagem;
	}

	public String getType() {
		return type;
	}

	public void setType(String tipo) {
		this.type = tipo;
	}

	public Long getCode() {
		return code;
	}

	public void setCode(Long codigo) {
		this.code = codigo;
	}
	
}
