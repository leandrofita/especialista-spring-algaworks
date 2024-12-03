package com.algaworks.algafood.api.exceptionhandler;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.algaworks.algafood.api.exceptionhandler.Problem.IvalidObject;
import com.algaworks.algafood.domain.exceptions.EntidadeEmUsoException;
import com.algaworks.algafood.domain.exceptions.EntidadeNaoEncontradaException;
import com.algaworks.algafood.domain.exceptions.NegocioException;
import com.algaworks.algafood.domain.exceptions.ValidacaoException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
	
	private static final String MSG_ERRO_GENERICA_USUARIO_FINAL = "Ocorreu um erro inesperado no sistema. Tentenovamente e se o "
			+ "problema persistir, entre em contato com o administrador do sistema.";
	
	@Autowired
	private MessageSource mesageSource;
	
	
	@ExceptionHandler(ValidacaoException.class)
	public ResponseEntity<Object> handleValidacaoException(ValidacaoException ex, 
			WebRequest request){
		return handleValidationInternal(ex, ex.getBindingResult(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		return handleValidationInternal(ex, ex.getBindingResult(), headers, status, request);
		
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleUncaught(Exception ex, WebRequest request) {
		
		String detail = "Ocorreu um erro inesperado no sistema. Tentenovamente e se o "
				+ "problema persistir, entre em contato com o administrador do sistema.";
		
		Problem problem = createProblemBuilder(
				HttpStatus.INTERNAL_SERVER_ERROR,
				ProblemType.ERRO_DE_SISTEMA,
				detail)
				.userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
				.build();
		
		 ex.printStackTrace();
		
		
		return handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
		
	}
	
	
	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		String detail = String.format("O recurso '%s', que você tentou acessar, é inexistente", ex.getRequestURL());
		
		Problem problem = createProblemBuilder(
				HttpStatus.BAD_REQUEST,
				ProblemType.PARAMETRO_INVALIDO,
				detail)
				.userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		
		
		if(ex instanceof MethodArgumentTypeMismatchException argumentNotvalid) {
			return handleMethodArgumentTypeMismatch(argumentNotvalid, headers, status, request);
		} 
		
		return super.handleTypeMismatch(ex, headers, status, request);
	}
	
	
	
	
	private ResponseEntity<Object> handleMethodArgumentTypeMismatch(
			MethodArgumentTypeMismatchException ex, HttpHeaders headers, HttpStatus status,
			WebRequest request) {
		
		String detail = String.format("O parâmetro de url '%s', recebeu o valor '%s' que é de um tipo inválido. "
				+ "Corrija e informe um valor compatível com o tipo '%s'.", ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName() );
		
		Problem problem = createProblemBuilder(
				HttpStatus.BAD_REQUEST,
				ProblemType.PARAMETRO_INVALIDO,
				detail)
				.userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}


	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		Throwable rootCause = ExceptionUtils.getRootCause(ex);
		
		if(rootCause instanceof InvalidFormatException invalidFormatException) {
			return handleInvalidFormat(invalidFormatException, headers, status, request);
		} else if(rootCause instanceof PropertyBindingException propertyBindingException) {
			return handlePropertyBinding(propertyBindingException, headers, status, request);
		}
		
		String detail = "O corpo da requisição esta inválido. Verifique erro de síntaxe.";
		
		Problem problem = createProblemBuilder(
				status,
				ProblemType.MENSAGEM_INCOMPREENSIVEL,
				detail)
				.userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	private ResponseEntity<Object> handlePropertyBinding(PropertyBindingException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		String path = joinPath(ex.getPath());
		
		String detail = String.format("A propriedade '%s' não existe no objeto '%s'.",
				path, ex.getReferringClass().getSimpleName());

		Problem problem = createProblemBuilder(
				status,
				ProblemType.MENSAGEM_INCOMPREENSIVEL,
				detail)
				.userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
				.build();
		
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}

	private ResponseEntity<Object> handleInvalidFormat(InvalidFormatException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		String path = joinPath(ex.getPath());
	
		String detail = String.format("A propriedade '%s' recebeu o valor '%s' "
				+ "que é de um tipo inválido. Corrija e informe um valor compatiável com tipo '%s'.",
				path, ex.getValue(), ex.getTargetType().getSimpleName());
		
		Problem problem = createProblemBuilder(
				status,
				ProblemType.MENSAGEM_INCOMPREENSIVEL,
				detail)
				.userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}

	@ExceptionHandler(EntidadeEmUsoException.class)
	public ResponseEntity<?> handleEntidadeEmUso(EntidadeEmUsoException e, WebRequest request) {
		
		Problem problem = createProblemBuilder(
				HttpStatus.CONFLICT,
				ProblemType.RECURSO_NAO_ENCONTRADO,
				e.getMessage())
				.userMessage(e.getMessage())
				.build();
		
		return handleExceptionInternal(e, problem, new HttpHeaders(), HttpStatus.CONFLICT, request);
		
	}
	
	@ExceptionHandler(EntidadeNaoEncontradaException.class)
	public ResponseEntity<?> handleEntidadeNaoEncontrada(EntidadeNaoEncontradaException e, WebRequest request) {
		
		Problem problem = createProblemBuilder(
				HttpStatus.NOT_FOUND,
				ProblemType.RECURSO_NAO_ENCONTRADO,
				e.getMessage())
				.userMessage(e.getMessage())
				.build();
		
		return handleExceptionInternal(e, problem, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
		
	}
	
	@ExceptionHandler(NegocioException.class)
	public ResponseEntity<?> handleNegocio(NegocioException e, WebRequest request) {
		
		Problem problem = createProblemBuilder(
				HttpStatus.BAD_REQUEST,
				ProblemType.RECURSO_NAO_ENCONTRADO,
				e.getMessage())
				.userMessage(e.getMessage())
				.build();
		
		
		return handleExceptionInternal(e, problem, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
		
	}
	
	private ResponseEntity<Object> handleValidationInternal(Exception ex, BindingResult bindingResult,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
	
		String detail = "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente";
		
		List<IvalidObject> problemFields = bindingResult.getAllErrors().stream()
				.map(objectError -> {
					String message = mesageSource.getMessage(objectError, LocaleContextHolder.getLocale());
					
					String name = objectError.getObjectName();
					
					if(objectError instanceof FieldError fieldError) {
						name = fieldError.getField();
					}
					
					return Problem.IvalidObject.builder()
						.name(name)
						.userMessage(message)
						.build();
				}).toList();
		
		HttpStatus statusExcecao = HttpStatus.BAD_REQUEST;
		
		
		Problem problem = createProblemBuilder(
				statusExcecao,
				ProblemType.DADOS_INVALIDOS,
				detail)
				.userMessage(detail)
				.invalidObjects(problemFields)
				.build();
		
		return handleExceptionInternal(ex, problem, headers, status, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		
		if (body == null) {
			body = Problem.builder()
					.status(status.value())
					.title(status.getReasonPhrase())
					.userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
					.build();
		} else if (body instanceof String string) {
			body = Problem.builder()
					.status(status.value())
					.title(string)
					.userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
					.build();
		}
		
		
		return super.handleExceptionInternal(ex, body, headers, status, request);
	}
	
	private String joinPath(List<Reference> references) {
	    return references.stream()
	        .map(ref -> ref.getFieldName())
	        .collect(Collectors.joining("."));
	}   
	
	private Problem.ProblemBuilder createProblemBuilder(HttpStatus status, 
			ProblemType problemType, String detail) {
		return Problem.builder()
				.status(status.value())
				.type(problemType.getUri())
				.title(problemType.getTitle())
				.timestamp(OffsetDateTime.now())
				.detail(detail);
	}

}
