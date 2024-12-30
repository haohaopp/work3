package org.example;

import java.sql.*;

public class JdbcDemo {
    public static void main(String[] args)  {
        try {
            JdbcUtil.Connection();
//            String q=JdbcUtil.queryOrders(Timestamp.valueOf("2024-12-23 00:00:00"), Timestamp.valueOf("2024-12-23 23:59:59"));
//            System.out.println(q);

//            String []products={"iPhone 14","Mate60"};
//            int[] quantity={2,3};
//            System.out.println(JdbcUtil.insertOrder(products,quantity));

//           System.out.println(JdbcUtil.deleteProduct("Mate60"));
//            System.out.println(JdbcUtil.deleteOrder(2));

//            System.out.println(JdbcUtil.updateOrder(3,"Xiaomi 15",1));

//            System.out.println(JdbcUtil.insertProduct("Xiaomi 15",3000.00));

//            System.out.println(JdbcUtil.updateOrder(1,"2024-12-26 21:09:48"));




        } catch (RuntimeException e) {
            System.out.println("捕获到错误！\n"+e.getMessage());
        } finally {
            JdbcUtil.release();
        }

    }
}
