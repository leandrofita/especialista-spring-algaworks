package com.algaworks.algafood.api.controller;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.algaworks.algafood.api.assembler.RestauranteInputDisassembler;
import com.algaworks.algafood.api.assembler.RestauranteModelAssembler;
import com.algaworks.algafood.api.model.RestauranteModel;
import com.algaworks.algafood.api.model.input.RestauranteInput;
import com.algaworks.algafood.domain.exceptions.CozinhaNaoEncontradaException;
import com.algaworks.algafood.domain.exceptions.NegocioException;
import com.algaworks.algafood.domain.model.Restaurante;
import com.algaworks.algafood.domain.repository.RestauranteRepository;
import com.algaworks.algafood.domain.service.CadastroRestauranteService;

@RestController
@RequestMapping(path = "/restaurantes")
public class RestauranteController {
	
	@Autowired
	private RestauranteRepository restauranteRepository;
	
	@Autowired
	private CadastroRestauranteService cadastroRestaurante;
		
	@Autowired
	private RestauranteModelAssembler restauranteModelAssembler;
	
	@Autowired
	private RestauranteInputDisassembler disassembler;
	
	@GetMapping
	public List<RestauranteModel> listar() {		
		return restauranteModelAssembler.toCollectionModel(restauranteRepository.findAll());		
	}
	
	@GetMapping("/{id}")
	public RestauranteModel buscar(@PathVariable final Long id) {
		Restaurante restaurante = cadastroRestaurante.buscarOuFalhar(id);
		
		return restauranteModelAssembler.toModel(restaurante);
	
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RestauranteModel adicionar(@RequestBody @Valid final RestauranteInput restauranteInput) {
		try {
			Restaurante restauranteSalvo = cadastroRestaurante.salvar(disassembler.toDomainObject(restauranteInput));
			
			return restauranteModelAssembler.toModel(restauranteSalvo);
		} catch (CozinhaNaoEncontradaException e) {
			throw new NegocioException(e.getMessage(), e);
		}
		
	}
	
	@GetMapping("/buscarPorNomeEidCozinha")
	public ResponseEntity<List<RestauranteModel>> buscarPorNomeEidCozinha(@RequestParam(name = "nome") String nome, 
			@RequestParam(name = "id") Long id) {
		return ResponseEntity.ok(restauranteModelAssembler.toCollectionModel((restauranteRepository.consultarPorNomeEIdCozinha(nome, id))));
	}
	
	@GetMapping("/findByTaxaFreteBetween")
	public ResponseEntity<List<RestauranteModel>> buiscarPorTaxaFrete(@RequestParam(name = "taxaInicial") BigDecimal taxaInicial, 
			@RequestParam(name = "taxaFinal") BigDecimal taxaFinal) {
		return ResponseEntity.ok(restauranteModelAssembler.toCollectionModel(restauranteRepository.findByTaxaFreteBetween(taxaInicial, taxaFinal)));
	}
	
	@PutMapping("/{id}")
	public RestauranteModel atualizar(@PathVariable final Long id, 
			@RequestBody @Valid final RestauranteInput restauranteInput) {
		try {
			Restaurante restaurante = disassembler.toDomainObject(restauranteInput);
			
			Restaurante restauranteAtual = cadastroRestaurante.buscarOuFalhar(id);
			
			BeanUtils.copyProperties(restaurante, restauranteAtual, 
						"id", "formasPagamento", "endereco", "dataCadastro");
			
			Restaurante restauranteAtualizado = cadastroRestaurante.salvar(restauranteAtual);
			
			return restauranteModelAssembler.toModel(restauranteAtualizado);			
			} catch (CozinhaNaoEncontradaException e) {
				throw new NegocioException(e.getMessage(), e);
			}
		
	}
	
	@DeleteMapping("/{id}")
	public void remover(@PathVariable final Long id) {
			cadastroRestaurante.excluir(id);
	}
	
}
