package com.team.team_work.memberA.mall.controller;
import com.team.team_work.common.ApiResponse;
import com.team.team_work.memberA.mall.dto.*;
import com.team.team_work.memberA.mall.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.*;
import com.team.team_work.common.SessionKeys;

@Controller
public class MallController {
    private final CatalogService catalog;
    private final EnvironmentService environment;
    public MallController(CatalogService catalog,EnvironmentService environment) {this.catalog=catalog;this.environment=environment;}
    @ModelAttribute public void identity(HttpServletRequest request,Model model) {
        HttpSession session=request.getSession(false);
        model.addAttribute("currentUser",session==null?null:session.getAttribute(SessionKeys.MALL_USER));
    }
    @GetMapping({"/","/index"}) public String home(Model model) {model.addAttribute("home",catalog.home());model.addAttribute("environment",environment.inspect());return "memberA/mall/index";}
    @GetMapping("/search") public String search(@RequestParam(defaultValue="") String keyword,@RequestParam(required=false) Long categoryId,@RequestParam(defaultValue="latest") String sort,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="12") int pageSize,Model model) {
        CatalogQuery q=catalog.query(keyword,categoryId,sort,page,pageSize);model.addAttribute("query",q);model.addAttribute("result",catalog.search(q));model.addAttribute("categories",catalog.categories(null));return "memberA/mall/search";
    }
    @GetMapping("/products/{id}") public String detail(@PathVariable Long id,Model model) {model.addAttribute("product",catalog.product(id));return "memberA/mall/detail";}
    @GetMapping("/login") public String login() {return "memberA/mall/login";}
    @GetMapping("/register") public String register() {return "memberA/mall/register";}
    @GetMapping("/personal") public String personal() {return "memberA/mall/personal";}
    @GetMapping("/personal/addresses") public String addresses() {return "memberA/mall/addresses";}
    @GetMapping("/api/categories") @ResponseBody public ApiResponse<?> categories(@RequestParam(required=false) Long parentId) {return ApiResponse.success(catalog.categories(parentId));}
    @GetMapping("/api/home") @ResponseBody public ApiResponse<?> home() {return ApiResponse.success(catalog.home());}
    @GetMapping("/api/products") @ResponseBody public ApiResponse<?> products(@RequestParam(defaultValue="") String keyword,@RequestParam(required=false) Long categoryId,@RequestParam(defaultValue="latest") String sort,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="12") int pageSize) {return ApiResponse.success(catalog.search(catalog.query(keyword,categoryId,sort,page,pageSize)));}
    @GetMapping("/api/products/{id}") @ResponseBody public ApiResponse<?> product(@PathVariable Long id) {return ApiResponse.success(catalog.product(id));}
}
