package tianqi;

import org.example.JdbcUtil;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SQLUtil {
    private static final String driver;
    private static final String url;
    private static final String user;
    private static final String password;
    static {
        //从文件里获取登入信息，并初始化
        try{
            InputStream in = JdbcUtil.class.getClassLoader().getResourceAsStream("db.properties");
            Properties prop = new Properties();
            prop.load(in);

            driver = prop.getProperty("driver");
            url = prop.getProperty("url");
            user = prop.getProperty("user");
            password = prop.getProperty("password");

            Class.forName(driver);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static City[] queryCityList(){
        try{
            Connection conn=DriverManager.getConnection(url,user,password);
            Statement st=conn.createStatement();
            ResultSet rs=st.executeQuery("select city_id,city_name from cities");
            List<City> cityList = new ArrayList<>();
            while(rs.next()){
                cityList.add(new City(rs.getString("city_name"), rs.getString("city_id")));
            }
            rs.close();
            st.close();
            conn.close();
            return cityList.toArray(new City[0]);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public  static String queryCityWeather(City city){
        //如果数据库里没有天气信息，返回null，否则返回天气信息
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate tomorrowTomorrow = today.plusDays(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayStr = formatter.format(today);
        String tomorrowStr = formatter.format(tomorrow);
        String tomorrowTomorrowStr = formatter.format(tomorrowTomorrow);
        try {
            StringBuilder sb=new StringBuilder();
            sb.append("城市："+city.getName());
            Connection conn=DriverManager.getConnection(url,user,password);
            Statement st=conn.createStatement();
            boolean flag=true;
            ResultSet rs=st.executeQuery("select fx_date,temp_max,temp_min,text_day from weather where city_id = '"+city.getId()+"' AND fx_date = '" +todayStr  + "'");
            if(rs.next()){
                sb.append("\n");
                sb.append(rs.getString("fx_date"));
                sb.append(" 最高温："+rs.getString("temp_max"));
                sb.append(" 最低温："+rs.getString("temp_min"));
                sb.append(" 天气："+rs.getString("text_day"));
            }else flag=false;

            rs=st.executeQuery("select fx_date,temp_max,temp_min,text_day from weather where city_id = '"+city.getId()+"' AND fx_date = '" +tomorrowStr  + "'");
            if(rs.next()){
                sb.append("\n");
                sb.append(rs.getString("fx_date"));
                sb.append(" 最高温："+rs.getString("temp_max"));
                sb.append(" 最低温："+rs.getString("temp_min"));
                sb.append(" 天气："+rs.getString("text_day"));
            }else flag=false;

            rs=st.executeQuery("select fx_date,temp_max,temp_min,text_day from weather where city_id = '"+city.getId()+"' AND fx_date = '" +tomorrowTomorrowStr  + "'");
            if(rs.next()){
                sb.append("\n");
                sb.append(rs.getString("fx_date"));
                sb.append(" 最高温："+rs.getString("temp_max"));
                sb.append(" 最低温："+rs.getString("temp_min"));
                sb.append(" 天气："+rs.getString("text_day"));
            }else flag=false;

            rs.close();
            st.close();
            conn.close();
            if(flag){
                return sb.toString();
            }else
                return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void storeWeatherData(String cityId, CityWeather[] weathers){
        //将查到的数据存储，并覆盖原数据
        try {
            Connection conn=DriverManager.getConnection(url,user,password);
            for(int i=0;i<weathers.length;i++){
                String fxDate=weathers[i].getFxDate();
                 String tempMax=weathers[i].getTempMax();
                 String tempMin=weathers[i].getTempMin();
                 String textDay=weathers[i].getTextDay();
                 Statement st=conn.createStatement();
                 //查询是否是已有日期
                 ResultSet rs=st.executeQuery("select fx_date from weather where city_id='"+cityId+"'");
                 boolean flag=false;
                 while(rs.next()){
                     if(fxDate.equals(rs.getString("fx_date"))){
                         flag=true;
                         break;
                     }
                 }
                 if(!flag){
                     //不是已有日期，插入日期
                     st.executeUpdate("INSERT INTO weather (city_id, fx_date, temp_max, temp_min, text_day) "
                             + "VALUES ('" + cityId + "', '" + fxDate + "', " + tempMax + ", " + tempMin + ", '" + textDay + "')");
                 }
                 else{
                     //是已有日期，更新日期
                     st.executeUpdate("UPDATE weather SET temp_max = " + tempMax + ", temp_min = " + tempMin
                             + ", text_day = '" + textDay + "' WHERE city_id = '" + cityId + "' AND fx_date = '" + fxDate + "'");
                 }
                 rs.close();
                 st.close();
            }
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void storeCity(City city){
        //将查到的city数据存储到city表
        try{
            Connection conn=DriverManager.getConnection(url,user,password);
            Statement st=conn.createStatement();
            st.executeUpdate("insert into cities (city_name,city_id) values('" + city.getName() + "' , '" + city.getId() + "')");
            st.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
