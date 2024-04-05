package com.algaworks.algafood.api.controller;

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

import com.algaworks.algafood.domain.model.Cozinha;
import com.algaworks.algafood.domain.repository.CozinhaRepository;
import com.algaworks.algafood.domain.service.CadastroCozinhaService;

@RestController
@RequestMapping(path = "/cozinhas")
public class CozinhaController {
	
	@Autowired
	private CozinhaRepository cozinhaRepository;
	
	@Autowired
	private CadastroCozinhaService cadastroCozinha;
	
	@GetMapping
	public List<Cozinha> listar() {
		return cozinhaRepository.findAll();		
	}
	
	@GetMapping("/listarPorNome")
	public List<Cozinha> listarPorNome(@RequestParam(name = "nome")String nome) {
		return cozinhaRepository.findByNomeContaining(nome);		
	}
	
	@GetMapping("/{id}")
	public Cozinha buiscar(@PathVariable final Long id) {
		return cadastroCozinha.buscarOuFalhar(id);
		
	}
	@PostMapping
	public ResponseEntity<Cozinha> adicionar(@RequestBody @Valid final Cozinha cozinha) {
		return ResponseEntity.status(HttpStatus.CREATED).body(cadastroCozinha.salvar(cozinha));
		
	}
	
	@PutMapping("/{id}")
	public Cozinha atualizar(@PathVariable final Long id, 
			@RequestBody @Valid final Cozinha cozinha) {
		
		Cozinha cozinhaAtual = cadastroCozinha.buscarOuFalhar(id);
		
		BeanUtils.copyProperties(cozinha, cozinhaAtual, "id");
			
		return cadastroCozinha.salvar(cozinhaAtual);

	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remover(@PathVariable final Long id) {
			cadastroCozinha.excluir(id);
			
	}
	
	

}
