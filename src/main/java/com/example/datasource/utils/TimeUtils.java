package com.example.datasource.utils;

import org.apache.commons.lang3.time.FastDateFormat;

/**
 * @author dell
 * @version 1.0
 * 时间工具类
 */
public abstract class TimeUtils {

    public static String format(Long timestamp,String pattern){
        return FastDateFormat.getInstance(pattern).format(timestamp);
    }

    public static void main(String[] args) {
        String format = TimeUtils.format(System.currentTimeMillis(), "yyyy-MM-dd");
        System.out.println(format);
    }

}
