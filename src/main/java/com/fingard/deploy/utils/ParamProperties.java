package com.fingard.deploy.utils;

import java.io.*;

/**
 * @author zhangym
 * @version 1.0  2018/5/11
 */
public class ParamProperties {
    private String filePath;
    private SafeProperties properties;

    public ParamProperties(String filePath){
        this.filePath = filePath;
        //FileReader reader = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            //reader = new FileReader(filePath);
            properties = new SafeProperties();
            //properties.load(reader);
            properties.load(fileInputStream);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(fileInputStream != null) {
                try {
                    fileInputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
    public boolean isKeyExist(String key){
        return properties.containsKey(key);
    }

    public String getKey(String key){
        return properties.getProperty(key);
    }
    public void addKeyValue(String key, String value){
        try{
            OutputStream out = new FileOutputStream(filePath);
            properties.setProperty(key, value);
            properties.store(out,null);
            out.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void addKeyValues(String[] keys, String[] values){
        try{
            OutputStream out = new FileOutputStream(filePath);
            for(int i = 0; i < keys.length; i ++){
                properties.setProperty(keys[i], values[i]);
                System.out.println(values[i]);
            }
            properties.store(out, null);
            out.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void addComment(String comment){
        properties.addComment(comment);
    }
}
