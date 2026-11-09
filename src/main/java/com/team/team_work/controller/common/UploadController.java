// Modified 2026-11-05: Java 17 upload handling.
/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package com.team.team_work.controller.common;
import com.team.team_work.common.Constants;
import com.team.team_work.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
@Controller @RequestMapping("/admin")
public class UploadController {
 private String save(MultipartFile file)throws IOException{
  if(file==null||file.isEmpty()||file.getSize()>5*1024*1024)throw new IOException("图片大小需在5MB以内");
  var image=ImageIO.read(file.getInputStream());if(image==null||image.getWidth()>4096||image.getHeight()>4096)throw new IOException("请上传尺寸不超过4096像素的有效图片");
  Path root=Paths.get(Constants.FILE_UPLOAD_DIC);Files.createDirectories(root);String name=UUID.randomUUID()+".png";Path target=root.resolve(name).normalize();
  if(!target.startsWith(root))throw new IOException("路径不正确");ImageIO.write(image,"png",target.toFile());return "/upload/"+name;
 }
 @PostMapping("/upload/file") @ResponseBody public Result upload(@RequestParam("file")MultipartFile file){try{Result r=ResultGenerator.genSuccessResult();r.setData(save(file));return r;}catch(IOException e){return ResultGenerator.genFailResult(e.getMessage());}}
 @PostMapping("/upload/files") @ResponseBody public Result uploads(@RequestParam("files")MultipartFile[] files){if(files.length<1||files.length>5)return ResultGenerator.genFailResult("请选择1至5张图片");try{List<String> paths=new ArrayList<>();for(var file:files)paths.add(save(file));Result r=ResultGenerator.genSuccessResult();r.setData(paths);return r;}catch(IOException e){return ResultGenerator.genFailResult(e.getMessage());}}
}
