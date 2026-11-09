package com.team.team_work;

import cn.hutool.captcha.ShearCaptcha;
import com.team.team_work.common.SessionKeys;
import com.team.team_work.config.MemberASecurityFilter;
import com.team.team_work.memberA.user.service.CaptchaService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.*;
import java.util.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@EnabledIfEnvironmentVariable(named="RUN_MALL_TESTS",matches="true")
@SpringBootTest(properties={"spring.datasource.url=${SYSTEM_TEST_DB_URL}","spring.datasource.username=${SYSTEM_TEST_DB_USER:root}","spring.datasource.password=${SYSTEM_TEST_DB_PASSWORD:}"})
@AutoConfigureMockMvc
@Transactional
class FullSystemTests {
 @Autowired MockMvc mvc;
 @Autowired JdbcTemplate jdbc;
 @Autowired ObjectMapper json;
 private record User(Long id,MockHttpSession session) { }
 private String token(MockHttpSession s)throws Exception {mvc.perform(get("/api/csrf").session(s)).andExpect(status().isOk());return (String)s.getAttribute(MemberASecurityFilter.CSRF);}
 private JsonNode body(MvcResult r)throws Exception{return json.readTree(r.getResponse().getContentAsString());}
 private MockHttpServletRequestBuilder write(MockHttpServletRequestBuilder r,MockHttpSession s,Object value)throws Exception{r.session(s).header("X-CSRF-TOKEN",token(s));if(value!=null)r.contentType(APPLICATION_JSON).content(json.writeValueAsString(value));return r;}
 private String captcha(MockHttpSession s)throws Exception{mvc.perform(get("/api/captcha").session(s)).andExpect(status().isOk());return ((CaptchaService.Challenge)s.getAttribute(SessionKeys.CAPTCHA)).answer();}
 private User user()throws Exception{
  MockHttpSession s=new MockHttpSession();String phone="139"+String.format("%08d",Math.floorMod(UUID.randomUUID().getLeastSignificantBits(),100000000L));
  var data=new HashMap<String,Object>();data.put("loginName",phone);data.put("password","LocalDemo123!");data.put("captcha",captcha(s));
  Long id=body(mvc.perform(write(post("/api/users/register"),s,data)).andExpect(status().isOk()).andReturn()).get("data").get("userId").asLong();
  data.put("captcha",captcha(s));mvc.perform(write(post("/api/users/login"),s,data)).andExpect(status().isOk());return new User(id,s);
 }
 private MockHttpSession admin()throws Exception{
  MockHttpSession s=new MockHttpSession();String csrf=token(s);mvc.perform(get("/common/kaptcha").session(s)).andExpect(status().isOk());String code=((ShearCaptcha)s.getAttribute("verifyCode")).getCode();
  mvc.perform(post("/admin/login").session(s).param("_csrf",csrf).param("userName","admin").param("password","123456").param("verifyCode",code)).andExpect(status().is3xxRedirection());assertThat(s.getAttribute("loginUserId")).isNotNull();return s;
 }
 private Long goods(){return jdbc.queryForObject("SELECT g.goods_id FROM tb_mall_goods_info g JOIN tb_mall_goods_category c ON g.goods_category_id=c.category_id WHERE g.goods_sell_status=0 AND g.stock_num>=5 AND c.is_deleted=0 ORDER BY g.goods_id LIMIT 1",Long.class);}
 private String order(User u,int quantity)throws Exception{
  mvc.perform(write(post("/api/addresses"),u.session(),Map.of("address","课程测试收货人 13800138000 上海市松江区演示路01号"))).andExpect(status().isOk());
  mvc.perform(write(post("/shop-cart"),u.session(),Map.of("goodsId",goods(),"goodsCount",quantity))).andExpect(jsonPath("$.resultCode").value(200));
  mvc.perform(get("/shop-cart").session(u.session())).andExpect(status().isOk());mvc.perform(get("/shop-cart/settle").session(u.session())).andExpect(status().isOk());
  MvcResult result=mvc.perform(write(post("/saveOrder"),u.session(),null)).andExpect(status().is3xxRedirection()).andReturn();String url=result.getResponse().getRedirectedUrl();assertThat(url).startsWith("/orders/");return url.substring("/orders/".length());
 }
 @Test void schemaChecksumMatchesAndOnlyProjectTablesArePresent()throws Exception{
  byte[] bytes=new org.springframework.core.io.ClassPathResource("mall_schema.sql").getInputStream().readAllBytes();
  String expected=Files.readString(Path.of("database/schema.sha256")).split(" ")[0];assertThat(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))).isEqualTo(expected);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()",Integer.class)).isEqualTo(9);
 }
 @Test void storefrontAndLoginPagesRenderUsingTheExistingStyle()throws Exception{
  for(String route:List.of("/","/index","/search","/login","/register","/products/"+goods(),"/admin/login"))mvc.perform(get(route)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("01电商系统")));
  mvc.perform(get("/api/products").param("pageSize","2").param("sort","priceAsc")).andExpect(status().isOk()).andExpect(jsonPath("$.data.list.length()").value(2));
  mvc.perform(get("/api/products").param("sort","invalid")).andExpect(status().isBadRequest());
  mvc.perform(get("/api/environment")).andExpect(jsonPath("$.data.schemaVersion").value("MALL-01"));
 }
 @Test void memberAProfileAndSingleAddressArePersisted()throws Exception{
  User u=user();mvc.perform(write(put("/api/users/me"),u.session(),Map.of("nickname","本地测试用户","introduceSign","测试签名"))).andExpect(status().isOk());
  mvc.perform(write(post("/api/addresses"),u.session(),Map.of("address","完整测试收货地址"))).andExpect(status().isOk());
  mvc.perform(write(put("/api/addresses/"+u.id()),u.session(),Map.of("address","修改后的完整收货信息"))).andExpect(status().isOk());
  assertThat(jdbc.queryForObject("SELECT address FROM tb_mall_user WHERE user_id=?",String.class,u.id())).isEqualTo("修改后的完整收货信息");
  assertThat(jdbc.queryForObject("SELECT password_md5 FROM tb_mall_user WHERE user_id=?",String.class,u.id())).hasSize(32).isNotEqualTo("LocalDemo123!");
  mvc.perform(get("/personal").session(u.session())).andExpect(status().isOk());mvc.perform(get("/personal/addresses").session(u.session())).andExpect(status().isOk());
 }
 @Test void privateRoutesCsrfAndAddressOwnershipAreEnforced()throws Exception{
  mvc.perform(get("/shop-cart")).andExpect(status().is3xxRedirection());mvc.perform(post("/api/users/register").contentType(APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
  User a=user(),b=user();mvc.perform(write(post("/api/addresses"),a.session(),Map.of("address","只属于用户A"))).andExpect(status().isOk());
  mvc.perform(write(put("/api/addresses/"+a.id()),b.session(),Map.of("address","越权地址"))).andExpect(status().isNotFound());
  mvc.perform(write(delete("/api/addresses/"+a.id()),b.session(),null)).andExpect(status().isNotFound());
  mvc.perform(write(post("/api/addresses"),b.session(),Map.of("address","长".repeat(101)))).andExpect(status().isBadRequest());
 }
 @Test void fullTransactionFlowReachesCompletedWithoutRealPayment()throws Exception{
  User u=user();Long good=goods();int stock=jdbc.queryForObject("SELECT stock_num FROM tb_mall_goods_info WHERE goods_id=?",Integer.class,good);String no=order(u,2);
  mvc.perform(get("/orders/"+no).session(u.session())).andExpect(status().isOk());mvc.perform(get("/orders").session(u.session())).andExpect(status().isOk());
  mvc.perform(get("/selectPayType").session(u.session()).param("orderNo",no)).andExpect(status().isOk());mvc.perform(get("/payPage").session(u.session()).param("orderNo",no).param("payType","1")).andExpect(status().isOk());
  mvc.perform(write(post("/paySuccess").param("orderNo",no).param("payType","1"),u.session(),null)).andExpect(jsonPath("$.resultCode").value(200));
  Long id=jdbc.queryForObject("SELECT order_id FROM tb_mall_order WHERE order_no=?",Long.class,no);MockHttpSession admin=admin();
  mvc.perform(write(post("/admin/orders/checkOut"),admin,List.of(id))).andExpect(jsonPath("$.resultCode").value(org.hamcrest.Matchers.not(200)));
  mvc.perform(write(post("/admin/orders/checkDone"),admin,List.of(id))).andExpect(jsonPath("$.resultCode").value(200));
  mvc.perform(write(post("/admin/orders/checkOut"),admin,List.of(id))).andExpect(jsonPath("$.resultCode").value(200));
  mvc.perform(write(put("/orders/"+no+"/finish"),u.session(),null)).andExpect(jsonPath("$.resultCode").value(200));
  assertThat(jdbc.queryForObject("SELECT order_status FROM tb_mall_order WHERE order_no=?",Integer.class,no)).isEqualTo(4);assertThat(jdbc.queryForObject("SELECT stock_num FROM tb_mall_goods_info WHERE goods_id=?",Integer.class,good)).isEqualTo(stock-2);
 }
 @Test void orderCancelRestoresTheLastStockExactlyOnce()throws Exception{
  User u=user();Long id=goods();jdbc.update("UPDATE tb_mall_goods_info SET stock_num=2 WHERE goods_id=?",id);
  mvc.perform(write(post("/api/addresses"),u.session(),Map.of("address","取消订单测试地址"))).andExpect(status().isOk());
  mvc.perform(write(post("/shop-cart"),u.session(),Map.of("goodsId",id,"goodsCount",2))).andExpect(jsonPath("$.resultCode").value(200));
  var result=mvc.perform(write(post("/saveOrder"),u.session(),null)).andExpect(status().is3xxRedirection()).andReturn();String no=result.getResponse().getRedirectedUrl().substring(8);
  mvc.perform(write(put("/orders/"+no+"/cancel"),u.session(),null)).andExpect(jsonPath("$.resultCode").value(200));mvc.perform(write(put("/orders/"+no+"/cancel"),u.session(),null)).andExpect(jsonPath("$.resultCode").value(org.hamcrest.Matchers.not(200)));
  assertThat(jdbc.queryForObject("SELECT stock_num FROM tb_mall_goods_info WHERE goods_id=?",Integer.class,id)).isEqualTo(2);
 }
 @Test void anotherUserCannotPayOrViewAnOrder()throws Exception{
  User a=user();String no=order(a,1);User b=user();
  mvc.perform(write(post("/paySuccess").param("orderNo",no).param("payType","1"),b.session(),null)).andExpect(jsonPath("$.resultCode").value(org.hamcrest.Matchers.not(200)));
  assertThat(jdbc.queryForObject("SELECT pay_status FROM tb_mall_order WHERE order_no=?",Integer.class,no)).isZero();
  mvc.perform(get("/orders/"+no).session(b.session()).accept(APPLICATION_JSON)).andExpect(jsonPath("$.resultCode").value(org.hamcrest.Matchers.not(200)));
 }
 @Test void adminPagesAndDataEndpointsWorkWithProjectSchema()throws Exception{
  MockHttpSession admin=admin();
  for(String route:List.of("/admin","/admin/goods","/admin/goods/edit","/admin/users","/admin/orders","/admin/carousels","/admin/categories?categoryLevel=1&parentId=0&backParentId=0","/admin/indexConfigs?configType=3"))mvc.perform(get(route).session(admin)).andExpect(status().isOk());
  for(String route:List.of("/admin/goods/list","/admin/users/list","/admin/orders/list","/admin/carousels/list"))mvc.perform(get(route).session(admin).param("page","1").param("limit","10")).andExpect(jsonPath("$.resultCode").value(200));
  Long id=goods();mvc.perform(write(put("/admin/goods/status/1"),admin,List.of(id))).andExpect(jsonPath("$.resultCode").value(200));mvc.perform(get("/api/products/"+id)).andExpect(status().isNotFound());
 }
 @Test void adminImageUploadUsesTheConfiguredLocalDirectory()throws Exception{
  MockHttpSession admin=admin();ByteArrayOutputStream bytes=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(4,4,BufferedImage.TYPE_INT_RGB),"png",bytes);
  MockMultipartFile image=new MockMultipartFile("file","sample.png","image/png",bytes.toByteArray());String csrf=token(admin);
  JsonNode result=body(mvc.perform(multipart("/admin/upload/file").file(image).session(admin).param("_csrf",csrf)).andExpect(status().isOk()).andExpect(jsonPath("$.resultCode").value(200)).andReturn());String url=result.get("data").asText();assertThat(url).startsWith("/upload/").endsWith(".png");
  Path root=Path.of(com.team.team_work.common.Constants.FILE_UPLOAD_DIC).toAbsolutePath().normalize();Path created=root.resolve(url.substring("/upload/".length())).normalize();assertThat(created).startsWith(root);assertThat(Files.exists(created)).isTrue();Files.delete(created);
 }
 @Test void invalidCartQuantitiesAndCaptchaReplayAreRejected()throws Exception{
  User u=user();mvc.perform(write(post("/shop-cart"),u.session(),Map.of("goodsId",goods(),"goodsCount",0))).andExpect(jsonPath("$.resultCode").value(org.hamcrest.Matchers.not(200)));
  String answer=captcha(u.session());var data=Map.of("loginName","13900001111","password","wrongpass","captcha",answer);
  mvc.perform(write(post("/api/users/login"),u.session(),data)).andExpect(status().isUnauthorized());mvc.perform(write(post("/api/users/login"),u.session(),data)).andExpect(status().isBadRequest());
 }
}
