package com.team.team_work.memberB.admin.controller;
import com.team.team_work.memberB.admin.entity.AdminAccount;
import com.team.team_work.memberB.admin.mapper.AdminMapper;
import com.team.team_work.memberB.admin.dto.ApiResult;
import com.team.team_work.memberB.admin.service.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.*;
@Controller
@RequestMapping("/admin/b")
public class AuthController {
 private final AdminMapper mapper;private final CommandService commands;
 private final SecureRandom random=new SecureRandom();
 public AuthController(AdminMapper mapper,CommandService commands){this.mapper=mapper;this.commands=commands;}
 @GetMapping("/login") public String loginPage(HttpSession session,Model model){String token=UUID.randomUUID().toString();session.setAttribute("memberB.csrf",token);model.addAttribute("csrf",token);return "memberB/admin/login";}
 @GetMapping("/captcha") public void captcha(HttpSession session,HttpServletResponse response)throws Exception{
  String code=String.format("%04d",random.nextInt(10000));session.setAttribute("memberB.captcha",code);session.setAttribute("memberB.captchaTime",System.currentTimeMillis());
  BufferedImage image=new BufferedImage(140,44,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();g.setColor(new Color(236,242,248));g.fillRect(0,0,140,44);g.setFont(new Font("SansSerif",Font.BOLD,28));
  for(int i=0;i<4;i++){g.setColor(new Color(20+random.nextInt(100),30+random.nextInt(100),70+random.nextInt(100)));g.drawString(code.substring(i,i+1),12+i*31,32);}
  for(int i=0;i<12;i++){g.setColor(new Color(random.nextInt(0xffffff)));g.drawLine(random.nextInt(140),random.nextInt(44),random.nextInt(140),random.nextInt(44));}g.dispose();response.setContentType("image/png");ImageIO.write(image,"png",response.getOutputStream());
 }
 public record Login(String username,String password,String captcha){}
 @PostMapping("/login") @ResponseBody public ApiResult login(@RequestBody Login input,HttpServletRequest req){
  HttpSession session=req.getSession();
  synchronized(session){
   Long until=(Long)session.getAttribute("memberB.blockedUntil");if(until!=null && until>System.currentTimeMillis())throw new BusinessException(429,"登录失败次数过多，请稍后重试");
   String expected=(String)session.getAttribute("memberB.captcha");Long time=(Long)session.getAttribute("memberB.captchaTime");session.removeAttribute("memberB.captcha");
   if(expected==null || time==null || System.currentTimeMillis()-time>120000 || !expected.equals(input.captcha()))throw BusinessException.bad("验证码错误或已过期");
   AdminAccount account=input.username()==null?null:mapper.byLogin(input.username());
   if(account==null || account.locked!=0 || input.password()==null || input.password().length()>128 || !PasswordCodec.matches(input.password(),account.loginPassword)){
    Integer failures=(Integer)session.getAttribute("memberB.failures");int count=failures==null?1:failures+1;session.setAttribute("memberB.failures",count);if(count>=5){session.setAttribute("memberB.blockedUntil",System.currentTimeMillis()+60000);session.setAttribute("memberB.failures",0);}throw new BusinessException(401,"账号或密码错误，或账号已锁定");
   }
   req.changeSessionId();session.setMaxInactiveInterval(1800);session.setAttribute(AdminSession.KEY,new AdminSession(account.adminUserId,account.credentialVersion));session.removeAttribute("memberB.failures");return ApiResult.ok(Map.of("redirect","/admin/b"));
  }
 }
 @GetMapping({"","/"})public String dashboard(HttpSession session,Model model){AdminSession state=AdminSession.current(session);model.addAttribute("admin",mapper.byId(state.id()));model.addAttribute("csrf",session.getAttribute("memberB.csrf"));return "memberB/admin/dashboard";}
 @PostMapping("/api/logout")@ResponseBody public ApiResult logout(HttpSession session){session.invalidate();return ApiResult.ok(null);}
 @GetMapping("/api/profile")@ResponseBody public ApiResult profile(HttpSession session){return ApiResult.ok(mapper.byId(AdminSession.current(session).id()));}
 public record Profile(String nickName){}
 @PostMapping("/api/profile")@ResponseBody public ApiResult profile(@RequestBody Profile input,@RequestHeader("Idempotency-Key")String key,HttpSession session){long id=AdminSession.current(session).id();return commands.run(id,key,commands.intent("profile",input),()->{mapper.profile(id,com.team.team_work.memberB.product.service.CatalogService.text(input.nickName(),50,"昵称"));return mapper.byId(id);});}
 public record Password(String oldPassword,String newPassword){}
 @PostMapping("/api/password")@ResponseBody public ApiResult password(@RequestBody Password input,@RequestHeader("Idempotency-Key")String key,HttpSession session){
  long id=AdminSession.current(session).id();ApiResult result=commands.run(id,key,commands.intent("password",input),()->{AdminAccount account=mapper.byId(id);if(input.oldPassword()==null || !PasswordCodec.matches(input.oldPassword(),account.loginPassword))throw BusinessException.bad("原密码错误");mapper.password(id,PasswordCodec.encode(input.newPassword()));return Map.of("loginRequired",true);});session.removeAttribute(AdminSession.KEY);return result;
 }
}
