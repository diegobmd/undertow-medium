package br.com.diegobmd.service;

import br.com.diegobmd.model.Echo;

public class MediumServiceImpl implements MediumService {
    @Override
    public String echo() {
        return "Hello World";
    }

    @Override
    public Echo echoJson(Echo echo) {
        Echo result = new Echo();
        result.setMessage(String.format("Hello %s!", echo.getMessage()));
        return result;
    }
}


