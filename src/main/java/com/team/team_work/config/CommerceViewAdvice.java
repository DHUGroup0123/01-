package com.team.team_work.config;
import com.team.team_work.common.SessionKeys;
import jakarta.servlet.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.util.UUID;
@ControllerAdvice
public class CommerceViewAdvice {
 @ModelAttribute public void shared(HttpServletRequest request,Model model){HttpSession s=request.getSession();String csrf=(String)s.getAttribute(MemberASecurityFilter.CSRF);if(csrf==null){csrf=UUID.randomUUID().toString();s.setAttribute(MemberASecurityFilter.CSRF,csrf);}model.addAttribute("csrfToken",csrf);model.addAttribute("currentUser",s.getAttribute(SessionKeys.MALL_USER));}
}
