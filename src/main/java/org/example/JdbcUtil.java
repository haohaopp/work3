package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class JdbcUtil {
    private static final String driver;
    private static final String url;
    private static final String user;
    private static final String password;
    //预定义了三个sql变量，供方法里使用和统一关闭
    private static Connection conn=null;
    private static PreparedStatement pst=null;
    private static ResultSet rs=null;

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
        } catch (IOException |ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void Connection()  {
        //获取连接
        try {
            conn=DriverManager.getConnection(url,user,password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void release()  {
        //关闭连接
        if(rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if(pst != null) {
            try {
                pst.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if(conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String queryProducts(String name){
        //查询商品信息，传入商品名称，查询出商品的价格，返回为字符串
        try {
            pst = conn.prepareStatement("select product_name,product_price from products where product_name=?");
            pst.setString(1,name);
            rs = pst.executeQuery();
            if(rs.next())
                return rs.getString("product_name")+" | "+rs.getString("product_price");
            else
                return "没有该商品！";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String insertProduct(String product_name,double product_price){
        //增加商品信息，如果已有该商品，则退出插入
        try {
            pst = conn.prepareStatement("select product_name from products");
            rs = pst.executeQuery();
            while (rs.next()) {
                if(product_name.equals(rs.getString("product_name"))) {
                    return "已有这个商品！退出插入！";
                }
            }
            pst=conn.prepareStatement("insert into products(product_name,product_price) values(?,?)");
            pst.setString(1, product_name);
            pst.setDouble(2, product_price);
            int i=pst.executeUpdate();
            if(i>0){
                return "插入成功！受影响行数："+i;
            }else
                return "插入失败";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static String deleteProduct(String product_name){
        //删除商品信息，因为在建表时使用严格删除，所以如果商品在订单中，则删除失败
        try {
            pst=conn.prepareStatement("delete from products where product_name=?");
            pst.setString(1, product_name);
            int i=pst.executeUpdate();
            if(i>0) {
                return "删除成功！删除商品："+product_name;
            }else
                return "删除失败";
        } catch (SQLException e) {
            return "删除失败！商品可能存在订单中，请先删除对应订单";
        }
    }
    public static String updateProduct(String product_name,double product_price){
        //更新商品信息
        try {
            pst=conn.prepareStatement("update products set product_price=? where product_name=?");
            pst.setDouble(1, product_price);
            pst.setString(2, product_name);
            int i=pst.executeUpdate();
            if(i>0) {
                return "修改成功！修改 "+product_name+" 为 "+product_price+"元";
            }else
                return "修改失败！";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static String queryOrders(Timestamp sdate,Timestamp edate){
        //查询订单信息，传入起始时间和结束时间，返回该时间段里的订单
        try {
            pst = conn.prepareStatement("SELECT o.order_id, o.order_time, p.product_name, oi.quantity, oi.item_total_price\n" +
                    "FROM orders o\n" +
                    "JOIN order_items oi ON o.order_id = oi.order_id\n" +
                    "JOIN products p ON oi.product_id = p.product_id\n" +
                    "WHERE o.order_time between ? and ?;");
            pst.setTimestamp(1,sdate);
            pst.setTimestamp(2,edate);
            rs = pst.executeQuery();
            StringBuffer sb = new StringBuffer();
            while (rs.next()) {
                sb.append("订单编号：").append(rs.getInt("order_id"))
                        .append(" 下单时间：").append(rs.getString("order_time"))
                        .append(" 商品名称：").append(rs.getString("product_name"))
                        .append(" 商品数量：").append(rs.getInt("quantity"))
                        .append("\n");
            }
            return sb.toString();
            } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static String insertOrder(String[] product_name,int[] product_quantity) {
        //插入订单,传入一一对应的商品名称和商品数量的数组
        if (product_name.length != product_quantity.length) {
            return "商品数量和商品名长度不一致！";
        }
        try {
            conn.setAutoCommit(false);//开启事务块
            //先通过商品名查询商品的价格和id，并计算出订单总金额
            double [] product_price = new double[product_name.length];
            int[] product_id = new int[product_name.length];
            double total_price = 0;

            pst = conn.prepareStatement("select product_price,product_id from products where product_name=?");
            for(int i=0;i<product_name.length;i++) {
                pst.setString(1, product_name[i]);
                rs = pst.executeQuery();
                if (rs.next()) {
                    double price = rs.getDouble("product_price");
                    product_id[i] = rs.getInt("product_id");
                    product_price[i] = price*product_quantity[i];
                    total_price += price*product_quantity[i];
                }
                else
                    return "商品 " + product_name[i] + " 不存在！";
            }
            //创建订单
            int order_id ;//初始化订单id，之后从查询返回值里获取
            pst= conn.prepareStatement("insert into orders(order_time,order_total_price) values(NOW(),?)",PreparedStatement.RETURN_GENERATED_KEYS);
            pst.setDouble(1, total_price);
            int i=pst.executeUpdate();
            if(i>0) {
                rs=pst.getGeneratedKeys();
                if(rs.next()) {
                    order_id = rs.getInt(1);
                }else
                    return "生成订单ID失败！";
            }else
                return "创建订单失败！";

            //创建订单和商品关系表
            int count=0;//计数插入了几行
            for(int ii=0;ii<product_name.length;ii++) {
                pst=conn.prepareStatement("insert into order_items(order_id,product_id,quantity,item_total_price) values(?,?,?,?)");
                pst.setInt(1,order_id);
                pst.setInt(2,product_id[ii]);
                pst.setInt(3,product_quantity[ii]);
                pst.setDouble(4,product_price[ii]);
                count+=pst.executeUpdate();
            }
            conn.commit();
            return "创建订单成功！订单id为`"+order_id+"`插入关系表成功！插入了: "+count+" 行";
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    // 回滚事务
                    conn.rollback();
                    System.out.println("事务已回滚！");
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            return "数据库操作失败！";
        }
    }
    public static String deleteOrder(int order_id){
        //删除订单，因为在建表时使用了级联删除，所有同时删除对应的关系表
        try {
            pst= conn.prepareStatement("delete from orders where order_id=?");
            pst.setInt(1, order_id);
            int i=pst.executeUpdate();
            if(i>0) {
                return "删除成功，已将该订单及其关系表删除，订单id: "+order_id;
            }else
                return "删除失败";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String updateOrder(int order_id,String product_name,int product_quantity) {
        //修改商品数量和种类，如果已有该商品，更新其数量，如果没有，则新增该商品
        try{
            conn.setAutoCommit(false);
            //先查询商品id和商品价格
            int productId;
            double productPrice;
            pst= conn.prepareStatement("select product_id,product_price from products where product_name=?");
            pst.setString(1, product_name);
            rs = pst.executeQuery();
            if (rs.next()) {
                productId = rs.getInt("product_id");
                productPrice = rs.getDouble("product_price");
            }else
                return "没有该商品，更新失败";
            //再查是否已有该商品
            int quantity=-1;
            pst= conn.prepareStatement("select product_id,quantity from order_items where order_id=?");
            pst.setInt(1, order_id);
            rs = pst.executeQuery();
            while (rs.next()) {
                if(rs.getInt("product_id")== productId) {
                    quantity=rs.getInt("quantity");
                    break;
                }
            }
            if(quantity>0) {
                //找到商品在订单中
                //修改关系表中商品数量和价格
                pst=conn.prepareStatement("update order_items set quantity=?,item_total_price=? where order_id=? and product_id=?");
                pst.setInt(1, product_quantity);
                pst.setDouble(2, product_quantity* productPrice);
                pst.setInt(3, order_id);
                pst.setInt(4, productId);
                pst.executeUpdate();
                //修改订单表
                pst= conn.prepareStatement("update orders set order_total_price=order_total_price+? where order_id=? ");
                pst.setDouble(1, (product_quantity-quantity)* productPrice);
                pst.setInt(2, order_id);
                pst.executeUpdate();
            }
            else {
                //商品不在订单中
                //增加关系表
                pst=conn.prepareStatement("insert order_items(order_id,product_id,quantity,item_total_price) values(?,?,?,?)");
                pst.setInt(1, order_id);
                pst.setInt(2, productId);
                pst.setInt(3, product_quantity);
                pst.setDouble(4,product_quantity*productPrice);
                pst.executeUpdate();
                //修改订单表
                pst= conn.prepareStatement("update orders set order_total_price=order_total_price+? where order_id=? ");
                pst.setDouble(1, product_quantity*productPrice);
                pst.setInt(2, order_id);
                pst.executeUpdate();
            }
            conn.commit();
            return "修改成功！";
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    // 回滚事务
                    conn.rollback();
                    System.out.println("事务已回滚！");
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            return "数据库操作失败";
        }
    }
    public static String updateOrder(int order_id,String time){
        //重载，修改订单时间
        try {
            try{
                Timestamp timestamp=Timestamp.valueOf(time);
            }
            catch (IllegalArgumentException e) {
                return "非法输入"+e.getMessage();
            }
            pst= conn.prepareStatement("update orders set order_time=? where order_id=?");
            pst.setTimestamp(1,Timestamp.valueOf(time));
            pst.setInt(2, order_id);
            pst.executeUpdate();
            return "修改成功";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


}

