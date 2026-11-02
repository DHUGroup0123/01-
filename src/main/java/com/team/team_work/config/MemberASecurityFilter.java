package com.team.team_work.config;
import com.team.team_work.common.SessionKeys;
import com.team.team_work.memberA.user.dto.SessionUser;
import com.team.team_work.memberA.user.service.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.dao.DataAccessException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@Order(10)
public class MemberASecurityFilter extends OncePerRequestFilter {
    public static final String CSRF="mallCsrf";
    private final UserService users;
    public MemberASecurityFilter(UserService users) {this.users=users;}
    private boolean at(String path,String root) {return path.equals(root)||path.startsWith(root+"/");}
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        String path=request.getRequestURI().substring(request.getContextPath().length());
        response.setHeader("X-Content-Type-Options","nosniff");
        response.setHeader("Referrer-Policy","same-origin");
        response.setHeader("Content-Security-Policy","default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; object-src 'none'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'");
        boolean userApi=at(path,"/api/users/me")||path.equals("/api/users/logout")||at(path,"/api/addresses")||at(path,"/api/cart")||at(path,"/api/orders");
        boolean page=at(path,"/personal")||at(path,"/cart")||at(path,"/orders")||at(path,"/order")||at(path,"/checkout");
        if(userApi||page) {
            HttpSession session=request.getSession(false);
            SessionUser user=session==null?null:(SessionUser)session.getAttribute(SessionKeys.MALL_USER);
            if(user==null) {
                if(page) response.sendRedirect(request.getContextPath()+"/login"); else failure(response,401,"请先登录");
                return;
            }
            try {users.active(user.userId());}
            catch(BusinessException ex) {session.invalidate();if(page) response.sendRedirect(request.getContextPath()+"/login");else failure(response,ex.getStatus(),ex.getMessage());return;}
            catch(DataAccessException ex) {failure(response,503,"数据库暂不可用，请稍后重试");return;}
            response.setHeader("Cache-Control","no-store");
        }
        boolean write=!request.getMethod().equals("GET")&&!request.getMethod().equals("HEAD")&&!request.getMethod().equals("OPTIONS");
        boolean memberApi=at(path,"/api/users")||at(path,"/api/addresses")||at(path,"/api/cart")||at(path,"/api/orders");
        if(write&&memberApi) {
            HttpSession session=request.getSession(false);
            String expected=session==null?null:(String)session.getAttribute(CSRF),actual=request.getHeader("X-CSRF-TOKEN");
            if(expected==null||actual==null||!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),actual.getBytes(StandardCharsets.UTF_8))) {failure(response,403,"页面凭证已失效，请刷新后重试");return;}
        }
        chain.doFilter(request,response);
    }
    private void failure(HttpServletResponse response,int code,String message) throws IOException {
        response.setStatus(code);response.setContentType("application/json;charset=UTF-8");response.setHeader("Cache-Control","no-store");
        response.getWriter().write("{\"code\":"+code+",\"message\":\""+message+"\",\"data\":null}");
    }
}
