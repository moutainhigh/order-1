package com.imooc.order.utils;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class test {

    public static void main(String args[]) throws InterruptedException {
        int count = 0, attemptTimes = 3;
        while (count < attemptTimes){
            if (count == 2){
                Thread.sleep(2000);
                break;
            }
            count++;
        }
        System.out.println("count:"+count);
        if (count == 3){
            System.out.println("尝试了3次都没成功");
        }


    }

    private static void get(Object... s) {

        Integer k = (Integer) s[0];
        System.out.println("k:"+s[0]);

        if(s.length > 1 && !StringUtils.isEmpty(s[1])){
            String d = (String) s[1];
            System.out.println("d:"+d);
        }


    }


}
