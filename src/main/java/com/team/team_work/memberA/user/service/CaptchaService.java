package com.team.team_work.memberA.user.service;

import com.team.team_work.common.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.security.SecureRandom;
import java.time.Instant;
import javax.imageio.ImageIO;

@Service
public class CaptchaService {
    public record Challenge(String answer, Instant expiresAt) implements Serializable { }
    private final SecureRandom random=new SecureRandom();
    private static final String ALPHABET="23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    public byte[] image(HttpSession session) {
        StringBuilder code=new StringBuilder();
        for(int i=0;i<5;i++) code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        synchronized(session) { session.setAttribute(SessionKeys.CAPTCHA,new Challenge(code.toString(),Instant.now().plusSeconds(120))); }
        BufferedImage image=new BufferedImage(160,54,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=image.createGraphics();
        try {
            g.setColor(new Color(235,244,238));g.fillRect(0,0,160,54);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            for(int i=0;i<14;i++) {g.setColor(new Color(170+random.nextInt(60),190+random.nextInt(50),185));g.drawLine(random.nextInt(160),random.nextInt(54),random.nextInt(160),random.nextInt(54));}
            g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,29));
            for(int i=0;i<5;i++) {g.setColor(new Color(20+random.nextInt(75),60+random.nextInt(75),60));g.drawString(code.substring(i,i+1),12+i*28,36+random.nextInt(6));}
        } finally {g.dispose();}
        try {ByteArrayOutputStream out=new ByteArrayOutputStream();ImageIO.write(image,"png",out);return out.toByteArray();}
        catch(java.io.IOException e) {throw new IllegalStateException("验证码图片不可用",e);}
    }
    public void consume(HttpSession session,String answer) {
        Challenge value;
        synchronized(session) {value=(Challenge)session.getAttribute(SessionKeys.CAPTCHA);session.removeAttribute(SessionKeys.CAPTCHA);}
        if(value==null || !Instant.now().isBefore(value.expiresAt()) || answer==null || !value.answer().equalsIgnoreCase(answer.trim()))
            throw new BusinessException(400,"验证码错误或已过期，请刷新后重试");
    }
}
