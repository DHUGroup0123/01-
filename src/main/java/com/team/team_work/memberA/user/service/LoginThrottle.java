package com.team.team_work.memberA.user.service;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.*;

/** Per-IP and per-account 5-minute window; bounded memory, no trusted proxy header. */
@Component
public class LoginThrottle {
    private record Window(Instant until,int count) { }
    private final Map<String,Window> entries=new HashMap<>();
    public synchronized void attempt(String ip,String loginName) {
        Instant now=Instant.now();entries.entrySet().removeIf(e->!e.getValue().until().isAfter(now));
        String account="user:"+loginName.toLowerCase(Locale.ROOT),address="ip:"+ip;
        if(entries.size()>10000) throw new BusinessException(429,"请求较多，请稍后再试");
        for(String key:List.of(account,address)) {
            Window w=entries.get(key);
            if(w!=null && w.count()>=(key.equals(account)?10:60)) throw new BusinessException(429,"登录尝试过多，请5分钟后再试");
        }
        for(String key:List.of(account,address)) {Window w=entries.get(key);entries.put(key,new Window(w==null?now.plusSeconds(300):w.until(),w==null?1:w.count()+1));}
    }
}
