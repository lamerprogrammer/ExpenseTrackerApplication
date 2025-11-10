package simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class ExpenseTrackerSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080/api")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder scn = scenario("Register -> Login -> Total -> Report (date range) -> Report (monthly)")
            .exec(session -> session
                    .set("randomEmail", "load-" + UUID.randomUUID() + "@example.com")
                    .set("randomName", "user-" + System.currentTimeMillis())
                    .set("dateFrom", "2025-11-01T00:00:00Z")
                    .set("dateTo", "2025-11-02T23:59:59Z")
                    .set("month", "NOVEMBER")
                    .set("year", 2025))
            .exec(http("registerUser")
                    .post("/auth/register")
                    .body(StringBody("{ \"email\": \"#{randomEmail}\", \"password\": \"123456\", \"name\": \"#{randomName}\" }"))
                    .check(status().is(201)))
            .exec(http("loginUser")
                    .post("/auth/login")
                    .body(StringBody("{ \"email\": \"#{randomEmail}\", \"password\": \"123456\" }"))
                    .check(status().is(200))
                    .check(jsonPath("$.data.accessToken").saveAs("jwtToken")))
            .exitHereIfFailed()
            .exec(http("getTotal")
                    .get("/expenses/total")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))
            .exec(http("getReport")
                    .get("/expenses/report?from=#{dateFrom}&to=#{dateTo}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))
            .exec(http("reportMonthly")
                    .get("/expenses/stats/monthly?month=#{month}&year=#{year}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))
            .pause(1, 3);

    {
        setUp(scn.injectOpen(rampUsers(200).during(60))).protocols(httpProtocol);
    }
}
