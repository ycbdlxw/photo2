package com.ycbd.photoservice.tools;

import lombok.Data;

/**
 * @author lxw
 * com.ycbd.common.Interface
 * @create 2022/5/18 14:16
 * @description
 */
@Data
public class ResultData<T> {
    /** 结果状态 ,具体状态码参见ResultData.java*/
    private int status;
    private int code;
    private T result;
    private String message;
    private T data;
    private long timestamp ;


    public ResultData(){
        this.timestamp = System.currentTimeMillis();
    }


    public static <T> ResultData<T> success(T data) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setCode(200);//ReturnCode.RC100.getCode());
        resultData.setMessage("203");//ReturnCode.RC100.getMessage());
        resultData.setResult(data);
       
        return resultData;
    }

    public static <T> ResultData<T> fail(int code, String message) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setCode(code);
        resultData.setMessage(message);
        return resultData;
    }

}

