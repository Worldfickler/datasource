package com.example.datasource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.datasource.bean.CovidBean;
import com.example.datasource.utils.HttpUtils;
import com.example.datasource.utils.TimeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class DatasourceApplicationTests {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    //����springboot����kafka
    @Test
    public void testKafkaTemplate() throws InterruptedException {

        kafkaTemplate.send("test1", 1, "abc");
        Thread.sleep(1000000);

    }

    @Test
    public void testCrawling() throws InterruptedException {

        String datetime = TimeUtils.format(System.currentTimeMillis(), "yyyy-MM-dd");

        //1.��ȡָ��ҳ��
        String html = HttpUtils.getHtml("https://ncov.dxy.cn/ncovh5/view/pneumonia");
//        System.out.println(html);

        //2.����ҳ���е�ָ������-��idΪgetAreaStat�ı�ǩ�е�ȫ����������
        Document doc = Jsoup.parse(html);
        String text = doc.select("script[id=getAreaStat]").toString();
//        System.out.println(text);

        //3.ʹ��������ʽ��ȡjson��ʽ������
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
        //��json���ݸ���һ���Ľ���
        //4.����һ��json(ʡ������)����Ϊjavabean
        List<CovidBean> province_covidBeans = JSON.parseArray(jsonStr, CovidBean.class);
        for (CovidBean province_covidBean : province_covidBeans) {
//            System.out.println(province_covidBean);
            //����ʱ���ֶ�
            province_covidBean.setDatetime(datetime);
            //��ȡcities
            String cities = province_covidBean.getCities();
            //5.��citiesҲת����json��ʽ����
            List<CovidBean> city_covidBeans = JSON.parseArray(cities, CovidBean.class);
            for (CovidBean city_covidBean : city_covidBeans) {
//                System.out.println(city_covidBean);
                city_covidBean.setDatetime(datetime);
                city_covidBean.setPid(province_covidBean.getLocationId());
                city_covidBean.setProvinceShortName(province_covidBean.getProvinceShortName());
//                System.out.println(city_covidBean);
                //�����е��������ݷ��͸�kafka
                //��javabeanת��Ϊjsonstr�ٷ��͸�kafka
                String city_str = JSON.toJSONString(city_covidBean);
                System.out.println(city_str);
                kafkaTemplate.send("covid19", city_covidBean.getPid(), city_str);
            }
            //6.��ȡ��һ��json��ÿһ���ͳ������
            String statisticsDataUrl = province_covidBean.getStatisticsData();
            String statisticsDataStr = HttpUtils.getHtml(statisticsDataUrl);
            //��ȡstatisticsDataStr�е�data�ֶζ�Ӧ������
            JSONObject jsonObject = JSON.parseObject(statisticsDataStr);
            String dataStr = jsonObject.getString("data");
//            System.out.println(dataStr);
            //�������������������·Żص�province_covidBean�е�getStatisticsData(֮ǰֻ��һ��url)
            province_covidBean.setStatisticsData(dataStr);
            province_covidBean.setCities(null);
//            System.out.println(province_covidBean);
            //��ʡ�ݵ��������ݷ��͸�kafka
            //ת��
            String province_str = JSON.toJSONString(province_covidBean);
            System.out.println(province_str);
            kafkaTemplate.send("covid19", province_covidBean.getLocationId(), province_str);
//            System.out.println("λ��id : " + province_covidBean.getLocationId());

        }
        Thread.sleep(1000000);

    }

    @Test
    void contextLoads() {
    }

}
