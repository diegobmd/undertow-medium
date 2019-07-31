package br.com.diegobmd.service;

import br.com.diegobmd.model.Echo;

public interface MediumService {

    String echo();

    Echo echoJson(Echo echo);

}
