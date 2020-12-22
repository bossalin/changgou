package com.changgou.goods.base64;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class Base64Test {

    @Test
    public void testEncode(){
        byte[] encode = Base64.getEncoder().encode("abcdef".getBytes());
        try {
            String s=new String(encode,"UTF-8");
            System.out.println("encode:  "+s);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }



    @Test
    public void testDecode(){
        byte[] decode = Base64.getDecoder().decode("YWJjZGVm");
        try {
            String s=new String(decode,"UTF-8");
            System.out.println("decode:  "+s);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
