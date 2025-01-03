# 订单管理系统

## 1.项目简介

实现了一个简单的订单管理系统

使用mysql和jdbc来管理和访问数据

基本实现了数据库的连接，数据的增删改查，解决SQL注入问题，添加事务管理，异常处理和资源释放

通过数据库记录信息：

​		商品：商品id、商品名称、商品价格

​		订单: 订单编号、下单时间、订单价格

​		订单商品信息：订单id、商品id、商品数量、商品总价格

## 2.功能实现

编写JdbcUtil类，实现数据库的连接和关闭，订单和商品的增删改查

从db.properties文件里读取数据库连接需要的信息

使用preparestatement，防止sql注入

在执行多条sql语句时使用事务块，防止数据丢失

在插入订单时自动增加订单和商品关系表

结构如下：

![image-20250103114121013](pic\image-20250103114121013.png)

查询订单示例：

```java
System.out.println(JdbcUtil.queryOrders(Timestamp.valueOf("2024-12-28 00:00:00"), Timestamp.valueOf("2024-12-28 23:59:59")));
```

结果：

![image-20250103114453797](pic\image-20250103114453797.png)

查询商品实例：

```java
System.out.println(JdbcUtil.queryProducts("Mate60"));
```

结果：

![image-20250103115708761](pic\image-20250103115708761.png)

## 3.问题解决

#### 3.1建表问题

在product表增加"ON DELETE RESTRICT" ，使商品在订单中时删除失败，需要先删除订单

在order表增加“ON DELETE CASCADE” ，使删除订单时级联删除对应的关系表

#### 3.2 SQL注入问题

使用prepareStatement，预定义sql语句

## 4.心得体会

学会数据库的安装与使用，使用可视化工具管理数据库

学会了jdbc的基本用法

学会了数据库设计三大范式，使建表更规范

学会了熟练使用sql语句进行增删改查

学会了使用事务块



# 天气查询系统

## 1.项目简介

使用Java原生的 `HttpURLConnection`来进行网络连接

调用https://geoapi.qweather.com/v2/city/lookup 来查询城市id

调用https://devapi.qweather.com/v7/weather/3d来查询三日天气

编写SQLUtil.java类来实现数据库相关功能

编写WeatherAPI.java类来实现api调用相关功能

编写Demo.java来实现控制台查询相关功能

编写CityWeather.java，City.java两个实体类来存储查询到的数据，其中CityWeather.java用来反序列化

在数据库里建立city表：城市名称、城市id

weather表：城市id、日期、最高温、最低温、天气情况，并将城市id设置为外键

## 2.功能实现

使用控制台管理输入输出

查询时会先从数据库里查找天气信息，如果没有该日的天气，则调用api查找并覆盖数据库里已有的天气

如果数据库里已有该城市：

![屏幕截图 2025-01-02 232205](pic\屏幕截图 2025-01-02 232205.png)

如果数据库里没有该城市，则先调用第一个api查询城市id，再调用第二个api查询三日天气，保存到数据库并输出到控制台中：

![屏幕截图 2025-01-02 235131](pic\屏幕截图 2025-01-02 235131.png)

## 3.问题解决

在调用api时如果输入中文，编码会出现问题导致调用返回值不等于200，进而导致查询失败

解决方法：

```java
encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString());
```

提前将输入的name编码成UTF_8格式

## 4.心得体会

学会了查看api文档，调用api获得json文件

学会使用maven管理项目和导入依赖

还可以改进的地方：

- 增加分页查询，提升查询大规模数据的效率
- 使用连接池，增加重复连接效率
- 下次就使用框架，不自己写jdbc了







