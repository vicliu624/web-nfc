package com.inesanet.web.nfc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Auther: liuweikai
 * @Date: 2019-12-31 21:19
 * @Description:
 */
public class Launcher {

    public static void main(String[] arg) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
        System.out.println("监听8001端口");
        System.out.println("os.name:" + System.getProperty("os.name"));
        server.createContext("/reader", new ReaderHandler());
        server.start();
    }

    static  class ReaderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream inputStream = null;
            BufferedReader reader = null;
            try{
                String requestMethod = exchange.getRequestMethod();
                if (requestMethod != null && requestMethod.equalsIgnoreCase("POST")) {
                    inputStream = exchange.getRequestBody();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String temp;
                    StringBuilder sb = new StringBuilder();
                    while((temp = reader.readLine()) != null) {
                        sb.append(temp);
                    }
                    String attribute = JSON.parseObject(sb.toString()).getString("action");
                    if("connectCard".toLowerCase().equals(attribute.toLowerCase())){
                        Map<String,Object> tips = new HashMap<>();
                        tips.put("tips","打开开通道成功成功");
                        Constant.CARD_READER = CardReader.getInstance();
                        if(Constant.CARD_READER.allCardTerminals().isEmpty()){
                            tips.put("tips","未能获取到读卡器通道");
                            fail(tips,exchange);
                            return;
                        }
                        System.out.println("os.name:" + System.getProperty("os.name"));
                        if(System.getProperty("os.name").toLowerCase().startsWith("win")){
                            System.out.println("selected card terminal:" + Constant.CARD_READER.allCardTerminals().get(0).getName());
                            CardReader.getInstance().selectedCardTerminal(0);
                        } else {
                            System.out.println("selected card terminal:" + Constant.CARD_READER.allCardTerminals().get(1).getName());
                            CardReader.getInstance().selectedCardTerminal(1);
                        }
                        int count = 0;
                        while (true){
                            count ++;
                            if(count > 10){
                                break;
                            }
                            if(CardReader.getInstance().isCardPresent()){
                                if(System.getProperty("os.name").toLowerCase().startsWith("win")){
                                    System.out.println("T=1");
                                    CardReader.getInstance().connect("T=1");
                                } else {
                                    System.out.println("T=0");
                                    CardReader.getInstance().connect("T=0");
                                }
                                break;
                            }
                        }
                        CardReader.getInstance().getBasicChannel();
                        success(tips,exchange);
                        return;
                    } else if("exec".toLowerCase().equals(attribute.toLowerCase())) {
                        try {
                            Map<String,Object> tips = new HashMap<>();
                            Map<String,String> respMap = new LinkedHashMap<>();
                            String cmd = JSON.parseObject(sb.toString()).getString("cmdRequest");
                            LinkedHashMap<Integer,String> cmds = JSONObject.parseObject(cmd, new LinkedHashMap<Integer,String>().getClass());
                            Iterator it = cmds.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry entry = (Map.Entry) it.next();
                                ResponseAPDU responseAPDU = CardReader.getInstance().transmit(new CommandAPDU(DataConvert.hexStringToBytes((String)entry.getValue())));
                                String respData = DataConvert.byteArrayToHexString(responseAPDU.getBytes());
                                System.out.println("执行指令:" + cmd + ",应答:" + respData);
                                respMap.put((String)entry.getKey() ,respData);
                            }
                            tips.put("cmdResponse",respMap);
                            success(tips,exchange);
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Map<String,Object> tips = new HashMap<>();
                            tips.put("tips",e.getMessage());
                            fail(tips,exchange);
                            return;
                        }
                    } else {
                        Map<String,Object> tips = new HashMap<>();
                        tips.put("tips","不支持的action");
                        fail(tips,exchange);
                        return;
                    }
                } else {
                    Map<String,Object> tips = new HashMap<>();
                    tips.put("tips","不支持的method");
                    fail(tips,exchange);
                    return;
                }
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("exception:" + e.getMessage());
                Map<String,Object> tips = new HashMap<>();
                tips.put("tips",e.getMessage());
                fail(tips,exchange);
                return;
            } finally {
                if(reader != null){
                    reader.close();
                }
                if(inputStream != null){
                    inputStream.close();
                }
            }
        }

        private void fail(Map<String,Object> respData,HttpExchange exchange){
            OutputStream responseBody = exchange.getResponseBody();
            try{
                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set("Content-Type", "application/json");
                // 允许跨域
                // 指定允许其他域名访问
                responseHeaders.set("Access-Control-Allow-Origin", "*");
                // 响应类型
                responseHeaders.set("Access-Control-Allow-Methods", "*");
                // 响应头设置
                responseHeaders.set("Access-Control-Allow-Headers",
                        "x-requested-with,content-type");
                exchange.sendResponseHeaders(200, 0);
                String ret = JSON.toJSONString(Result.fail(respData));
                responseBody.write(ret.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                if(responseBody != null){
                    try {
                        responseBody.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        private void success(Map<String,Object> respData,HttpExchange exchange){
            OutputStream responseBody = exchange.getResponseBody();
            try{
                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set("Content-Type", "application/json");
                // 允许跨域
                // 指定允许其他域名访问
                responseHeaders.set("Access-Control-Allow-Origin", "*");
                // 响应类型
                responseHeaders.set("Access-Control-Allow-Methods", "*");
                // 响应头设置
                responseHeaders.set("Access-Control-Allow-Headers",
                        "x-requested-with,content-type");
                exchange.sendResponseHeaders(200, 0);
                String ret = JSON.toJSONString(Result.success(respData));
                responseBody.write(ret.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e){
                e.printStackTrace();
                Map<String,Object> tips = new HashMap<>();
                tips.put("tips",e.getMessage());
                fail(tips,exchange);
            } finally {
                if(responseBody != null){
                    try {
                        responseBody.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }
}
