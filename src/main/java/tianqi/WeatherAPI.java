package tianqi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class WeatherAPI {
    private static final String apiKey = "*****";
    private static final String url = "https://devapi.qweather.com/v7/weather/3d";//三日天气api
    private static final String url1 = "https://geoapi.qweather.com/v2/city/lookup";//查询城市id的api


    public static String getCityIdName(String name) {
        //根据api，反回城市的id和name
        String encodedName = null;
        try {
            encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String urlString = url1 + "?key=" + apiKey + "&location=" + encodedName;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                GZIPInputStream gzipInputStream = new GZIPInputStream(connection.getInputStream());
                StringBuilder res = new StringBuilder();
                String line;
                BufferedReader br = new BufferedReader(new
                        InputStreamReader(gzipInputStream, StandardCharsets.UTF_8));
                while ((line = br.readLine()) != null) {
                    res.append(line);
                }
                JSONObject city = JSON.parseObject(res.toString());
                JSONArray cityArray = city.getJSONArray("location");
                JSONObject city1 = cityArray.getJSONObject(0);
                return city1.getString("id")+" "+city1.getString("name");
            }
            else
                return null;
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



        public static void getWeather (String cityId){
            String urlString = url + "?key=" + apiKey + "&location=" + cityId;
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    GZIPInputStream gzipInputStream = new GZIPInputStream(connection.getInputStream());
                    StringBuilder res = new StringBuilder();
                    String line;
                    BufferedReader br = new BufferedReader(new
                            InputStreamReader(gzipInputStream, StandardCharsets.UTF_8));
                    while ((line = br.readLine()) != null) {
                        res.append(line);
                    }
                    //反序列化
                    JSONObject weather = JSON.parseObject(res.toString());
                    JSONArray daily = weather.getJSONArray("daily");
                    CityWeather[] weathers = new CityWeather[daily.size()];
                    for (int i = 0; i < daily.size(); i++) {
                        weathers[i] = JSON.parseObject(daily.get(i).toString(), CityWeather.class);
                    }
                    SQLUtil.storeWeatherData(cityId, weathers);

                }
                else
                    System.out.println("111111");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }



}
