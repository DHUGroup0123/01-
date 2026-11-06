package com.team.team_work.memberB.admin.service;
import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
public final class PasswordCodec {
 private PasswordCodec(){}
 public static String encode(String password){
  if(password==null || password.length()<12 || password.length()>128) throw BusinessException.bad("密码长度须为12至128个字符");
  byte[] salt=new byte[16]; new SecureRandom().nextBytes(salt);
  return "pbkdf2$210000$"+Base64.getEncoder().encodeToString(salt)+"$"+Base64.getEncoder().encodeToString(derive(password,salt,210000));
 }
 public static boolean matches(String password,String encoded){
  try{String[] parts=encoded.split("\\$");if(parts.length!=4 || !parts[0].equals("pbkdf2"))return false;
   return MessageDigest.isEqual(Base64.getDecoder().decode(parts[3]),derive(password,Base64.getDecoder().decode(parts[2]),Integer.parseInt(parts[1])));
  }catch(RuntimeException e){return false;}
 }
 private static byte[] derive(String password,byte[] salt,int iterations){
  PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),salt,iterations,256);
  try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}
  catch(GeneralSecurityException e){throw new IllegalStateException("当前JDK不支持PBKDF2",e);}finally{spec.clearPassword();}
 }
}
