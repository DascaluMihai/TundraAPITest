import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.hamcrest.collection.IsMapContaining.hasKey;

public class TestApi {

    Random random = new Random();
    int randomUserId = random.nextInt(10) + 1;

    String regex = "^(.+)@(.+)$";
    Pattern pattern = Pattern.compile(regex);

    String url = "https://jsonplaceholder.typicode.com";
    String userPath = "/users/";
    String userPostPath = "/posts?userId=";

    Response userDetailsResponse =  RestAssured.get(url + userPath + randomUserId);
    Response userPostsResponse = RestAssured.get(url + userPostPath + randomUserId);

    @Test(priority = 1)
    public void getUserAddress() {
        Map<String, String> userAddress = userDetailsResponse.jsonPath().getMap("address");
        System.out.println("The address of user " + randomUserId + " is " + userAddress);
    }

    @Test(priority = 2)
    public void validateUserEmail() {
        JsonPath jsonPathEvaluator = userDetailsResponse.jsonPath();
        String email = jsonPathEvaluator.get("email");
        Matcher matcher = pattern.matcher(email);
        Assert.assertTrue(matcher.matches(), "The email address from the response has incorrect format");
    }

    @Test(priority = 3)
    public void getUserPosts() {

        int size = userPostsResponse.jsonPath().getList("$").size();

        for (int i = 0; i < size; i++) {
            given()
                    .when()
                    .get(url + userPostPath + randomUserId).
                    then()
                    .body("[" + i + "]", hasKey("id"));
        }

        for (int i = 0; i < size; i++) {
            given()
                    .when()
                    .get(url + userPostPath + randomUserId).
                    then()
                    .body("[" + i + "]", hasKey("title"));
        }

        for (int i = 0; i < size; i++) {
            given()
                    .when()
                    .get(url + userPostPath + randomUserId).
                    then()
                    .body("[" + i + "]", hasKey("body"));
        }
    }

    @Test (priority = 4)
    public void sendUserPost() {

        RestAssured.baseURI = url;

         String requestBody =
                 "{\n" +
                "  \"title\": \"testTitle03\",\n" +
                "  \"body\": \"testBody03\",\n" +
                "  \"userId\": " + "\""+ randomUserId + "\" \n}";

            Response response = given()
                    .header("Content-type", "application/json")
                    .and()
                    .body(requestBody)
                    .when()
                    .post("/posts")
                    .then()
                    .extract().response();

        System.out.println(response.getBody().asString());
    }

}