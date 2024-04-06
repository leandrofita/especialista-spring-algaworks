package com.algaworks.algafood;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.algaworks.algafood.domain.exceptions.CozinhaNaoEncontradaException;
import com.algaworks.algafood.domain.exceptions.EntidadeEmUsoException;
import com.algaworks.algafood.domain.model.Cozinha;
import com.algaworks.algafood.domain.model.Restaurante;
import com.algaworks.algafood.domain.service.CadastroCozinhaService;
import com.algaworks.algafood.domain.service.CadastroRestauranteService;

@SpringBootTest
class CadastroCozinhaIT {

	@Autowired
	private CadastroCozinhaService cadastroCozinha;
	
	@Autowired
	private CadastroRestauranteService cadastroRestaurante;
	
	
	@Test
	void deveAtribuirId_QuandoCadatsrarCOzinhaComDAdosCorretos() {
		
		Cozinha novaCozinha = new Cozinha();
		novaCozinha.setNome("Chinesa");
		
		novaCozinha = cadastroCozinha.salvar(novaCozinha);
		
		assertNotNull(novaCozinha);
		assertNotNull(novaCozinha.getId());
		
	}
	
	@Test
	void deveFalhar_QuandoCadastrarUmaCozinhaSemNome() {

		Cozinha novaCozinha = new Cozinha();
		novaCozinha.setNome(null);

		ConstraintViolationException erroEsperado = assertThrows(ConstraintViolationException.class, () -> {
			cadastroCozinha.salvar(novaCozinha);
		});

		assertNotNull(erroEsperado);

	}
	
	@Test
	void deveFalhar_QuandoExcluriCozinhaEmUSo() {
		
		Cozinha cozinha = new Cozinha();
		cozinha.setNome("Americana");
		
		Cozinha cozinhaSalva = cadastroCozinha.salvar(cozinha);
		
		Long id = cozinhaSalva.getId();
		
		Restaurante restaurante = new Restaurante();
		restaurante.setCozinha(cozinha);
		restaurante.setNome("Teste");
		restaurante.setTaxaFrete(BigDecimal.valueOf(10));
	
		cadastroRestaurante.salvar(restaurante);
		
		EntidadeEmUsoException erroEsperado = assertThrows(EntidadeEmUsoException.class, () -> {
			cadastroCozinha.excluir(id);
		});
		
		assertNotNull(erroEsperado);
	}
	
	@Test
	void deveFalhar_QuandoExcluirCozinhaInexistente() {
		
		Long id = Long.valueOf(999999);
		
		CozinhaNaoEncontradaException erroEsperado = assertThrows(CozinhaNaoEncontradaException.class, () -> {
			cadastroCozinha.excluir(id);
		});
		
		assertNotNull(erroEsperado);
	}

}
