package com.team.team_work.memberB.admin.service;

import com.team.team_work.memberB.admin.dto.ApiResult;
import com.team.team_work.memberB.admin.mapper.AdminMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.function.Supplier;

/** Serializes catalog invariants and commits the effect and replay receipt atomically. */
@Service
public class CommandService {
    private final AdminMapper mapper;
    private final JsonMapper json;
    public CommandService(AdminMapper mapper, JsonMapper json) { this.mapper=mapper;this.json=json; }
    @Transactional
    public ApiResult run(long actor,String key,String intent,Supplier<Object> action) {
        if(key==null || !key.matches("[a-zA-Z0-9_-]{16,100}")) throw BusinessException.bad("需要16至100位 Idempotency-Key");
        mapper.guard();
        String digest=hash(intent), old=mapper.receipt(actor,key);
        if(old!=null) {
            if(!digest.equals(mapper.fingerprint(actor,key))) throw new BusinessException(409,"此请求编号已用于不同操作");
            return json.readValue(old,ApiResult.class);
        }
        ApiResult result=ApiResult.ok(action.get());
        mapper.remember(actor,key,digest,json.writeValueAsString(result));
        return result;
    }
    public String intent(String operation,Object payload) { return operation+":"+json.writeValueAsString(payload); }
    public static String hash(String value) {
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}
        catch(Exception e){throw new IllegalStateException(e);}
    }
}
