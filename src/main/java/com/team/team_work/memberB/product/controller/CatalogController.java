package com.team.team_work.memberB.product.controller;
import com.team.team_work.memberB.admin.dto.ApiResult;
import com.team.team_work.memberB.admin.service.*;
import com.team.team_work.memberB.product.entity.*;
import com.team.team_work.memberB.product.service.CatalogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import java.util.function.Supplier;
@RestController
@RequestMapping("/admin/b/api")
public class CatalogController {
 private final CatalogService catalog;private final CommandService commands;
 public CatalogController(CatalogService catalog,CommandService commands){this.catalog=catalog;this.commands=commands;}
 @GetMapping("/categories")public ApiResult categories(){return ApiResult.ok(catalog.tree());}
 @PostMapping("/categories")public ApiResult category(@RequestBody Category c,@RequestHeader("Idempotency-Key")String key,HttpSession s){return run(s,key,"category",c,()->catalog.saveCategory(c));}
 @DeleteMapping("/categories/{id}")public ApiResult deleteCategory(@PathVariable long id,@RequestHeader("Idempotency-Key")String key,HttpSession s){return run(s,key,"delete-category",id,()->catalog.deleteCategory(id));}
 @GetMapping("/products")public ApiResult products(@RequestParam(defaultValue="")String q,@RequestParam(required=false)Long categoryId,@RequestParam(required=false)Integer status,@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="10")int size){return ApiResult.ok(catalog.products(q,categoryId,status,page,size));}
 @GetMapping("/products/{id}")public ApiResult product(@PathVariable long id){return ApiResult.ok(catalog.product(id));}
 @PostMapping("/products")public ApiResult product(@RequestBody Product p,@RequestHeader("Idempotency-Key")String key,HttpSession s){return run(s,key,"product",p,()->catalog.saveProduct(p));}
 public record Status(int status,int version){}
 @PostMapping("/products/{id}/status")public ApiResult status(@PathVariable long id,@RequestBody Status status,@RequestHeader("Idempotency-Key")String key,HttpSession s){return run(s,key,"product-status:"+id,status,()->catalog.status(id,status.status(),status.version()));}
 private ApiResult run(HttpSession s,String key,String operation,Object payload,Supplier<Object> action){return commands.run(AdminSession.current(s).id(),key,commands.intent(operation,payload),action);}
}
