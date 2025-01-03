package tianqi;

import java.util.Scanner;

public class Demo {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        City[] cities=SQLUtil.queryCityList();
        System.out.println("-----------------");
        System.out.println("输入要查询的城市：");
        StringBuilder sb=new StringBuilder();
        for(City city:cities){
            sb.append(city.getName()+",");
        }
        System.out.println("已有的城市："+sb.toString());
        String input = sc.nextLine();
        City cityToQuery=null;
        for(City city:cities){
            if(city.getName().equals(input)){
                cityToQuery=city;
                break;
            }
        }
        if(cityToQuery!=null){
            //已有这个城市
            String weatherInfo=SQLUtil.queryCityWeather(cityToQuery);
            if(weatherInfo!=null){
                System.out.println(weatherInfo);
            }
            else{
                //数据库里没有天气信息，查询并储存天气信息
                WeatherAPI.getWeather(cityToQuery.getId());
                System.out.println(SQLUtil.queryCityWeather(cityToQuery));
            }
        }
        else{
            //没有这个城市
            System.out.println("没有该城市，正在建立该城市");
            //先查询城市id
            String cityInfo=WeatherAPI.getCityIdName(input);
            String [] cityInfos=cityInfo.split(" ");
            cityToQuery= new City(cityInfos[1],cityInfos[0]);
            //插入城市信息
            SQLUtil.storeCity(cityToQuery);
            System.out.println("创建新城市成功，正在查询天气");
            //查询并储存城市信息
            WeatherAPI.getWeather(cityToQuery.getId());
            System.out.println(SQLUtil.queryCityWeather(cityToQuery));
        }
        System.out.println("-----------------");
    }
}
