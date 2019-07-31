package br.com.diegobmd.handler;

import br.com.diegobmd.config.ServerProperties;
import br.com.diegobmd.controller.MediumController;
import br.com.diegobmd.model.Echo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Disponibiliza o handler dos requests da aplicacao
 */
public class HandlerProvider {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ServerProperties config;
    private final ObjectMapper mapper;

    private final MediumController mediumController;

    @Inject
    public HandlerProvider(ServerProperties config,
                           ObjectMapper mapper,
                           MediumController mediumController) {

        this.config = config;
        this.mapper = mapper;
        this.mediumController = mediumController;
    }

    /**
     * Retorna o fluxo da aplicacao
     * <p>
     * - Exception Handler, caso erro pega a exception e trata o retorno em json
     * -- Routing Handler, roteia os paths da aplicacao
     * --- Authentication Handler, consulta o auth para verficar a percissao ao recurso
     * ---- Wrapper (Json/File/etc) Handler, caso necessario realiza o parser do body
     * ----- Controller Handler executa a logica da aplicacao
     *
     * @return HttpHandler root
     */
    public HttpHandler getHandler() {

        LOGGER.debug("HttpHandler initializing");

        String ctxp = config.getContextPath();

        HttpHandler path = Handlers.path()
                .addExactPath(ctxp + "/echo", text(mediumController::echo))
                .addExactPath(ctxp + "/json", new BlockingHandler(json(Echo.class, mediumController::echoJson)))
                ;

        return path;
    }


    /**
     * Realiza a leitura do json de request do exchange
     */
    private <T> T readJson(HttpServerExchange exchange, Class<T> type) throws IOException {

        String contentType = exchange.getRequestHeaders().getFirst(Headers.CONTENT_TYPE);

        if (contentType != null && contentType.startsWith("application/json") && exchange.isRequestChannelAvailable()) {

            InputStream is = exchange.getInputStream();

            if (is != null && is.available() != -1) {
                return this.mapper.readValue(is, type);
            }

            return null;
        } else {
            return null;
        }
    }

    /**
     * Realiza o parser do objeto para json e envia ao exchange
     */
    private void writeJson(HttpServerExchange exchange, Object resp) {
        if (resp == null)
            return;

        try {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(ByteBuffer.wrap(this.mapper.writeValueAsBytes(resp)));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error ao escrever a resposta", e);
        }
    }

    /**
     * Cria o HttpHandler wrapper para leitura do json e parse para o request type
     * Apos isso realiza a chamada ao controller com o request e retorna o json do objeto retornado
     */
    private <A, B> HttpHandler json(final Class<A> requestType, final BiFunction<HttpServerExchange, A, B> next) {

        return new BlockingHandler((e) -> {

            A req = readJson(e, requestType);
            B resp = next.apply(e, req);

            writeJson(e, resp);
        });
    }

    /**
     * Cria o HttpHandler wrapper para escrita do json edo objeto retornado
     */
    private HttpHandler text(final Function<HttpServerExchange, String> next) {
        return (e) -> {

            String resp = next.apply(e);

            e.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
            e.getResponseSender().send(resp);
        };
    }
}
