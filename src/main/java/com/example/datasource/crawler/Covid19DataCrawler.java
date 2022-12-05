package com.example.datasource.crawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.datasource.bean.CovidBean;
import com.example.datasource.utils.HttpUtils;
import com.example.datasource.utils.TimeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dell
 * @version 1.0
 * 实现疫情数据爬取
 */

@Component
public class Covid19DataCrawler {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    //后续改成定时任务
    @Scheduled(initialDelay = 1000, fixedDelay = 1000 * 60 * 60 * 12)//启动执行一次，后续每经过12个小时执行一次
//    @Scheduled(cron = "0 0 8 * * ?")//每天8点执行
    public void testCrawling() throws InterruptedException {

//        System.out.println("每隔10s执行一次");
        String datetime = TimeUtils.format(System.currentTimeMillis(), "yyyy-MM-dd");

        //1.爬取指定页面
        String html = HttpUtils.getHtml("https://ncov.dxy.cn/ncovh5/view/pneumonia");
//        System.out.println(html);

        //2.解析页面中的指定内容-即id为getAreaStat的标签中的全国疫情数据
        Document doc = Jsoup.parse(html);
        String text = doc.select("script[id=getAreaStat]").toString();
//        System.out.println(text);

        //3.使用正则表达式获取json格式的数据
        String pattern = "\\[(.*)\\]";
        Pattern reg = Pattern.compile(pattern);
        Matcher matcher = reg.matcher(text);
        String jsonStr = "";
        if (matcher.find()) {
            jsonStr = matcher.group(0);
//            System.out.println(jsonStr);
        } else {
            System.out.println("NO MATCH");
        }
        //对json数据更进一步的解析
        //4.将第一次json(省份数据)解析为javabean
        List<CovidBean> province_covidBeans = JSON.parseArray(jsonStr, CovidBean.class);
        for (CovidBean province_covidBean : province_covidBeans) {
//            System.out.println(province_covidBean);
            //设置时间字段
            province_covidBean.setDatetime(datetime);
            //获取cities
            String cities = province_covidBean.getCities();
            //5.将cities也转换成json格式数据
            List<CovidBean> city_covidBeans = JSON.parseArray(cities, CovidBean.class);
            for (CovidBean city_covidBean : city_covidBeans) {
//                System.out.println(city_covidBean);
                city_covidBean.setDatetime(datetime);
                city_covidBean.setPid(province_covidBean.getLocationId());
                city_covidBean.setProvinceShortName(province_covidBean.getProvinceShortName());
//                System.out.println(city_covidBean);
                //将城市的疫情数据发送给kafka
                //将javabean转换为jsonstr再发送给kafka
                String city_str = JSON.toJSONString(city_covidBean);
                System.out.println(city_str);
                kafkaTemplate.send("covid19", city_covidBean.getPid(), city_str);
            }
            //6.获取第一次json中每一天的统计数据
            String statisticsDataUrl = province_covidBean.getStatisticsData();
            String statisticsDataStr = HttpUtils.getHtml(statisticsDataUrl);
            //获取statisticsDataStr中的data字段对应的数据
            JSONObject jsonObject = JSON.parseObject(statisticsDataStr);
            String dataStr = jsonObject.getString("data");
//            System.out.println(dataStr);
            //将解析出来的数据重新放回到province_covidBean中的getStatisticsData(之前只是一个url)
            province_covidBean.setStatisticsData(dataStr);
            province_covidBean.setCities(null);
//            System.out.println(province_covidBean);
            //将省份的疫情数据发送给kafka
            //转换
            String province_str = JSON.toJSONString(province_covidBean);
            System.out.println(province_str);
            kafkaTemplate.send("covid19", province_covidBean.getLocationId(), province_str);
//            System.out.println("位置id : " + province_covidBean.getLocationId());

        }
//        Thread.sleep(1000000);

    }


}
