import org.example.JdbcUtil;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class JdbcTest {
    int id;
    @BeforeAll
    public static void init() {
        JdbcUtil.Connection();
    }
    @BeforeEach
    public void prepareData() {
        JdbcUtil.insertProduct("TestProduct1", 10.0);
        JdbcUtil.insertProduct("TestProduct2", 20.0);
    }
    @AfterAll
    public static void close(){
        JdbcUtil.release();
    }
    @AfterEach
    public void cleanData() {
        JdbcUtil.deleteProduct("TestProduct1");
        JdbcUtil.deleteProduct("TestProduct2");
    }

    @Test
    public void testQueryProducts() {
        String result = JdbcUtil.queryProducts("TestProduct1");
        assertEquals("TestProduct1 | 10.00", result);
    }
    @Test
    public void testInsertProduct() {
        String result = JdbcUtil.insertProduct("NewProduct", 30.0);
        assertEquals("插入成功！受影响行数：1", result);
        String queryResult = JdbcUtil.queryProducts("NewProduct");
        assertEquals("NewProduct | 30.00", queryResult);
        JdbcUtil.deleteProduct("NewProduct");
    }
    @Test
    public void testDeleteProduct() {
        String result = JdbcUtil.deleteProduct("TestProduct1");
        assertEquals("删除失败！商品可能存在订单中，请先删除对应订单", result);
        String queryResult = JdbcUtil.queryProducts("TestProduct1");
        assertEquals("没有该商品！", queryResult);
    }

    @Test
    public void testUpdateProduct() {
        String result = JdbcUtil.updateProduct("TestProduct1", 15.0);
        assertEquals("修改成功！修改 TestProduct1 为 15.00元", result);
        String queryResult = JdbcUtil.queryProducts("TestProduct1");
        assertEquals("TestProduct1 | 15.00", queryResult);
    }
    @Test
    public void testQueryOrders() {
        String[] products = {"TestProduct1", "TestProduct2"};
        int[] quantities = {1, 2};
        JdbcUtil.insertOrder(products, quantities);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoMinutesBefore = now.minusMinutes(2);
        LocalDateTime twoMinutesAfter = now.plusMinutes(2);
        //当前获取时间，差值2分钟
        Timestamp start = Timestamp.valueOf(twoMinutesBefore);
        Timestamp end = Timestamp.valueOf(twoMinutesAfter);
        String result = JdbcUtil.queryOrders(start, end);

        Assertions.assertTrue(result.contains("TestProduct1"));
        Assertions.assertTrue(result.contains("TestProduct2"));
    }
    @Test
    public void testInsertOrder() {
        String[] products = {"TestProduct1", "TestProduct2"};
        int[] quantities = {1, 2};

        String result = JdbcUtil.insertOrder(products, quantities);
        Assertions.assertTrue(result.contains("创建订单成功"));
    }
    @Test
    public void testDeleteOrder() {
        String[] products = {"TestProduct1", "TestProduct2"};
        int[] quantities = {1, 2};
        String insertResult = JdbcUtil.insertOrder(products, quantities);
        int orderId = Integer.parseInt(insertResult.split("订单id为`")[1].split("`")[0]);
        String result = JdbcUtil.deleteOrder(orderId);
        assertEquals("删除成功，已将该订单及其关系表删除，订单id: " + orderId, result);
    }
    @Test
    public void testUpdateOrder() {
        String[] products = {"TestProduct1", "TestProduct2"};
        int[] quantities = {1, 2};
        String insertResult = JdbcUtil.insertOrder(products, quantities);

        int orderId = Integer.parseInt(insertResult.split("订单id为`")[1].split("`")[0]);

        String updateResult = JdbcUtil.updateOrder(orderId, "TestProduct1", 3);
        assertEquals("修改成功！", updateResult);
    }
}
