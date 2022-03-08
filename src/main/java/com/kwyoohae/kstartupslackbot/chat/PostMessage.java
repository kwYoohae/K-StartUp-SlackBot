package com.kwyoohae.kstartupslackbot.chat;

import com.kwyoohae.kstartupslackbot.certification.Certification;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@EnableScheduling
public class PostMessage {

    @Value("${slack.token}")
    private String token;

    @Value("${slack.test.channel}")
    private String channelCode;

    private static final Logger log = LoggerFactory.getLogger(PostMessage.class);

    private List<Map<String,Object>> todayData = new ArrayList<>();

    private List<Map<String,Object>> newData = new ArrayList<>();

    @Scheduled(cron = "0 0 9 * * *")
    public void getTodayData() throws NoSuchAlgorithmException, KeyManagementException, SlackApiException, IOException {
        todayData = getKstartUpData();
        sendSlackMessage(combineData(todayData));

    }

    @Scheduled(cron = "0/5 * * * * *")
    public void getNewData() throws NoSuchAlgorithmException, KeyManagementException, SlackApiException, IOException {

        if(todayData.isEmpty()){
            log.info("todayData가 없어서 새로 불러왔습니다");
            sendSlackMessage("슬랙봇을 시작했습니다");
            todayData = getKstartUpData();
            sendSlackMessage(combineData(todayData));
        }
        newData.clear();
        newData = getKstartUpData();

        if(!newData.get(0).get("title").equals(todayData.get(0).get("title"))){
            log.info("새로운 데이터가 있어서 slack에 출력합니다. ");
            sendSlackMessage(combineNewData(splitNewData()));
            todayData.clear();
            todayData = getKstartUpData();
        }
    }

    public void sendSlackMessage(String text) throws SlackApiException, IOException {
        Slack slack = Slack.getInstance();

        MethodsClient methods = slack.methods(token);

        ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                .channel(channelCode)
                .text(text).build();

        ChatPostMessageResponse response = methods.chatPostMessage(request);
    }

    public List<Map<String,Object>> getKstartUpData() throws NoSuchAlgorithmException, KeyManagementException {

        PostMessage postMessage = new PostMessage();
        String URL = "https://www.k-startup.go.kr/common/announcement/announcementList.do?mid=30004&bid=701&searchAppAt=A";
        Certification.setSSL();
        Connection conn = Jsoup.connect(URL);

        List<Map<String, Object>> parseData = new ArrayList<>();

        try{
            Document document = conn.get();
            Elements title = document.select("div.middle > a > div.tit_wrap > p.tit");
            Elements start_time = document.select("div.bottom > span.list:eq(2)");
            Elements end_time = document.select("div.bottom > span.list:eq(3)");
            for(int i = 0 ; i < title.size() ; i++){
                Map<String, Object> data = new HashMap<>();
                data.put("title", title.get(i).text());
                data.put("start_time",start_time.get(i).text());
                data.put("end_time", end_time.get(i).text());
                parseData.add(data);
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        return parseData;
    }

    public String combineData(List<Map<String,Object>> dataList){

        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("[yyyy년 MM월 dd일]"));
        String result = ":mega:*" + todayDate + " 날짜의 데이터 입니다* :mega:\n\n";

        for(Map<String,Object> data : dataList){
            result += ">*" + data.get("title") + "*\n>" + data.get("start_time") + "\n>" + data.get("end_time") + "\n\n";
        }

        return result;
    }

    public String combineNewData(List<Map<String,Object>> dataList){

        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("[yyyy년 MM월 dd일]"));
        String result = ":no_entry:*" + todayDate + " 날짜의 새로운 공고 입니다* :no_entry:\n\n";

        for(Map<String,Object> data : dataList){
            result += ">*" + data.get("title") + "*\n>" + data.get("start_time") + "\n>" + data.get("end_time") + "\n\n";
        }

        return result;
    }

    public List<Map<String,Object>> splitNewData(){
        List<Map<String,Object>> result = new ArrayList<>();
        for(Map<String,Object> map : newData){
            if(!map.get("title").equals(todayData.get(0).get("title"))){
                Map<String,Object> newMap = new HashMap<>();
                newMap.put("title",map.get("title"));
                newMap.put("start_time",map.get("start_time"));
                newMap.put("end_time",map.get("end_time"));
                result.add(newMap);
            }
        }
        return result;
    }
}
