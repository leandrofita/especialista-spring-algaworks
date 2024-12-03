package com.algaworks.algafood.api.exceptionhandler;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Getter;

@JsonInclude(Include.NON_NULL)
@Getter
@Builder
public class Problem {
	
	private Integer status;
	private String type;
	private String title;
	private String detail;
	
	private String userMessage;
	private OffsetDateTime timestamp;
	
	private List<IvalidObject> invalidObjects;
	
	@Getter
	@Builder
	public static class IvalidObject {
		
		private String name;
		private String userMessage;
	}

}
