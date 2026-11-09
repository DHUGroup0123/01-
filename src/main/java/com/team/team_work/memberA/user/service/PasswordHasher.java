package com.team.team_work.memberA.user.service;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
@Component
public class PasswordHasher {
    public String hash(String password) {
        try {return HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(password.getBytes(StandardCharsets.UTF_8)));}
        catch(java.security.GeneralSecurityException e){throw new IllegalStateException(e);}
    }
    public boolean matches(String password,String encoded) {
        return password!=null&&encoded!=null&&MessageDigest.isEqual(hash(password).getBytes(StandardCharsets.US_ASCII),encoded.toLowerCase(java.util.Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
    }
}
