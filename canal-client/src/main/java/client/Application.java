package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.alibaba.otter.canal.protocol.Message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Application
 */
@SpringBootApplication
public class Application {

    @Bean
    RedisPush redisPush(){
        return new RedisPush();
    }

    @Bean
    RabbitmqPush rabbitmqPush(){
        return new RabbitmqPush();
    }

    public static String canalHost;
    public static int canalPort;
    public static String canalInstance;
    public static int canalBatchSize; // 每次获取数据数量
    public static int canalSleep; // 无数据时等待时间

    public static String cacheMode;

    public static void main(String[] args) {
        ApplicationContext springApplication = SpringApplication.run(Application.class, args);
        RedisPush redisPush = springApplication.getBean(RedisPush.class);
        RabbitmqPush rabbitmqPush = springApplication.getBean(RabbitmqPush.class);
        System.out.printf("%s_%s_%s",canalHost,canalPort,canalInstance);

        // 创建链接   (example 为server conf/example配置)
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(canalHost, canalPort), canalInstance, "", "");
        //int emptyCount = 0;
        try {
            connector.connect();
            connector.subscribe(".*\\..*");
            connector.rollback();

            System.out.println("canal server connect success!\r\n startup...");

            while (true) {
                Message message = connector.getWithoutAck(canalBatchSize); // 获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                System.out.println("empty count : " + size);
                if (batchId == -1 || size == 0) {
                    try {
                        Thread.sleep(canalSleep); // 等待时间
                    } catch (InterruptedException e) {

                    }
                } else {
                    System.out.printf("message[batchId=%s,size=%s] \n", batchId, size);
                    String[] pushArray = printEntry(message.getEntries());
                    try {
                        if(cacheMode.equals("rabbitmq")){
                            rabbitmqPush.push_mq(pushArray);
                        }else if(cacheMode.equals("redis")){
                            redisPush.push_redis(pushArray);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("push cache error!");
                    }
                }

                connector.ack(batchId); // 提交确认
                // connector.rollback(batchId); // 处理失败, 回滚数据
            }
            //System.out.println("empty too many times, exit");
        } finally {
        	System.out.println("connect error!");
            connector.disconnect();
        }
    }

    private static String[] printEntry(List<Entry> entrys) {
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = df.format(new Date());

        ArrayList<String> dataArray = new ArrayList<String> ();

        //循环每行binlog
        for (Entry entry : entrys) {
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                continue;
            }

            RowChange rowChage = null;
            try {
                rowChage = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),e);
            }

            //单条 binlog sql
            EventType eventType = rowChage.getEventType();

            System.out.println(String.format("================> binlog[%s:%s] , name[%s,%s] , eventType : %s",
                                             entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                                             entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                                             eventType));

            String header_str = "{\"binlog\":\"" + entry.getHeader().getLogfileName()+ ":" + entry.getHeader().getLogfileOffset() + "\"," +
            					"\"db\":\"" + entry.getHeader().getSchemaName() + "\"," +
            					"\"table\":\"" + entry.getHeader().getTableName() + "\",";
            //受影响 数据行
            for (RowData rowData : rowChage.getRowDatasList()) {
            	String row_str = "\"eventType\":\"" + eventType +"\",";
            	String before = "\"\"";
            	String after = "\"\"";

            	//获取字段值
                if (eventType == EventType.DELETE) {
                	after = printColumn(rowData.getBeforeColumnsList());
                } else if (eventType == EventType.INSERT) {
                	after = printColumn(rowData.getAfterColumnsList());
                } else {  //update
                    //System.out.println("-------> before");
                    before = printColumn(rowData.getBeforeColumnsList());
                    //System.out.println("-------> after");
                    after = printColumn(rowData.getAfterColumnsList());
                }

                String row_data = header_str + row_str + "\"before\":" +before + ",\"after\":" + after + ",\"time\":\"" + timeStr +"\"}";
                dataArray.add(row_data);
                //save_data_logs(row_data);
                System.out.println(row_data);
            }
        }

        // ArrayList<String>  TO String[]
        return dataArray.toArray(new String[]{});
    }

    private static String printColumn(List<Column> columns) {
    	Map<String, String> column_map = new HashMap<String, String>();
        for (Column column : columns) {
        	String column_name = column.getName();
        	String column_value = column.getValue();
        	column_map.put(column_name, column_value);
        }
        return JSON.toJSONString(column_map);
    }


    /**
     * @param canalHost the canalHost to set
     */
    @Value("${canal.server.host}")
    public void setCanalHost(String canalHost) {
        Application.canalHost = canalHost;
    }

    /**
     * @param canalPort the canalPort to set
     */
    @Value("${canal.server.port}")
    public void setCanalPort(int canalPort) {
        Application.canalPort = canalPort;
    }

    /**
     * @param canalInstance the canalInstance to set
     */
    @Value("${canal.server.instance}")
    public void setCanalInstance(String canalInstance) {
        Application.canalInstance = canalInstance;
    }

    /**
     * @param canalBatchSize the canalBatchSize to set
     */
    @Value("${canal.batchsize}")
    public void setCanalBatchSize(int canalBatchSize) {
        Application.canalBatchSize = canalBatchSize;
    }

    /**
     * @param canalSleep the canalSleep to set
     */
    @Value("${canal.sleep}")
    public void setCanalSleep(int canalSleep) {
        Application.canalSleep = canalSleep;
    }

    /**
     * @param cacheMode the cacheMode to set
     */
    @Value("${canal.mq}")
    public void setCacheMode(String cacheMode) {
        Application.cacheMode = cacheMode;
    }
}