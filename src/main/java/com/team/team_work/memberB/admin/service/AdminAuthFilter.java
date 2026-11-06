package com.team.team_work.memberB.admin.service;
import com.team.team_work.memberB.admin.entity.AdminAccount;
import com.team.team_work.memberB.admin.mapper.AdminMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Component
public class AdminAuthFilter extends OncePerRequestFilter {
 private final AdminMapper mapper;
 public AdminAuthFilter(AdminMapper mapper){this.mapper=mapper;}
 private String path(HttpServletRequest req){String path=req.getServletPath();return path.isEmpty()?req.getRequestURI().substring(req.getContextPath().length()):path;}
 @Override protected boolean shouldNotFilter(HttpServletRequest req){String path=path(req);return !(path.equals("/admin/b") || path.startsWith("/admin/b/"));}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  res.setHeader("Cache-Control","no-store");res.setHeader("X-Content-Type-Options","nosniff");res.setHeader("X-Frame-Options","DENY");
  String path=path(req);
  boolean login=path.equals("/admin/b/login") || path.equals("/admin/b/captcha");
  HttpSession session=req.getSession(false);
  if(!login){
   AdminSession state=session==null?null:(AdminSession)session.getAttribute(AdminSession.KEY);
   AdminAccount account=state==null?null:mapper.byId(state.id());
   if(account==null || account.locked!=0 || account.credentialVersion!=state.credentialVersion()){
    if(session!=null)session.removeAttribute(AdminSession.KEY);
    if(path.startsWith("/admin/b/api/")){error(res,401,"请重新登录后台");}else res.sendRedirect(req.getContextPath()+"/admin/b/login");return;
   }
  }
  if(!req.getMethod().equals("GET") && !req.getMethod().equals("HEAD")){
   String expected=session==null?null:(String)session.getAttribute("memberB.csrf");
   String actual=req.getHeader("X-CSRF-Token");if(actual==null)actual=req.getParameter("_csrf");
   if(expected==null || actual==null || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),actual.getBytes(StandardCharsets.UTF_8))){error(res,403,"页面校验失败，请刷新后重试");return;}
  }
  chain.doFilter(req,res);
 }
 private void error(HttpServletResponse res,int status,String message)throws IOException{res.setStatus(status);res.setContentType("application/json;charset=UTF-8");res.getWriter().write("{\"resultCode\":"+status+",\"message\":\""+message+"\",\"data\":null}");}
}
