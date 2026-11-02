package com.team.team_work.memberA.user.service;

import org.springframework.stereotype.Component;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PasswordHasher {
    private static final int ITERATIONS=600000;
    private final SecureRandom random=new SecureRandom();
    public String hash(String password) {
        byte[] salt=new byte[16]; random.nextBytes(salt);
        return "pbkdf2-sha256$"+ITERATIONS+"$"+Base64.getEncoder().encodeToString(salt)+"$"
            +Base64.getEncoder().encodeToString(derive(password,salt));
    }
    public boolean matches(String password, String encoded) {
        if(password==null || encoded==null) return false;
        try {
            String[] parts=encoded.split("\\$",-1);
            if(parts.length!=4 || !parts[0].equals("pbkdf2-sha256") || !parts[1].equals(""+ITERATIONS)) return false;
            byte[] salt=Base64.getDecoder().decode(parts[2]);
            byte[] expected=Base64.getDecoder().decode(parts[3]);
            return salt.length==16 && expected.length==32 && MessageDigest.isEqual(expected,derive(password,salt));
        } catch(IllegalArgumentException e) { return false; }
    }
    private byte[] derive(String password, byte[] salt) {
        PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),salt,ITERATIONS,256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch(java.security.GeneralSecurityException e) { throw new IllegalStateException("密码服务不可用",e); }
        finally { spec.clearPassword(); }
    }
}
