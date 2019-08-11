package com.ctc.exercises;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Properties;

import static io.restassured.RestAssured.given;

public class JiraAPICalls {

    public static final String JSON_FILE_1="src/test/resources/data/create-session.json";
    public static final String JSON_FILE_2="src/test/resources/data/create-issue.json";
    public static final String JSON_FILE_3="src/test/resources/data/create-comment.json";
    public static final String JSON_FILE_4="src/test/resources/data/update-comment.json";


    Properties prop = new Properties();

    @BeforeSuite
    public void getProperties() throws Exception{

        //load the properties file with environment settings
        prop.load(new FileInputStream("src/test/resources/properties/env.properties"));
        RestAssured.baseURI = prop.getProperty("HOST");
    }

    @Test(enabled = true)
    public String createSession() throws Exception { //create Session ID for the logged in User

        //Read the properties file and convert to jsonObject
        JSONParser parser =  new JSONParser();
        Object obj = parser.parse(new FileReader(JSON_FILE_1));
        JSONObject jsonObject = (JSONObject) obj;

        Response res =
                given().
                        header("Content-Type","application/json").and().
                        body(jsonObject.toJSONString()).
                when().
                        post("/rest/auth/1/session").
                then().
                        statusCode(200).log().all().extract().response();

        //extracting values from the RestAssured Response
        JsonPath jp = res.jsonPath();
        String sessionName = jp.get("session.name"); //session Name
        String sessionID = jp.get("session.value");  //session Value
        System.out.println(sessionName);
        System.out.println(sessionID);
        return sessionID;
    }

    @Test(enabled = true)
    public String createIssue() throws Exception {//create new issue using already created session Id
        //Read the properties file and convert to jsonObject
        JSONParser parser =  new JSONParser();
        Object obj = parser.parse(new FileReader(JSON_FILE_2));
        JSONObject jsonObject = (JSONObject) obj;

        Response res =
                given().
                        header("Cookie","JSESSIONID="+createSession()).and().
                        header("Content-Type","application/json").
                        body(jsonObject.toJSONString()).
                when().
                        post("/rest/api/2/issue").
                then().
                        statusCode(201).log().all().extract().response();

        //extracting values from the RestAssured Response
        JsonPath jp = res.jsonPath();
        String issueID = jp.get("id");
        System.out.println("New Issue ID:" +issueID);//10005
        return issueID;
    }

    @Test(enabled = true)
    public String createComment() throws Exception{ //Inserting comment to the newly created Issue
        //Read the properties file and convert to jsonObject
        JSONParser parser =  new JSONParser();
        Object obj = parser.parse(new FileReader(JSON_FILE_3));
        JSONObject jsonObject = (JSONObject) obj;

        Response res =
                given().
                        header("cookie","JSESSIONID="+createSession()).and().
                        header("Content-Type","application/json").
                        body(jsonObject.toJSONString()).
                when().
                        post("/rest/api/2/issue/10008/comment").
                then().
                        statusCode(201).log().all().extract().response();
        //extracting values from the RestAssured Response
        JsonPath jp = res.jsonPath();
        String commentID = jp.get("id");
        System.out.println("New Comment ID:" +commentID);//10002
        return commentID;
    }

    @Test
    public void updateComment() throws Exception{//Updating comment
        //Read the properties file and convert to jsonObject
        JSONParser parser =  new JSONParser();
        Object obj = parser.parse(new FileReader(JSON_FILE_4));
        JSONObject jsonObject = (JSONObject) obj;

        Response res =
                given().
                        header("cookie","JSESSIONID="+createSession()).and().
                        header("Content-Type","application/json").
                        body(jsonObject.toJSONString()).
                when().
                        put("/rest/api/2/issue/10008/comment/10004").
                then().
                        statusCode(200).log().all().extract().response();
    }
}
