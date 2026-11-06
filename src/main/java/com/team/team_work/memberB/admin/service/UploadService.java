package com.team.team_work.memberB.admin.service;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
@Service
public class UploadService {
 private final Path root;
 public UploadService(@Value("${memberb.upload-dir:./member-b-uploads}")String directory){root=Path.of(directory).toAbsolutePath().normalize();}
 public String save(MultipartFile file)throws IOException{
  if(file==null || file.isEmpty() || file.getSize()>5*1024*1024)throw BusinessException.bad("请选择不超过5MB的PNG/JPEG/GIF图片");
  byte[] bytes=file.getBytes();BufferedImage image;
  try(ImageInputStream input=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))){
   var readers=ImageIO.getImageReaders(input);if(!readers.hasNext())throw BusinessException.bad("文件不是有效图片");
   ImageReader reader=readers.next();
   try{reader.setInput(input);String format=reader.getFormatName().toLowerCase();if(!format.matches("png|jpeg|jpg|gif"))throw BusinessException.bad("只支持PNG/JPEG/GIF");
    int w=reader.getWidth(0),h=reader.getHeight(0);if(w<1 || h<1 || w>4096 || h>4096 || (long)w*h>16000000)throw BusinessException.bad("图片尺寸不得超过4096及1600万像素");image=reader.read(0);
   }finally{reader.dispose();}
  }catch(BusinessException e){throw e;}catch(Exception e){throw BusinessException.bad("图片已损坏或格式无效");}
  ByteArrayOutputStream out=new ByteArrayOutputStream();ImageIO.write(image,"png",out);byte[] safe=out.toByteArray();
  String name;
  try{name=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(safe))+".png";}catch(Exception e){throw new IllegalStateException(e);}
  Files.createDirectories(root);
  try{Files.write(root.resolve(name),safe,StandardOpenOption.CREATE_NEW);}catch(FileAlreadyExistsException ignored){}
  return "/member-b/media/"+name;
 }
}
