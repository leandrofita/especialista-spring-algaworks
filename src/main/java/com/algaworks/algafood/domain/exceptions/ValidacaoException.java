package com.algaworks.algafood.domain.exceptions;

import org.springframework.validation.BindingResult;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ValidacaoException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private BindingResult bindingResult;

}
