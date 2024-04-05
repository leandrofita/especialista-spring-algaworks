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
import org.springframework.web.bind.annotation.RestController;

import com.algaworks.algafood.domain.model.Estado;
import com.algaworks.algafood.domain.repository.EstadoRepository;
import com.algaworks.algafood.domain.service.CadastroEstadoService;

@RestController
@RequestMapping(path = "/estados")
public class EstadoController {

	@Autowired
	private EstadoRepository estadoRepository;

	@Autowired
	private CadastroEstadoService cadastroEstado;

	@GetMapping()
	public List<Estado> listar() {
		return estadoRepository.findAll();
	}

	@GetMapping("/{id}")
	public Estado buscar(final @PathVariable Long id) {
		return cadastroEstado.buscarOuFalhar(id);
	}

	@PostMapping()
	public ResponseEntity<Estado> adicionar(@RequestBody @Valid final Estado estado) {
		return ResponseEntity.status(HttpStatus.CREATED).body(cadastroEstado.salvar(estado));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Estado> atualizar(@PathVariable Long id, @RequestBody @Valid final Estado estado) {
		Estado estadoAtual = cadastroEstado.buscarOuFalhar(id);

		BeanUtils.copyProperties(estado, estadoAtual, "id");
		
		return ResponseEntity.ok(cadastroEstado.salvar(estadoAtual));

	}

	@DeleteMapping("/{id}")
	public void remover(@PathVariable Long id) {
		cadastroEstado.excluir(id);

	}

}
