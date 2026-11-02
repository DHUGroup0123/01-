package com.team.team_work;

import com.team.team_work.common.SessionKeys;
import com.team.team_work.config.MemberASecurityFilter;
import com.team.team_work.memberA.user.dto.UserRequests;
import com.team.team_work.memberA.user.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(properties={
    "spring.datasource.url=${WEEK2_TEST_DB_URL:jdbc:h2:mem:mall_week2_tests;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1}",
    "spring.datasource.driver-class-name=${WEEK2_TEST_DB_DRIVER:org.h2.Driver}",
    "spring.datasource.username=${WEEK2_TEST_DB_USER:sa}",
    "spring.datasource.password=${WEEK2_TEST_DB_PASSWORD:}"
})
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class MemberAWeekTwoTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    @Autowired UserService users;
    @Autowired PasswordHasher passwords;
    private static final String PASSWORD="Passw0rd!2026";
    private final List<String> names=new ArrayList<>();
    private record Account(String name,Long id,MockHttpSession session) { }
    private String token(MockHttpSession session) throws Exception {
        mvc.perform(get("/api/csrf").session(session)).andExpect(status().isOk());
        return (String)session.getAttribute(MemberASecurityFilter.CSRF);
    }
    private String captcha(MockHttpSession session) throws Exception {
        mvc.perform(get("/api/captcha").session(session)).andExpect(status().isOk()).andExpect(content().contentType("image/png")).andExpect(header().string("Cache-Control","no-store"));
        return ((CaptchaService.Challenge)session.getAttribute(SessionKeys.CAPTCHA)).answer();
    }
    private JsonNode body(MvcResult result) throws Exception {return json.readTree(result.getResponse().getContentAsString());}
    private MockHttpServletRequestBuilder write(MockHttpServletRequestBuilder request,MockHttpSession session,Object data) throws Exception {
        request.session(session).header("X-CSRF-TOKEN",token(session));
        if(data!=null) request.contentType(APPLICATION_JSON).content(json.writeValueAsString(data));
        return request;
    }
    private Map<String,String> credentials(String name,String answer,String password) {return Map.of("loginName",name,"password",password,"captcha",answer);}
    private Account account() throws Exception {
        String name="w2_"+UUID.randomUUID().toString().replace("-","").substring(0,12);names.add(name);
        MockHttpSession session=new MockHttpSession();
        JsonNode profile=body(mvc.perform(write(post("/api/users/register"),session,credentials(name,captcha(session),PASSWORD))).andExpect(status().isOk()).andReturn()).get("data");
        mvc.perform(write(post("/api/users/login"),session,credentials(name,captcha(session),PASSWORD))).andExpect(status().isOk());
        return new Account(name,profile.get("userId").asLong(),session);
    }
    private Map<String,Object> address(String name,boolean isDefault) {return Map.of("receiverName",name,"receiverPhone","13800138000","province","上海市","city","上海市","district","松江区","detailAddress","演示路01号","isDefault",isDefault);}
    private String add(Account a,String receiver,boolean isDefault) throws Exception {
        return body(mvc.perform(write(post("/api/addresses"),a.session(),address(receiver,isDefault))).andExpect(status().isOk()).andReturn()).get("data").get("addressId").asText();
    }
    @AfterEach void cleanup() {
        for(String name:names) {
            jdbc.update("DELETE FROM user_address WHERE user_id IN (SELECT user_id FROM mall_user WHERE login_name=?)",name);
            jdbc.update("DELETE FROM mall_user WHERE login_name=?",name);
        }
    }

    @Test void registrationLoginSessionRotationAndLogout() throws Exception {
        String name="w2_"+UUID.randomUUID().toString().substring(0,8);names.add(name);
        MockHttpSession session=new MockHttpSession();String originalSessionId=session.getId();String oldToken=token(session);
        String registered=mvc.perform(write(post("/api/users/register"),session,credentials(name,captcha(session),PASSWORD))).andExpect(status().isOk()).andExpect(jsonPath("$.data.userId").isString()).andReturn().getResponse().getContentAsString();
        assertThat(registered).doesNotContain("passwordHash",PASSWORD);
        String encoded=jdbc.queryForObject("SELECT password_hash FROM mall_user WHERE login_name=?",String.class,name);
        assertThat(encoded).startsWith("pbkdf2-sha256$600000$").doesNotContain(PASSWORD);
        assertThat(passwords.matches(PASSWORD,encoded)).isTrue();
        mvc.perform(write(post("/api/users/register"),session,credentials(name.toUpperCase(Locale.ROOT),captcha(session),PASSWORD))).andExpect(status().isConflict());
        mvc.perform(write(post("/api/users/login"),session,credentials(name,captcha(session),PASSWORD))).andExpect(status().isOk());
        assertThat(session.getId()).isNotEqualTo(originalSessionId);
        assertThat(session.getAttribute(MemberASecurityFilter.CSRF)).isNotEqualTo(oldToken);
        mvc.perform(get("/api/users/me").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.data.loginName").value(name));
        mvc.perform(post("/api/users/logout").session(session).header("X-CSRF-TOKEN",oldToken)).andExpect(status().isForbidden());
        mvc.perform(write(post("/api/users/logout"),session,null)).andExpect(status().isOk());
        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }
    @Test void captchaRejectsWrongExpiredAndReusedValues() throws Exception {
        Account a=account();MockHttpSession s=a.session();String answer=captcha(s);
        mvc.perform(write(post("/api/users/login"),s,credentials(a.name(),"WRONG",PASSWORD))).andExpect(status().isBadRequest());
        mvc.perform(write(post("/api/users/login"),s,credentials(a.name(),answer,PASSWORD))).andExpect(status().isBadRequest());
        s.setAttribute(SessionKeys.CAPTCHA,new CaptchaService.Challenge("ABCDE",Instant.now().minusSeconds(1)));
        mvc.perform(write(post("/api/users/login"),s,credentials(a.name(),"ABCDE",PASSWORD))).andExpect(status().isBadRequest());
        String fresh=captcha(s);
        mvc.perform(write(post("/api/users/login"),s,credentials(a.name(),fresh,"WrongPassword123"))).andExpect(status().isUnauthorized());
        mvc.perform(write(post("/api/users/login"),s,credentials(a.name(),fresh,PASSWORD))).andExpect(status().isBadRequest());
    }
    @Test void authenticationAndCsrfAreRequiredForWritesAndPrivateRoutes() throws Exception {
        for(String route:List.of("/api/users/me","/api/addresses","/api/cart/items","/api/orders")) mvc.perform(get(route)).andExpect(status().isUnauthorized());
        for(String route:List.of("/personal","/personal/addresses","/cart","/checkout","/orders")) mvc.perform(get(route)).andExpect(status().isFound()).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/api/users/register").contentType(APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
        Account a=account();
        mvc.perform(put("/api/users/me").session(a.session()).contentType(APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
        mvc.perform(write(put("/api/users/me"),a.session(),Map.of("nickname","演示昵称","phone","123"))).andExpect(status().isBadRequest());
    }
    @Test void profileIsPersistedAndDisabledUserSessionIsRevoked() throws Exception {
        Account a=account();
        mvc.perform(write(put("/api/users/me"),a.session(),Map.of("nickname","新的昵称","phone","13900139000","userId","999999"))).andExpect(status().isOk()).andExpect(jsonPath("$.data.nickname").value("新的昵称"));
        assertThat(jdbc.queryForObject("SELECT nickname FROM mall_user WHERE user_id=?",String.class,a.id())).isEqualTo("新的昵称");
        jdbc.update("UPDATE mall_user SET status=1 WHERE user_id=?",a.id());
        mvc.perform(get("/api/users/me").session(a.session())).andExpect(status().isForbidden());
        assertThat(a.session().isInvalid()).isTrue();
    }
    @Test void addressCrudMaintainsExactlyOneDefault() throws Exception {
        Account a=account();String first=add(a,"第一位收货人",false),second=add(a,"第二位收货人",true);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM user_address WHERE user_id=? AND is_default=1",Integer.class,a.id())).isEqualTo(1);
        mvc.perform(write(put("/api/addresses/"+first),a.session(),address("更新的收货人",true))).andExpect(status().isOk()).andExpect(jsonPath("$.data.isDefault").value(true));
        mvc.perform(write(delete("/api/addresses/"+first),a.session(),null)).andExpect(status().isOk());
        mvc.perform(get("/api/addresses").session(a.session())).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1)).andExpect(jsonPath("$.data[0].addressId").value(second)).andExpect(jsonPath("$.data[0].isDefault").value(true));
        mvc.perform(write(delete("/api/addresses/"+second),a.session(),null)).andExpect(status().isOk());
        mvc.perform(get("/api/addresses").session(a.session())).andExpect(jsonPath("$.data.length()").value(0));
    }
    @Test void anotherUserCannotReadEditOrDeleteAnAddress() throws Exception {
        Account a=account(),b=account();String id=add(a,"仅属于A",true);
        mvc.perform(get("/api/addresses").session(b.session())).andExpect(jsonPath("$.data.length()").value(0));
        mvc.perform(write(put("/api/addresses/"+id),b.session(),address("越权修改",true))).andExpect(status().isNotFound());
        mvc.perform(write(delete("/api/addresses/"+id),b.session(),null)).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT receiver_name FROM user_address WHERE address_id=?",String.class,id)).isEqualTo("仅属于A");
    }
    @Test void concurrentAddressRequestsCannotCreateMultipleDefaults() throws Exception {
        Account a=account();
        ExecutorService pool=Executors.newFixedThreadPool(4);
        try {
            List<Future<?>> futures=new ArrayList<>();
            for(int i=0;i<8;i++){int index=i;futures.add(pool.submit(()->users.saveAddress(a.id(),null,new UserRequests.Address("并发"+index,"13800138000","上海市","上海市","松江区","并发测试地址",true))));}
            for(Future<?> future:futures) future.get(20,TimeUnit.SECONDS);
        } finally {pool.shutdownNow();}
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM user_address WHERE user_id=?",Integer.class,a.id())).isEqualTo(8);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM user_address WHERE user_id=? AND is_default=1",Integer.class,a.id())).isEqualTo(1);
    }
    @Test void addressLimitsAndRequiredFieldsAreEnforced() throws Exception {
        Account a=account();
        mvc.perform(write(post("/api/addresses"),a.session(),Map.of("receiverName",""))).andExpect(status().isBadRequest());
        for(int i=0;i<20;i++) users.saveAddress(a.id(),null,new UserRequests.Address("限额测试","13800138000","上海市","上海市","松江区","演示地址",false));
        mvc.perform(write(post("/api/addresses"),a.session(),address("第21条",true))).andExpect(status().isConflict());
    }
    @Test void searchSupportsKeywordAncestorCategorySortingAndPagination() throws Exception {
        JsonNode all=body(mvc.perform(get("/api/products").param("sort","priceAsc").param("pageSize","2")).andExpect(status().isOk()).andReturn()).get("data");
        assertThat(all.get("list").size()).isEqualTo(2);assertThat(all.get("total").asLong()).isGreaterThan(2);
        assertThat(all.get("list").get(0).get("sellingPrice").isTextual()).isTrue();
        long one=body(mvc.perform(get("/api/products").param("categoryId","1")).andReturn()).get("data").get("total").asLong();
        long three=body(mvc.perform(get("/api/products").param("categoryId","3")).andReturn()).get("data").get("total").asLong();
        assertThat(one).isEqualTo(three).isGreaterThan(0);
        mvc.perform(get("/api/products").param("keyword","台灯")).andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(3));
        mvc.perform(get("/api/products").param("page","9999")).andExpect(jsonPath("$.data.list.length()").value(0));
        mvc.perform(get("/api/categories")).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].children[0].children[0].categoryId").value("3"));
        mvc.perform(get("/api/categories").param("parentId","1")).andExpect(jsonPath("$.data[0].categoryId").value("2"));
        mvc.perform(get("/search").param("pageSize","6").param("page","2")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("下一页")));
    }
    @Test void maliciousSearchParametersAreRejectedOrTreatedLiterally() throws Exception {
        for(String[] param:new String[][]{{"sort","selling_price;DROP TABLE product"},{"page","0"},{"pageSize","51"},{"categoryId","-1"},{"keyword","a".repeat(51)},{"page","9999999999999999"}})
            mvc.perform(get("/api/products").param(param[0],param[1])).andExpect(status().isBadRequest());
        for(String keyword:List.of("' OR 1=1 --","%","_")) mvc.perform(get("/api/products").param("keyword",keyword)).andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(0));
    }
    @Test void hiddenProductsAndDeletedCategoryAncestorsAreNeverExposed() throws Exception {
        jdbc.update("UPDATE product SET sell_status=0 WHERE product_id=1");
        try {
            mvc.perform(get("/api/products/1")).andExpect(status().isNotFound());
            mvc.perform(get("/products/1")).andExpect(status().isNotFound());
            String home=mvc.perform(get("/api/home")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(home).doesNotContain("无线办公鼠标");
            jdbc.update("UPDATE category SET is_deleted=1 WHERE category_id=1");
            mvc.perform(get("/api/products")).andExpect(jsonPath("$.data.total").value(0));
            mvc.perform(get("/api/products/101")).andExpect(status().isNotFound());
        } finally {jdbc.update("UPDATE category SET is_deleted=0 WHERE category_id=1");jdbc.update("UPDATE product SET sell_status=1 WHERE product_id=1");}
    }
    @Test void productHtmlAndUrlsCannotExecuteScripts() throws Exception {
        jdbc.update("UPDATE product SET detail_html=?,cover_url=? WHERE product_id=1","<p>安全商品介绍</p><script>alert('xss')</script>","javascript:alert(1)");
        try {
            String detail=mvc.perform(get("/products/1")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(detail).contains("安全商品介绍").doesNotContain("alert('xss')","javascript:alert(1)");
            mvc.perform(get("/api/products/1")).andExpect(jsonPath("$.data.coverUrl").value("/images/01-product.svg"));
        } finally {jdbc.update("UPDATE product SET detail_html=NULL,cover_url=NULL WHERE product_id=1");}
        for(String route:List.of("/","/login","/register","/search","/products/101")) mvc.perform(get(route)).andExpect(status().isOk()).andExpect(header().string("X-Content-Type-Options","nosniff"));
    }
    @Test void passwordSaltsAreRandomAndMalformedHashesDoNotAuthenticate() {
        String first=passwords.hash(PASSWORD),second=passwords.hash(PASSWORD);
        assertThat(first).isNotEqualTo(second);assertThat(passwords.matches(PASSWORD,first)).isTrue();assertThat(passwords.matches("wrong",first)).isFalse();assertThat(passwords.matches(PASSWORD,"broken")).isFalse();
    }
    @Test void loginRateLimitIsBoundedAndIndependentByAccount() {
        LoginThrottle limiter=new LoginThrottle();for(int i=0;i<10;i++)limiter.attempt("127.0.0.1","membera");
        assertThatThrownBy(()->limiter.attempt("127.0.0.1","membera")).isInstanceOf(BusinessException.class).hasMessageContaining("尝试过多");
        assertThatCode(()->limiter.attempt("127.0.0.1","memberb")).doesNotThrowAnyException();
    }
}
