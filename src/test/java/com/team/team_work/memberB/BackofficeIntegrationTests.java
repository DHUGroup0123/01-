package com.team.team_work.memberB;

import com.team.team_work.memberB.admin.entity.*;
import com.team.team_work.memberB.admin.mapper.AdminMapper;
import com.team.team_work.memberB.admin.service.*;
import com.team.team_work.memberB.product.entity.*;
import com.team.team_work.memberB.product.mapper.CatalogMapper;
import com.team.team_work.memberB.product.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.http.MediaType;
import java.util.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class BackofficeIntegrationTests {
 @Autowired WebApplicationContext context;@Autowired AdminAuthFilter filter;@Autowired AdminMapper admin;
 @Autowired CatalogMapper mapper;@Autowired CatalogService catalog;@Autowired InventoryService inventory;@Autowired JsonMapper json;
 MockMvc mvc;MockHttpSession session;String csrf;
 @BeforeEach void setup(){mvc=MockMvcBuilders.webAppContextSetup(context).addFilters(filter).build();session=new MockHttpSession();csrf=UUID.randomUUID().toString();session.setAttribute("memberB.csrf",csrf);AdminAccount a=admin.byLogin("admin");session.setAttribute(AdminSession.KEY,new AdminSession(a.adminUserId,a.credentialVersion));}
 String key(){return UUID.randomUUID().toString();}
 MockHttpServletRequestBuilder postJson(String path,Object data,String key){return post("/admin/b/api/"+path).session(session).header("X-CSRF-Token",csrf).header("Idempotency-Key",key).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(data));}
 @SuppressWarnings("unchecked") Map<String,Object> data(MvcResult result)throws Exception{return (Map<String,Object>)json.readValue(result.getResponse().getContentAsString(),Map.class).get("data");}
 Product first(){return mapper.products("",null,null,0,10).get(0);}
 Category draft(){Category c=new Category();c.categoryName="新分类";c.parentId=0L;c.categoryRank=5;return c;}
 @Test void unauthenticatedPageRedirectsAndApiRejects()throws Exception{mvc.perform(get("/admin/b")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/b/login"));mvc.perform(get("/admin/b/api/products")).andExpect(status().isUnauthorized());}
 @Test void missingCsrfRejectsMutation()throws Exception{mvc.perform(post("/admin/b/api/categories").session(session).contentType(MediaType.APPLICATION_JSON).content("{}" )).andExpect(status().isForbidden());}
 @Test void csrfAlsoProtectsLogin()throws Exception{mvc.perform(post("/admin/b/login").contentType(MediaType.APPLICATION_JSON).content("{}" )).andExpect(status().isForbidden());}
 @Test void loginCaptchaAndPasswordWorkflow()throws Exception{
  session.removeAttribute(AdminSession.KEY);mvc.perform(get("/admin/b/login").session(session)).andExpect(status().isOk()).andExpect(view().name("memberB/admin/login"));csrf=(String)session.getAttribute("memberB.csrf");mvc.perform(get("/admin/b/captcha").session(session)).andExpect(status().isOk()).andExpect(content().contentType("image/png"));String captcha=(String)session.getAttribute("memberB.captcha");
  mvc.perform(post("/admin/b/login").session(session).header("X-CSRF-Token",csrf).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("username","admin","password","Test-only-memberB-2026","captcha",captcha)))).andExpect(status().isOk());assertNotNull(session.getAttribute(AdminSession.KEY));
 }
 @Test void wrongCaptchaDoesNotAuthenticate()throws Exception{session.removeAttribute(AdminSession.KEY);mvc.perform(post("/admin/b/login").session(session).header("X-CSRF-Token",csrf).contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"admin\",\"password\":\"bad\",\"captcha\":\"0000\"}")).andExpect(status().isBadRequest());assertNull(session.getAttribute(AdminSession.KEY));}
 @Test void dashboardRenders()throws Exception{mvc.perform(get("/admin/b").session(session)).andExpect(status().isOk()).andExpect(view().name("memberB/admin/dashboard"));}
 @Test void categoryCreationReplaysAndConflictingKeyRejects()throws Exception{
  String key=key();Category c=draft();String request=json.writeValueAsString(c);MvcResult one=mvc.perform(postJson("categories",c,key)).andExpect(status().isOk()).andReturn();long id=((Number)data(one).get("categoryId")).longValue();
  MvcResult two=mvc.perform(post("/admin/b/api/categories").session(session).header("X-CSRF-Token",csrf).header("Idempotency-Key",key).contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isOk()).andReturn();assertEquals(id,((Number)data(two).get("categoryId")).longValue());
  c.categoryName="不同内容";mvc.perform(postJson("categories",c,key)).andExpect(status().isConflict());
 }
 @Test void categoryCannotDeleteParentOrProductAssociation()throws Exception{Category root=mapper.categories().stream().filter(c->c.categoryLevel==1).findFirst().orElseThrow();mvc.perform(delete("/admin/b/api/categories/"+root.categoryId).session(session).header("X-CSRF-Token",csrf).header("Idempotency-Key",key())).andExpect(status().isConflict());assertThrows(BusinessException.class,()->catalog.deleteCategory(first().goodsCategoryId));}
 @Test void categoryCannotMoveOrExceedThreeLevels(){Category leaf=mapper.categories().stream().filter(c->c.categoryLevel==3).findFirst().orElseThrow();Category c=draft();c.parentId=leaf.categoryId;assertThrows(BusinessException.class,()->catalog.saveCategory(c));leaf.parentId=0L;assertThrows(BusinessException.class,()->catalog.saveCategory(leaf));}
 @Test void categoryRenameAndDeleteEmptyCategory()throws Exception{Category c=catalog.saveCategory(draft());c.categoryName="新名称";mvc.perform(postJson("categories",c,key())).andExpect(status().isOk());assertEquals("新名称",catalog.category(c.categoryId).categoryName);String key=key();mvc.perform(delete("/admin/b/api/categories/"+c.categoryId).session(session).header("X-CSRF-Token",csrf).header("Idempotency-Key",key)).andExpect(status().isOk());mvc.perform(delete("/admin/b/api/categories/"+c.categoryId).session(session).header("X-CSRF-Token",csrf).header("Idempotency-Key",key)).andExpect(status().isOk());}
 @Test void productCreationDefaultsOffShelfAndCanGoOnline()throws Exception{Product p=first();p.goodsId=null;p.goodsName="新增测试商品";p.goodsSellStatus=0;MvcResult result=mvc.perform(postJson("products",p,key())).andExpect(status().isOk()).andReturn();long id=((Number)data(result).get("goodsId")).longValue();assertEquals(1,catalog.product(id).goodsSellStatus);mvc.perform(postJson("products/"+id+"/status",Map.of("status",0,"version",0),key())).andExpect(status().isOk());assertEquals(0,catalog.product(id).goodsSellStatus);}
 @Test void productPricesAndCategoryAreValidated()throws Exception{Product p=first();p.sellingPrice=p.originalPrice.add(java.math.BigDecimal.ONE);mvc.perform(postJson("products",p,key())).andExpect(status().isBadRequest());p.sellingPrice=java.math.BigDecimal.ONE;p.goodsCategoryId=mapper.categories().get(0).categoryId;mvc.perform(postJson("products",p,key())).andExpect(status().isBadRequest());}
 @Test void staleProductEditCannotOverwriteStock()throws Exception{Product p=first();int old=p.stockNum;inventory.reserve("stale-edit",p.goodsId,1);p.stockNum=old+100;mvc.perform(postJson("products",p,key())).andExpect(status().isConflict());assertEquals(old-1,catalog.product(p.goodsId).stockNum);}
 @Test void homepageReferenceProtectsOfflineProduct()throws Exception{HomeConfig config=admin.configs().get(0);Product p=catalog.product(config.goodsId);mvc.perform(postJson("products/"+p.goodsId+"/status",Map.of("status",1,"version",p.version),key())).andExpect(status().isConflict());}
 @Test void productSearchAndPagination()throws Exception{mvc.perform(get("/admin/b/api/products").session(session).param("q","键盘").param("page","1").param("size","1")).andExpect(status().isOk());mvc.perform(get("/admin/b/api/products").session(session).param("size","10000")).andExpect(status().isBadRequest());}
 @Test void carouselCreateEditAndDelete()throws Exception{Carousel c=new Carousel();c.carouselUrl=first().goodsCoverImg;c.carouselRank=50;c.redirectUrl="/";c.isDeleted=0;MvcResult result=mvc.perform(postJson("carousels",c,key())).andExpect(status().isOk()).andReturn();c.carouselId=((Number)data(result).get("carouselId")).longValue();c.carouselRank=99;c.isDeleted=1;mvc.perform(postJson("carousels",c,key())).andExpect(status().isOk());assertEquals(99,admin.carousel(c.carouselId).carouselRank);mvc.perform(delete("/admin/b/api/carousels/"+c.carouselId).session(session).header("X-CSRF-Token",csrf).header("Idempotency-Key",key())).andExpect(status().isOk());assertNull(admin.carousel(c.carouselId));}
 @Test void externalRedirectAndInvalidImageReject()throws Exception{Carousel c=new Carousel();c.carouselUrl=first().goodsCoverImg;c.carouselRank=0;c.isDeleted=0;c.redirectUrl="javascript:alert(1)";mvc.perform(postJson("carousels",c,key())).andExpect(status().isBadRequest());c.redirectUrl="/";c.carouselUrl="/uploads/test.svg";mvc.perform(postJson("carousels",c,key())).andExpect(status().isBadRequest());}
 @Test void homepageOnlyAcceptsOnlineStockedProducts()throws Exception{Product p=first();p.goodsId=null;p.goodsName="下架配置测试";p=catalog.saveProduct(p);HomeConfig c=new HomeConfig();c.configName="配置";c.configType=3;c.goodsId=p.goodsId;c.configRank=1;c.isDeleted=0;mvc.perform(postJson("configs",c,key())).andExpect(status().isBadRequest());}
 @Test void homepageConfigEditAndDelete()throws Exception{HomeConfig c=admin.configs().get(0);c.configName="已修改配置";c.isDeleted=1;mvc.perform(postJson("configs",c,key())).andExpect(status().isOk());assertEquals("已修改配置",admin.config(c.configId).configName);mvc.perform(delete("/admin/b/api/configs/"+c.configId).session(session).header("X-CSRF-Token",csrf).header("Idempotency-Key",key())).andExpect(status().isOk());}
 @Test void memberDisableAndEnable()throws Exception{Member member=admin.members("demo_member_b",0,10).get(0);mvc.perform(postJson("members/"+member.userId+"/status",Map.of("locked",1),key())).andExpect(status().isOk());assertEquals(1,admin.member(member.userId).lockedFlag);mvc.perform(postJson("members/"+member.userId+"/status",Map.of("locked",0),key())).andExpect(status().isOk());assertEquals(0,admin.member(member.userId).lockedFlag);}
 @Test void profileNeverExposesPasswordAndPasswordChangeExpiresSession()throws Exception{MvcResult result=mvc.perform(get("/admin/b/api/profile").session(session)).andExpect(status().isOk()).andReturn();assertFalse(result.getResponse().getContentAsString().contains("pbkdf2"));mvc.perform(postJson("profile",Map.of("nickName","新管理员"),key())).andExpect(status().isOk());mvc.perform(postJson("password",Map.of("oldPassword","Test-only-memberB-2026","newPassword","Changed-test-password-2026"),key())).andExpect(status().isOk());mvc.perform(get("/admin/b/api/profile").session(session)).andExpect(status().isUnauthorized());assertTrue(PasswordCodec.matches("Changed-test-password-2026",admin.byLogin("admin").loginPassword));}
 @Test void uploadedImagesAreDeduplicatedAndDisguisedFilesReject()throws Exception{BufferedImage image=new BufferedImage(2,2,BufferedImage.TYPE_INT_RGB);ByteArrayOutputStream out=new ByteArrayOutputStream();ImageIO.write(image,"png",out);MockMultipartFile file=new MockMultipartFile("file","../../evil.png","image/png",out.toByteArray());MvcResult a=mvc.perform(multipart("/admin/b/api/upload").file(file).session(session).header("X-CSRF-Token",csrf)).andExpect(status().isOk()).andReturn();MvcResult b=mvc.perform(multipart("/admin/b/api/upload").file(file).session(session).header("X-CSRF-Token",csrf)).andExpect(status().isOk()).andReturn();assertEquals(data(a).get("url"),data(b).get("url"));mvc.perform(multipart("/admin/b/api/upload").file(new MockMultipartFile("file","fake.png","image/png","<script/>".getBytes())).session(session).header("X-CSRF-Token",csrf)).andExpect(status().isBadRequest());}
 @Test void stockReservationAndReleaseAreIdempotentAndCannotOversell(){Product p=first();int stock=p.stockNum;inventory.reserve("order-line-001",p.goodsId,2);inventory.reserve("order-line-001",p.goodsId,2);assertEquals(stock-2,catalog.product(p.goodsId).stockNum);assertThrows(BusinessException.class,()->inventory.reserve("other-line",p.goodsId,stock));assertThrows(BusinessException.class,()->inventory.reserve("order-line-001",p.goodsId,3));inventory.release("order-line-001",p.goodsId,2);inventory.release("order-line-001",p.goodsId,2);assertEquals(stock,catalog.product(p.goodsId).stockNum);}
}
