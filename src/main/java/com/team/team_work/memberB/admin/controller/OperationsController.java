package com.team.team_work.memberB.admin.controller;
import com.team.team_work.memberB.admin.dto.ApiResult;
import com.team.team_work.memberB.admin.entity.*;
import com.team.team_work.memberB.admin.service.*;
import com.team.team_work.memberB.product.entity.*;
import com.team.team_work.memberB.product.service.CatalogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.function.Supplier;
@RestController
@RequestMapping("/admin/b/api")
public class OperationsController {
 private final CatalogService catalog;private final OperationsService operations;private final CommandService commands;private final UploadService upload;
 public OperationsController(CatalogService catalog,OperationsService operations,CommandService commands,UploadService upload){this.catalog=catalog;this.operations=operations;this.commands=commands;this.upload=upload;}
 @GetMapping("/members")public ApiResult members(@RequestParam(defaultValue="")String q,@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="10")int size){return ApiResult.ok(operations.members(q,page,size));}
 public record MemberStatus(int locked){}
 @PostMapping("/members/{id}/status")public ApiResult member(@PathVariable long id,@RequestBody MemberStatus status,@RequestHeader("Idempotency-Key")String key,HttpSession s){return run(s,key,"member:"+id,status,()->operations.memberStatus(id,status.locked()));}
 @GetMapping("/carousels")public ApiResult carousels(){return ApiResult.ok(operations.carousels());}
 @PostMapping("/carousels")public ApiResult carousel(@RequestBody Carousel c,@RequestHeader("Idempotency-Key")String key,HttpSession s){return run(s,key,"carousel",c,()->operations.saveCarousel(c));}
 @DeleteMapping("/carousels/{id}")public ApiResult deleteCarousel(@PathVariable long id,@RequestHeader("Idempotency-Key")String key,HttpSession s){return run(s,key,"delete-carousel",id,()->operations.deleteCarousel(id));}
 @GetMapping("/configs")public ApiResult configs(){return ApiResult.ok(operations.configs());}
 @PostMapping("/configs")public ApiResult config(@RequestBody HomeConfig c,@RequestHeader("Idempotency-Key")String key,HttpSession s){return run(s,key,"config",c,()->operations.saveConfig(c));}
 @DeleteMapping("/configs/{id}")public ApiResult deleteConfig(@PathVariable long id,@RequestHeader("Idempotency-Key")String key,HttpSession s){return run(s,key,"delete-config",id,()->operations.deleteConfig(id));}
 @PostMapping("/upload")public ApiResult upload(@RequestParam("file")MultipartFile file)throws Exception{return ApiResult.ok(Map.of("url",upload.save(file)));}
 private ApiResult run(HttpSession s,String key,String operation,Object payload,Supplier<Object> action){return commands.run(AdminSession.current(s).id(),key,commands.intent(operation,payload),action);}
}
