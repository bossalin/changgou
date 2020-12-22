package com.changgou.goods.token;

import org.junit.Test;

import java.util.Base64;

public class Base64EncodeDecode {

    @Test
    public void test() throws Exception {
        String s="Y2hhbmdnb3U6Y2hhbmdnb3U=";
        byte[] decode = Base64.getDecoder().decode(s);
        String decodeString=new String(decode,"UTF-8");
        System.out.println(decodeString);
    }
}
