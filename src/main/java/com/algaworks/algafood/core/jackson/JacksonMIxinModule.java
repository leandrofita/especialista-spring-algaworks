package com.algaworks.algafood.core.jackson;

import org.springframework.stereotype.Component;

import com.algaworks.algafood.api.model.mixin.RestauranteMixin;
import com.algaworks.algafood.domain.model.Restaurante;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Component
public class JacksonMIxinModule extends SimpleModule {
	private static final long serialVersionUID = 1L;

	public JacksonMIxinModule() {
		setMixInAnnotation(Restaurante.class, RestauranteMixin.class);
	}

}
