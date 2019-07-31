package br.com.diegobmd.controller;

import br.com.diegobmd.model.Echo;
import br.com.diegobmd.service.MediumService;
import io.undertow.server.HttpServerExchange;

import javax.inject.Inject;

public class MediumController {

    private final MediumService service;

    @Inject
    public MediumController(MediumService service) {
        this.service = service;
    }

    public String echo(HttpServerExchange exchange) {
        return service.echo();
    }

    public Echo echoJson(HttpServerExchange exchange, Echo echo) {
        return service.echoJson(echo);
    }

}
