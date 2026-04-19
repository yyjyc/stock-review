package com.stock.review.dto;

import lombok.Data;

@Data
public class PreloadStatus {
    private boolean loading;
    private int total;
    private int current;
    private String message;
    private boolean success;
    
    public static PreloadStatus idle() {
        PreloadStatus status = new PreloadStatus();
        status.setLoading(false);
        status.setTotal(0);
        status.setCurrent(0);
        status.setMessage("未开始");
        status.setSuccess(false);
        return status;
    }
    
    public static PreloadStatus loading(int total, int current) {
        PreloadStatus status = new PreloadStatus();
        status.setLoading(true);
        status.setTotal(total);
        status.setCurrent(current);
        status.setMessage("正在加载...");
        status.setSuccess(false);
        return status;
    }
    
    public static PreloadStatus completed(int total) {
        PreloadStatus status = new PreloadStatus();
        status.setLoading(false);
        status.setTotal(total);
        status.setCurrent(total);
        status.setMessage("加载完成");
        status.setSuccess(true);
        return status;
    }
    
    public static PreloadStatus failed(String errorMsg) {
        PreloadStatus status = new PreloadStatus();
        status.setLoading(false);
        status.setTotal(0);
        status.setCurrent(0);
        status.setMessage("加载失败: " + errorMsg);
        status.setSuccess(false);
        return status;
    }
}
