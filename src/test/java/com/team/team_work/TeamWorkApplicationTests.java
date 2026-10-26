package com.team.team_work;

import com.team.team_work.memberA.mall.mapper.EnvironmentMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("demo")
class TeamWorkApplicationTests {
    @LocalServerPort int port;
    @Autowired EnvironmentMapper mapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;
    private HttpResponse<String> get(String path) throws Exception {
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(
            URI.create("http://localhost:" + port + path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    @Test void frontAndAdminPagesRenderRealDatabaseState() throws Exception {
        HttpResponse<String> front = get("/");
        assertThat(front.statusCode()).isEqualTo(200);
        assertThat(front.body()).contains("01电商系统", "无线办公鼠标", "89.00", "H2 临时演示");
        for (String path : new String[]{"/index", "/admin", "/admin/", "/admin/login"}) {
            HttpResponse<String> result = get(path);
            assertThat(result.statusCode()).as(path).isEqualTo(200);
            assertThat(result.body()).contains("01电商系统");
        }
    }
    @Test void environmentEndpointAndExistingDiagnosticWork() throws Exception {
        HttpResponse<String> result = get("/api/environment");
        assertThat(result.statusCode()).isEqualTo(200);
        assertThat(result.body()).contains("\"code\":200", "01电商系统", "V001", "4.0.8");
        assertThat(result.body()).doesNotContain("password", "jdbc:", "DB_PASSWORD");
        assertThat(get("/test-db").body()).contains("\"data\":1");
    }
    @Test void accountEntryDoesNotPretendAuthenticationIsImplemented() throws Exception {
        for (String path : new String[]{"/login", "/register"}) {
            assertThat(get(path).body()).contains("尚未开放注册和登录");
        }
        assertThat(get("/css/01-mall.css").statusCode()).isEqualTo(200);
    }
    @Test void initializationCanRunTwiceWithoutOverwritingRows() {
        jdbc.update("UPDATE product SET summary = ? WHERE product_id = 1", "保留既有修改");
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("db/V001__schema.sql"), new ClassPathResource("db/V001__seed.sql"));
            populator.setSqlScriptEncoding("UTF-8");
            populator.execute(dataSource);
            populator.execute(dataSource);
            assertThat(mapper.countProducts()).isEqualTo(3);
            assertThat(mapper.countCategories()).isEqualTo(3);
            assertThat(jdbc.queryForObject("SELECT summary FROM product WHERE product_id=1", String.class)).isEqualTo("保留既有修改");
        } finally {
            jdbc.update("UPDATE product SET summary = ? WHERE product_id = 1", "轻巧便携，用于商品字段与价格展示验证");
        }
    }
    @Test void unpublishedProductsAreNotReturnedToTheMall() {
        jdbc.update("UPDATE product SET sell_status=0 WHERE product_id=3");
        try {
            assertThat(mapper.findPreviewProducts()).extracting("productId").containsExactly(1L, 2L);
        } finally { jdbc.update("UPDATE product SET sell_status=1 WHERE product_id=3"); }
    }
    @Test void schemaRejectsInvalidInventoryAndOrphanProducts() {
        assertThatThrownBy(() -> jdbc.update("UPDATE product SET stock=-1 WHERE product_id=1"))
            .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("UPDATE product SET category_id=999999 WHERE product_id=1"))
            .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThat(mapper.schemaVersion()).isEqualTo("V001");
    }
}
