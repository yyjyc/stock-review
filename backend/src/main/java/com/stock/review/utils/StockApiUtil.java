package com.stock.review.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class StockApiUtil {
    
    private static final String TENCENT_STOCK_API = "https://qt.gtimg.cn/q=";
    private static final String EASTMONEY_SEARCH_API = "https://searchapi.eastmoney.com/api/suggest/get";
    private static final String MAIRUI_STOCK_LIST_API = "http://api.mairuiapi.com/hslt/list/";
    private static final String MAIRUI_LICENCE = "LICENCE-66D8-9F96-0C7F0FBCD073";
    
    public BigDecimal getCurrentPrice(String stockCode) {
        try {
            String fullCode = convertToFullCode(stockCode);
            String url = TENCENT_STOCK_API + fullCode;
            String response = HttpUtil.get(url, 5000);
            
            if (StrUtil.isNotBlank(response)) {
                Pattern pattern = Pattern.compile("v_[^=]+=\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(response);
                if (matcher.find()) {
                    String data = matcher.group(1);
                    String[] parts = data.split("~");
                    if (parts.length > 30) {
                        return new BigDecimal(parts[3]);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取股票实时价格失败: {}", stockCode, e);
        }
        return null;
    }
    
    public Map<String, BigDecimal> batchGetCurrentPrice(List<String> stockCodes) {
        Map<String, BigDecimal> result = new HashMap<>();
        try {
            List<String> fullCodes = new ArrayList<>();
            Map<String, String> codeMapping = new HashMap<>();
            
            for (String code : stockCodes) {
                String fullCode = convertToFullCode(code);
                fullCodes.add(fullCode);
                codeMapping.put(fullCode, code);
            }
            
            String url = TENCENT_STOCK_API + String.join(",", fullCodes);
            String response = HttpUtil.get(url, 10000);
            
            if (StrUtil.isNotBlank(response)) {
                Pattern pattern = Pattern.compile("v_([^=]+)=\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(response);
                while (matcher.find()) {
                    String code = matcher.group(1);
                    String data = matcher.group(2);
                    String[] parts = data.split("~");
                    if (parts.length > 30) {
                        String originalCode = codeMapping.get(code);
                        if (originalCode != null) {
                            result.put(originalCode, new BigDecimal(parts[3]));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("批量获取股票实时价格失败", e);
        }
        return result;
    }
    
    public List<Map<String, Object>> searchStock(String keyword) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String url = EASTMONEY_SEARCH_API + "?input=" + keyword + "&type=14&count=10";
            String response = HttpUtil.get(url, 5000);
            
            if (StrUtil.isNotBlank(response)) {
                JSONObject json = JSON.parseObject(response);
                if (json.containsKey("Data")) {
                    JSONArray data = json.getJSONArray("Data");
                    if (data != null) {
                        for (int i = 0; i < data.size(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            Map<String, Object> stock = new HashMap<>();
                            String code = item.getString("Code");
                            String marketCode = item.getString("MktNum");
                            
                            stock.put("stockCode", code);
                            stock.put("stockName", item.getString("Name"));
                            stock.put("pinyin", item.getString("PingYin"));
                            stock.put("market", marketCode);
                            
                            result.add(stock);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("搜索股票失败: {}", keyword, e);
        }
        return result;
    }
    
    public List<Map<String, Object>> getAllStockList() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String url = MAIRUI_STOCK_LIST_API + MAIRUI_LICENCE;
            log.info("请求股票列表: {}", url);
            
            String response = HttpUtil.get(url, 30000);
            
            if (StrUtil.isBlank(response)) {
                log.error("API返回为空");
                return result;
            }
            
            JSONArray stockArray = JSON.parseArray(response);
            if (stockArray == null || stockArray.isEmpty()) {
                log.error("解析股票列表失败");
                return result;
            }
            
            log.info("获取到 {} 条股票信息", stockArray.size());
            
            for (int i = 0; i < stockArray.size(); i++) {
                JSONObject item = stockArray.getJSONObject(i);
                Map<String, Object> stock = new HashMap<>();
                
                String dm = item.getString("dm");
                String mc = item.getString("mc");
                String jys = item.getString("jys");
                
                String code = dm;
                if (dm != null && dm.contains(".")) {
                    code = dm.split("\\.")[0];
                }
                
                stock.put("stockCode", code);
                stock.put("stockName", mc);
                stock.put("pinyin", "");
                stock.put("firstLetter", "");
                stock.put("market", jys != null ? jys.toLowerCase() : "sz");
                
                result.add(stock);
            }
            
            log.info("共获取 {} 条股票信息", result.size());
            
        } catch (Exception e) {
            log.error("获取股票列表失败", e);
        }
        return result;
    }
    
    private String convertToFullCode(String stockCode) {
        if (stockCode.startsWith("sh") || stockCode.startsWith("sz")) {
            return stockCode;
        }
        
        if (stockCode.startsWith("6")) {
            return "sh" + stockCode;
        } else {
            return "sz" + stockCode;
        }
    }
    
    public String getMarketByCode(String stockCode) {
        if (stockCode.startsWith("6")) {
            return "sh";
        } else {
            return "sz";
        }
    }
}
