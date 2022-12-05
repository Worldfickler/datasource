package com.example.datasource.generator;

import com.alibaba.fastjson.JSON;
import com.example.datasource.bean.MaterialBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * @author dell
 * @version 1.0
 * 防疫物资数据生成
 */

@Component
public class Covid19DataGenerator {

    @Autowired
    private KafkaTemplate kafkaTemplate;

//    @Scheduled(initialDelay = 1000, fixedDelay = 1000 * 10)
    public void generator() {

        System.out.println("每隔10s生成10条数据");
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            MaterialBean materialBean = new MaterialBean(wzmc[random.nextInt(wzmc.length)], wzlx[random.nextInt(wzlx.length)], random.nextInt(1000));
            String jsonString = JSON.toJSONString(materialBean);
            System.out.println(materialBean);
            kafkaTemplate.send("covid19_wz", random.nextInt(3),jsonString);
        }


    }

    private static String[] wzmc = new String[]{"N95口罩/个", "医用外科口罩/个", "84消毒液/瓶", "电子体温计/个", "一次性橡胶手套/副", "防护目镜/副",  "医用防护服/套"};

    private static String[] wzlx = new String[]{"采购", "下拨", "捐赠", "消耗","需求"};


}
