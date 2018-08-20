package com.fingard.deploy.utils;

import java.io.*;
import java.util.*;

/**
 * @author zhangym
 * @version 1.0  2018/4/25
 */
public class KeyWordHelper {
    private Properties properties;
    private String fileName;
    public KeyWordHelper(String fileName){
        this.fileName = fileName;
        FileReader reader = null;
        try {
            reader = new FileReader(fileName);
            properties = new Properties();
            properties.load(reader);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(reader != null) {
                try {
                    reader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public Vector<String> getKeyWords(){
        Vector<KeyWord> keyWords = new Vector<>();
        Enumeration en = properties.propertyNames(); //得到配置文件的名字
        while(en.hasMoreElements()) {
            String strKey = (String) en.nextElement();
            keyWords.add(new KeyWord(strKey, Integer.parseInt(properties.getProperty(strKey))));
        }
        Collections.sort(keyWords, new Comparator<KeyWord>() {
            @Override
            public int compare(KeyWord keyWord, KeyWord t1) {
                if(keyWord.getCount() > t1.getCount())
                    return -1;
                else if(keyWord.getCount() == t1.getCount())
                    return 0;
                else
                    return 1;
            }
        });
        Vector<String> keyWordsVector = new Vector<>();
        for(int i = 0; i < keyWords.size(); i++){
            keyWordsVector.add(keyWords.get(i).getKeyWord());
        }
        return keyWordsVector;
    }

    public void addKeyWord(String keyWord){
        try{
            OutputStream out = new FileOutputStream(fileName);
            if(properties.containsKey(keyWord)){
                properties.setProperty(keyWord, "" + (Integer.parseInt(properties.getProperty(keyWord)) + 1));
            }else{
                properties.setProperty(keyWord, "" + 1);
            }
            properties.store(out,"Update  " + keyWord);
            out.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    class KeyWord{
        private String keyWord;
        private int count;
        public KeyWord(String keyWord, int count){
            this.keyWord = keyWord;
            this.count = count;
        }

        public String getKeyWord() {
            return keyWord;
        }

        public void setKeyWord(String keyWord) {
            this.keyWord = keyWord;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

    }

}
