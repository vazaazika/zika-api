package br.les.opus.dengue.api.test.integration;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import br.les.opus.dengue.api.test.util.RestAssuredTest;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;

public class PointOfInterestControllerIT extends RestAssuredTest {
	
	private void deletePoi(Integer id) {
		expect()
		  .statusCode(HttpStatus.OK.value())
		.given()
		  .header("X-Auth-User-Token", TEST_TOKEN)
		.when()
		  .delete("/poi/" + id);
	}
	
	public Integer postTestPicture() {
		Response response = expect()
		  .statusCode(HttpStatus.OK.value())
		.given()
		  .multiPart(new File(TEST_DIR + "picture/web.png"))
		.when()
		  .post("/picture/upload")
		.then().extract().response();
		
		return response.path("id");
	}
	
	
	@Test
	public void postPicturesAndFieldsWithToken() throws Exception {
		File postJson = new File(TEST_DIR + "poi/post-with-pictures-and-fields.json");
		File schemaFile = new File(SCHEMA_DIR + "poi/poi-get-{id}.json");
		
		Map<String, String> variableValues = new HashMap<String, String>();
		variableValues.put("PICTURE_ID_1", postTestPicture().toString());
		variableValues.put("PICTURE_ID_2", postTestPicture().toString());
		variableValues.put("PICTURE_ID_3", postTestPicture().toString());
		
		Response response = expect()
		  .statusCode(HttpStatus.CREATED.value())
		.given()
		  .contentType(JSON_CONTENT_TYPE)
		  .body(super.getContent(postJson, variableValues))
		  .header("X-Auth-User-Token", TEST_TOKEN)
		.when()
		  .post("/poi")
		.then()
		  .body(matchesJsonSchema(getJsonSchemaContent(schemaFile)))
		.extract().response();
		
		Integer id = response.path("id");
		deletePoi(id);
	}
	
	
	@Test
	public void postNoPicturesAnonimous() throws Exception {
		File postJson = new File(TEST_DIR + "poi/post-no-pictures.json");
		File schemaFile = new File(SCHEMA_DIR + "poi/poi-get-{id}.json");
		
		Response response = expect()
		  .statusCode(HttpStatus.CREATED.value())
		.given()
		  .contentType(JSON_CONTENT_TYPE)
		  .body(super.getContent(postJson))
		.when()
		  .post("/poi")
		.then()
		  .body(matchesJsonSchema(getJsonSchemaContent(schemaFile)))
		  .body("pictures", equalTo(new ArrayList<>()))
		  .body("fieldValues", equalTo(new ArrayList<>()))
		.extract().response();
		
		Integer id = response.path("id");
		deletePoi(id);
	}
	
	@Test
	public void postInvalidPoiAnonimous() throws Exception {
		expect()
		  .statusCode(HttpStatus.BAD_REQUEST.value())
		.given()
		  .contentType(JSON_CONTENT_TYPE)
		.when()
		  .post("/poi")
		.then();
	}
	
	@Test
	public void posMissingTitleAnonimous() throws Exception {
		File postJson = new File(TEST_DIR + "poi/post-missing-title.json");
		
		expect()
		  .statusCode(HttpStatus.BAD_REQUEST.value())
		.given()
		  .contentType(JSON_CONTENT_TYPE)
		  .body(super.getContent(postJson))
		.when()
		  .post("/poi");
	}
	
	
	@Test
	public void postNoPicturesWithToken() throws Exception {
		File postJson = new File(TEST_DIR + "poi/post-no-pictures.json");
		File schemaFile = new File(SCHEMA_DIR + "poi/poi-get-{id}.json");
		
		Response response = expect()
		  .statusCode(HttpStatus.CREATED.value())
		.given()
		  .header("X-Auth-User-Token", TEST_TOKEN)
		  .contentType(JSON_CONTENT_TYPE)
		  .body(super.getContent(postJson))
		.when()
		  .post("/poi")
		.then()
		  .body(matchesJsonSchema(getJsonSchemaContent(schemaFile)))
		  .body("user.id", equalTo(1))
		  .body("pictures", equalTo(new ArrayList<>()))
		  .body("fieldValues", equalTo(new ArrayList<>()))
		.extract().response();
		
		Integer id = response.path("id");
		deletePoi(id);
	}
	
	@Test
	public void postWithInvalidToken() throws Exception {
		File postJson = new File(TEST_DIR + "poi/post-no-pictures.json");
		File schemaFile = new File(SCHEMA_DIR + "poi/poi-get-{id}.json");
		
		Response response = expect()
		  .statusCode(HttpStatus.CREATED.value())
		.given()
		  .header("X-Auth-User-Token", "invalid token :)")
		  .contentType(JSON_CONTENT_TYPE)
		  .body(super.getContent(postJson))
		.when()
		  .post("/poi")
		.then()
		  .body(matchesJsonSchema(getJsonSchemaContent(schemaFile)))
		  .body("user", equalTo(null))
		  .body("pictures", equalTo(new ArrayList<>()))
		  .body("fieldValues", equalTo(new ArrayList<>()))
		.extract().response();
		
		Integer id = response.path("id");
		deletePoi(id);
	}
	
	@Test
	public void getAllEmptyTest() throws Exception {
		ValidatableResponse response = get("/poi").then();
		File schemaFile = new File(SCHEMA_DIR + "poi/poi-get.json");
		response.body(matchesJsonSchema(getJsonSchemaContent(schemaFile)));
		response.body("content", equalTo(new ArrayList<>()));
		response.statusCode(HttpStatus.OK.value());
	}
}
