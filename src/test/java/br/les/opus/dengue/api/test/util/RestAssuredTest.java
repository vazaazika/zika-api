package br.les.opus.dengue.api.test.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.jayway.restassured.RestAssured;

public class RestAssuredTest {
	
	protected static final String TEST_DIR = "json/tests/";
	
	protected static final String SCHEMA_DIR = "json/schemas/";
	
	protected static final String TEST_TOKEN = "f2d1439ccbd57d6108185e0f7e387ecd";
	
	protected static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	
	public String getContent(File file) throws Exception {
		InputStream input =  new FileInputStream(file);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		IOUtils.copy(input, output);
		return new String(output.toByteArray(), "UTF-8");
	}
	
	public String getContent(File file, Map<String, String> variableValues) throws Exception {
		String content = getContent(file);
		for (String key : variableValues.keySet()) {
			content = content.replace("${" + key + "}", variableValues.get(key));
		}
		return content;
	}
	
	private String getSchemaDirAbsolutePath() {
		File folder = new File(SCHEMA_DIR);
		return folder.getAbsolutePath();
	}
	
	public String getJsonSchemaContent(File file) throws Exception {
		String content = getContent(file);
		return content.replace("${SCHEMAS_ABSOLUTE_FOLDER}", getSchemaDirAbsolutePath());
	}
	
	public RestAssuredTest() {
		RestAssured.port = 9999;
	}

}
