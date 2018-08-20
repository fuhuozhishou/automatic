package com.fingard.deploy.utils;
import java.io.File;
import java.util.*;
/**
 * @author zhangym
 * @version 1.0  2018/4/20
 */
public class SearchFile {

    public static Vector<String> getFiles(String key1, String key2){
        System.out.println(key1);
        Vector<String> destPath = searchFile(key1, key2);
        if(destPath != null && destPath.size() > 0){
            System.out.println("你要找的文件的目录如下：");
            for(String filePath : destPath){
                System.out.println(filePath);
            }
        }else{
            System.out.println("没有找到或您输入有误");
        }
        return destPath;
    }
     private static Vector<String> searchFile(String path,String fileName){
        if(path != null && fileName != null){
            File file = new File(path);
            Vector<String> filePaths = new Vector<>();
            getFilesList(file, fileName, filePaths);
            return filePaths;
        }
        return null;
    }

     private static void getFilesList(File file,String fileName,Vector<String> filePaths){
        if(file.exists()){
            if(file.isDirectory()){
                File[] files = file.listFiles();
                if(files!=null&&files.length>0){
                    for(File f:files){
                        getFilesList(f,fileName,filePaths);
                    }
                }
            }else if(file.isFile()){
                if(file.getName().contains(fileName)){
                    filePaths.add(file.getPath());
                }
            }
        }
    }


    private static void bfsSearchFile(String path,String regex,boolean isDisplyDir,boolean isDisplayFile)
    {
        if(!(isDisplayFile||isDisplyDir))
        {
            throw new IllegalArgumentException("isDisplyDir和isDisplayFile中至少要有一个为true");
        }
        Queue<File> queue=new LinkedList<>();
        File[] fs=new File(path).listFiles();
        //遍历第一层
        for(File f:fs)
        {
            //把第一层文件夹加入队列
            if(f.isDirectory())
            {
                queue.offer(f);
            }
            else
            {
                if(f.getName().matches(regex)&&isDisplayFile)
                {
                    System.out.println(f.getName());
                }
            }
        }
        //逐层搜索下去
        while (!queue.isEmpty()) {
            File fileTemp=queue.poll();//从队列头取一个元素
            if(isDisplyDir)
            {
                if(fileTemp.getName().matches(regex))
                {
                    System.out.println(fileTemp.getAbsolutePath());
                }
            }

            File[] fileListTemp=fileTemp.listFiles();
            if(fileListTemp==null)
                continue;//遇到无法访问的文件夹跳过
            for(File f:fileListTemp)
            {
                if(f.isDirectory())
                {
                    queue.offer(f);////从队列尾插入一个元素
                }
                else
                {
                    if(f.getName().matches(regex)&&isDisplayFile)
                    {
                        System.out.println(f.getName());
                    }
                }
            }

        }
    }

    public static void main(String[] args) {


         int count = 0;
         int num = 0;
         for(int i = 0; i<=100; i++){
             num +=i;
             count = ++count;
         }
        System.out.println(num*count);
         /*for(int i = 0; i < 95000; i++){
             String s = "e://qwert//f" + i + ".txt";
             System.out.println(FileUtils.readFile(s));
             *//*File file = new File(s);
             try{
                 file.createNewFile();
             } catch (Exception e ){
                 e.printStackTrace();
             }
             StringBuilder stringBuilder = new StringBuilder();
             for(int j = 0; j < i; j++){
                 stringBuilder.append("sdsdasfsf").append(i).append("fdsfsfsdfs").append(i);
             }
             FileUtils.stringToFile(stringBuilder.toString(), s);

             try {
                 Thread.sleep(200);
             } catch (Exception e){
                 e.printStackTrace();
             }*//*

         }*/


    }
}
