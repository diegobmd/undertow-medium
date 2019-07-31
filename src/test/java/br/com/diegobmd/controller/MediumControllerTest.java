package br.com.diegobmd.controller;

import br.com.diegobmd.model.Echo;
import br.com.diegobmd.utils.ServerExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.ContentType.TEXT;
import static io.undertow.util.StatusCodes.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ServerExtension.class)
class MediumControllerTest {

//    caso necessario mocker servicos
//    public static class MockModule implements Module {
//        @Override
//        public void configure(Binder binder) {
//           binder.bind(Service.class)
//                    .toInstance(Mockito.mock(Service.class));
//        }
//    }

    @Inject
    private ObjectMapper mapper;

    @Test
    void echo() {
        given()
                .accept(TEXT)
                .when()
                .get("/echo")
                .then().assertThat()
                .statusCode(200)
                .body( not(isEmptyOrNullString()));
    }

    @Test
    void echoJson() {
        Echo request = new Echo();

        request.setMessage("Test");

        given()
                .accept(JSON)
                .contentType(JSON)
                .body(request)
                .when()
                .post("/json")
                .then().assertThat()
                .statusCode(200)
                .body("message", not(isEmptyOrNullString()));
    }
}