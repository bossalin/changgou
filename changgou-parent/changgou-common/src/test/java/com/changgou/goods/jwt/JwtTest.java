package com.changgou.goods.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class JwtTest {

    /****
     * 创建Jwt令牌
     */
    @Test
    public void testCreateJwt(){
        JwtBuilder builder= Jwts.builder()
                .setIssuer("bossalin")    //颁发者
                .setId("888")             //设置唯一编号
                .setSubject("jwt令牌测试")       //设置主题  可以是JSON数据
                .setIssuedAt(new Date())  //设置签发日期
                .setExpiration(new Date(System.currentTimeMillis()+3600000))//用于设置过期时间 ，参数为Date类型数据
                .signWith(SignatureAlgorithm.HS256,"itcast");//设置签名 使用HS256算法，并设置SecretKey(字符串)
        Map<String,Object>map=new HashMap<>();
        map.put("name","jack");
        builder.addClaims(map);
        //构建 并返回一个字符串
        System.out.println(builder.compact());
    }

    /***
     * 解析Jwt令牌数据
     */
    @Test
    public void testParseJwt(){
        String compactJwt="eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJib3NzYWxpbiIsImp0aSI6Ijg4OCIsInN1YiI6Imp3dOS7pOeJjOa1i-ivlSIsImlhdCI6MTYwNzI0MzczMCwiZXhwIjoxNjA3MjQ3MzMwLCJuYW1lIjoiamFjayJ9.vp2rpFmKWF07yO8S-McSgCWEgvqc-AXyYaYPgU4JLdg";
        Claims claims = Jwts.parser().
                setSigningKey("itcast").
                parseClaimsJws(compactJwt).
                getBody();   //获取解析后的数据
        System.out.println(claims.toString());
    }
}
