package com.algaworks.algafood.api.controller;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.algaworks.algafood.api.model.RestauranteModel;
import com.algaworks.algafood.domain.exceptions.CozinhaNaoEncontradaException;
import com.algaworks.algafood.domain.exceptions.NegocioException;
import com.algaworks.algafood.domain.exceptions.ValidacaoException;
import com.algaworks.algafood.domain.model.Restaurante;
import com.algaworks.algafood.domain.repository.RestauranteRepository;
import com.algaworks.algafood.domain.service.CadastroRestauranteService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(path = "/restaurantes")
public class RestauranteController {
	
	@Autowired
	private RestauranteRepository restauranteRepository;
	
	@Autowired
	private CadastroRestauranteService cadastroRestaurante;
	
	@Autowired
	private SmartValidator validator;
	
	@GetMapping
	public List<Restaurante> listar() {
		return restauranteRepository.findAll();		
	}
	
	@GetMapping("/{id}")
	public RestauranteModel buiscar(@PathVariable final Long id) {
		Restaurante restaurante = cadastroRestaurante.buscarOuFalhar(id);
		
		RestauranteModel restauranteModel = new RestauranteModel();
		
		return restauranteModel;
	
	}
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Restaurante adicionar(@RequestBody @Valid final Restaurante restaurante) {
		try {
			return cadastroRestaurante.salvar(restaurante);
		} catch (CozinhaNaoEncontradaException e) {
			throw new NegocioException(e.getMessage(), e);
		}
		
	}
	
	@GetMapping("/buscarPorNomeEidCozinha")
	public ResponseEntity<List<Restaurante>> buscarPorNomeEidCozinha(@RequestParam(name = "nome") String nome, 
			@RequestParam(name = "id") Long id) {
		return ResponseEntity.ok(restauranteRepository.consultarPorNomeEIdCozinha(nome, id));
	}
	
	@GetMapping("/findByTaxaFreteBetween")
	public ResponseEntity<List<Restaurante>> buiscarPorTaxaFrete(@RequestParam(name = "taxaInicial") BigDecimal taxaInicial, 
			@RequestParam(name = "taxaFinal") BigDecimal taxaFinal) {
		return ResponseEntity.ok(restauranteRepository.findByTaxaFreteBetween(taxaInicial, taxaFinal));
	}
	
	@PutMapping("/{id}")
	public Restaurante atualizar(@PathVariable final Long id, 
			@RequestBody @Valid final Restaurante restaurante) {
		try {
			Restaurante restauranteAtual = cadastroRestaurante.buscarOuFalhar(id);
			
			BeanUtils.copyProperties(restaurante, restauranteAtual, 
						"id", "formasPagamento", "endereco", "dataCadastro");
				return cadastroRestaurante.salvar(restauranteAtual);			
			} catch (CozinhaNaoEncontradaException e) {
				throw new NegocioException(e.getMessage(), e);
			}
		
	}
	
	@DeleteMapping("/{id}")
	public void remover(@PathVariable final Long id) {
			cadastroRestaurante.excluir(id);
	}
	
	@PatchMapping("/{id}")
	public Restaurante atualizarParcial(@PathVariable final Long id, @RequestBody Map<String, Object> campos, HttpServletRequest request) {
		Restaurante restauranteAtual = cadastroRestaurante.buscarOuFalhar(id);
		
		merge(campos, restauranteAtual, request);
		
		validate(restauranteAtual, "restaurante");
		
		return atualizar(id, restauranteAtual);

	}

	private void validate(Restaurante restaurante, String objectName) {
		
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(restaurante, objectName);
		
		validator.validate(restaurante, bindingResult);
		
		if(bindingResult.hasErrors()) {
			throw new  ValidacaoException(bindingResult);
		}
		
	}

	private void merge(Map<String, Object> camposOrigem, Restaurante restauranteDestino, HttpServletRequest request) {
		
		ServletServerHttpRequest serverHttpRequest = new ServletServerHttpRequest(request);
		
		try {
			ObjectMapper mapper = new ObjectMapper();

			// configurando o ObjectMapper para falhar ao encontrar propriedades ignoradas
			// ou inexistentes
			mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

			// copia do map para uma entidade
			Restaurante restauranteOrigem = mapper.convertValue(camposOrigem, Restaurante.class);

			camposOrigem.forEach((nomePropriedade, valorPropriedade) -> {
				Field field = ReflectionUtils.findField(Restaurante.class, nomePropriedade);
				// torna acessível uma variável privada da classe
				field.setAccessible(true);
				// pega o valor da propriedade da entidade pelo nome do campo no map
				Object novoValor = ReflectionUtils.getField(field, restauranteOrigem);

				// seta na entidade destino o valor copiado acessando pelo nome do campo no map
				ReflectionUtils.setField(field, restauranteDestino, novoValor);
			});
		} catch (IllegalArgumentException e) {
			Throwable rootCause = ExceptionUtils.getRootCause(e);
			throw new HttpMessageNotReadableException(e.getMessage(), rootCause, serverHttpRequest);
		}
		
	}
	
	

}
