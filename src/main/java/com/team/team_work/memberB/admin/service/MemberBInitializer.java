package com.team.team_work.memberB.admin.service;
import com.team.team_work.memberB.admin.entity.*;
import com.team.team_work.memberB.admin.mapper.AdminMapper;
import com.team.team_work.memberB.product.entity.*;
import com.team.team_work.memberB.product.mapper.CatalogMapper;
import com.team.team_work.memberB.product.service.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
@Component
public class MemberBInitializer implements ApplicationRunner {
 private final Environment env;private final DataSource source;private final AdminMapper admin;private final CatalogMapper mapper;private final CatalogService catalog;private final OperationsService operations;private final TransactionTemplate tx;
 public MemberBInitializer(Environment env,DataSource source,AdminMapper admin,CatalogMapper mapper,CatalogService catalog,OperationsService operations,PlatformTransactionManager manager){this.env=env;this.source=source;this.admin=admin;this.mapper=mapper;this.catalog=catalog;this.operations=operations;tx=new TransactionTemplate(manager);}
 @Override public void run(ApplicationArguments args)throws Exception{
  if(!env.getProperty("memberb.initialize",Boolean.class,false))return;
  new ResourceDatabasePopulator(new ClassPathResource("memberB/schema.sql")).execute(source);
  String password=env.getProperty("memberb.admin-password","");
  String username=env.getProperty("memberb.admin-name","admin");
  if(admin.byLogin(username)==null){
   if(password.isBlank())throw new IllegalStateException("首次初始化须设置 MEMBERB_ADMIN_PASSWORD（12至128字符），没有默认管理员密码");
   tx.executeWithoutResult(status->{admin.guard();if(admin.byLogin(username)==null){AdminAccount a=new AdminAccount();a.loginUserName=username;a.nickName="成员B管理员";a.loginPassword=PasswordCodec.encode(password);admin.insertAdmin(a);}});
  }
  if(env.getProperty("memberb.seed",Boolean.class,false))seed();
 }
 private void seed()throws Exception{
  String image=demoImage();
  tx.executeWithoutResult(status->{admin.guard();if(!mapper.categories().isEmpty())return;
   Category c1=new Category();c1.categoryName="数码生活";c1.parentId=0L;c1.categoryRank=100;catalog.saveCategory(c1);
   Category c2=new Category();c2.categoryName="桌面配件";c2.parentId=c1.categoryId;c2.categoryRank=100;catalog.saveCategory(c2);
   Category c3=new Category();c3.categoryName="精选好物";c3.parentId=c2.categoryId;c3.categoryRank=100;catalog.saveCategory(c3);
   String[] names={"无线办公键盘","轻量便携鼠标","桌面收纳支架","旅行充电套装"};
   for(int i=0;i<names.length;i++){Product p=new Product();p.goodsName=names[i];p.goodsIntro="0123组统一演示商品 "+(i+1);p.goodsCategoryId=c3.categoryId;p.goodsCoverImg=image;p.originalPrice=BigDecimal.valueOf(199+i*50);p.sellingPrice=BigDecimal.valueOf(129+i*40);p.stockNum=30+i*10;p.tag="课程演示";p.goodsDetailContent="用于展示后台发布、上下架与库存联调的测试商品，不接入真实支付。";catalog.saveProduct(p);catalog.status(p.goodsId,0,0);
    if(i<3){HomeConfig h=new HomeConfig();h.configName=new String[]{"热销精选","新品上线","为你推荐"}[i];h.configType=3+i;h.configRank=100-i;h.goodsId=p.goodsId;h.isDeleted=0;operations.saveConfig(h);}
   }
   Carousel c=new Carousel();c.carouselUrl=image;c.redirectUrl="/";c.carouselRank=100;c.isDeleted=0;operations.saveCarousel(c);
   new JdbcTemplate(source).update("INSERT INTO tb_newbee_mall_user(nick_name,login_name,locked_flag) VALUES(?,?,0)","演示会员","demo_member_b");
  });
 }
 private String demoImage()throws Exception{
  BufferedImage image=new BufferedImage(900,560,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();g.setColor(new Color(223,235,240));g.fillRect(0,0,900,560);g.setColor(new Color(28,67,78));g.fillRoundRect(190,160,520,240,30,30);g.setColor(new Color(245,247,245));for(int y=0;y<4;y++)for(int x=0;x<12;x++)g.fillRoundRect(210+x*40,180+y*46,30,30,5,5);g.setFont(new Font("SansSerif",Font.BOLD,26));g.setColor(new Color(28,67,78));g.drawString("0123 / DESK COLLECTION",235,110);g.dispose();ByteArrayOutputStream out=new ByteArrayOutputStream();ImageIO.write(image,"png",out);byte[] bytes=out.toByteArray();String name=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))+".png";Path root=Path.of(env.getProperty("memberb.upload-dir","./member-b-uploads")).toAbsolutePath();Files.createDirectories(root);try{Files.write(root.resolve(name),bytes,StandardOpenOption.CREATE_NEW);}catch(FileAlreadyExistsException ignored){}return "/member-b/media/"+name;
 }
}
