package com.kwyoohae.kstartupslackbot.chat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class SeleniumPostMessage {

    public static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    public static final String WEB_DRIVER_PATH = "./chromedriver_mac";

    public WebDriver getConnection(){
        try {
            System.setProperty(WEB_DRIVER_ID,WEB_DRIVER_PATH);
        }catch (Exception e){
            e.printStackTrace();
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");

        return new ChromeDriver(options);
    }

    public void getData(){
        WebDriver driver = getConnection();

        String url = "https://www.k-startup.go.kr/common/announcement/announcementList.do?mid=30004&bid=701&searchAppAt=A";
        driver.get(url);

        threadSleepOneSecond();

        Select select = new Select(driver.findElement(By.id("sort")));
        select.selectByIndex(1);

        threadSleepOneSecond();

        List<WebElement> data = driver.findElements(By.cssSelector("div.middle > a > div.tit_wrap > p.tit"));


        for (WebElement e : data){
            System.out.println(e.getText());
        }
    }

    public void threadSleepOneSecond(){
        try {Thread.sleep(1000);}
        catch (InterruptedException e){e.printStackTrace();}
    }
}
