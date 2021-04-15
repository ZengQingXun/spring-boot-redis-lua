package com.jincou.redislua.service.impl;

import com.jincou.redislua.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xub
 * @Description: redis 实现类
 * @date 2019/7/25 下午7:44
 */
@Component
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    private DefaultRedisScript<Boolean> existsDefaultRedisScript;
    private DefaultRedisScript<Boolean> insertDefaultRedisScript;

    @PostConstruct
    public void initDefaultRedisScript() {
        existsDefaultRedisScript = getExistsDefaultRedisScript();
        insertDefaultRedisScript = getInsertDefaultRedisScript();
    }

    @Override
    public Object addsLuaBloomFilter(String filterName, List<String> values) {
        /**
         * 这里调用方法 execute(RedisScript<T> script, List<K> keys, Object... args)
         * 这里的keys 对于 lua脚本中的 KEY[i]  这个i跟集合大小有关
         * 这里的args 对于 lua脚本中的 ARGV[i] 这个i跟加入可变参数的个数有关
         */
        List<String> keyList= new ArrayList<>();
        keyList.add(filterName);
        keyList.add(values.get(0));
        return redisTemplate.execute(insertDefaultRedisScript, values, filterName);
    }
    @Override
    public Boolean existsLuabloomFilter(String filterName, String value) {
        List<String> keyList= new ArrayList<>();
        keyList.add(filterName);
        keyList.add(value);
        return (Boolean) redisTemplate.execute(existsDefaultRedisScript,keyList);
    }

    private DefaultRedisScript<Boolean> getExistsDefaultRedisScript() {
        return getDefaultRedisScript("bloomFilter-exist.lua");
    }

    private DefaultRedisScript<Boolean> getInsertDefaultRedisScript() {
        return getDefaultRedisScript("bloomFilter-inster.lua");
    }

    private DefaultRedisScript<Boolean> getDefaultRedisScript(String s) {
        DefaultRedisScript<Boolean> bloomExists = new DefaultRedisScript<>();
        bloomExists.setScriptSource(new ResourceScriptSource(new ClassPathResource(s)));
        bloomExists.setResultType(Boolean.class);
        return bloomExists;
    }

}
