package com.fingard.deploy.utils;


import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * @author zhangym
 * @version 1.0  2018/4/23
 */
public class FileUtils {
    public static void copyFileUsingFileChannels(String source, String dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            if(inputChannel != null)
                inputChannel.close();
            if(outputChannel != null)
               outputChannel.close();
        }
    }


    public static String getSelectedFilepath(JFrame main, String title){
        String path = null;
        int result = 0;
        JFileChooser fileChooser = new JFileChooser();
        FileSystemView fsv = FileSystemView.getFileSystemView();  //注意了，这里重要的一句
        System.out.println(fsv.getHomeDirectory());                //得到桌面路径
        fileChooser.setCurrentDirectory(fsv.getHomeDirectory());
        fileChooser.setDialogTitle(title);
        fileChooser.setApproveButtonText("确定");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        result = fileChooser.showOpenDialog(main);
        if (JFileChooser.APPROVE_OPTION == result) {
            path=fileChooser.getSelectedFile().getPath();
            System.out.println("path: " + path);
        }
        return path;
    }

    /**
     *
     * @param main  上下文
     * @param current 默认初始目录
     * @param dialogTitle
     * @param flag 选择类型
     * @return
     */
    public static String getSelectedDirPath(JFrame main, File current, String dialogTitle, int flag){
        String path = null;
        int result = 0;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(current);
        fileChooser.setDialogTitle(dialogTitle);
        fileChooser.setApproveButtonText("确定");
        fileChooser.setFileSelectionMode(flag);
        result = fileChooser.showOpenDialog(main);
        if (JFileChooser.APPROVE_OPTION == result) {
            path = fileChooser.getSelectedFile().getPath();
            System.out.println("path: "+path);
        }
        return path;
    }

    public static void fileOpen(String path){
        try {
            Runtime.getRuntime().exec("notepad " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String filePath){
        StringBuilder content = new StringBuilder("");
        try { //以缓冲区方式读取文件内容
            File file = new File(filePath);
            //FileReader filereader = new FileReader(file, "UTF-8");
            BufferedReader bufferreader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file),"UTF-8"));
            String aline;
            while ((aline = bufferreader.readLine()) != null)
                //按行读取文本，显示在TEXTAREA中
                content.append(aline + "\r\n");
            //filereader.close();
            bufferreader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public static void textAreaToFile(JTextArea fileContent, String filePath){
        BufferedWriter bw = null;
        try {
            OutputStream os = new FileOutputStream(filePath);
            bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            bw.write(fileContent.getText());
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
    public static void stringToFile(String fileContent, String filePath){
        BufferedWriter bw = null;
        try {
            OutputStream os = new FileOutputStream(filePath);
            bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            bw.write(fileContent);
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    //获取指定目录下的parameter配置文件
    public static Vector<String> getParameterFiles(String defaultPath){
        Vector<String> pFilePaths = new Vector<>();
        File file = new File(defaultPath);
        if(file.exists()){
            try {
                File[] files = file.listFiles();
                for(File f : files){
                    String path = f.getAbsolutePath();
                    if(path.endsWith("parameter.properties")){
                        pFilePaths.add(path);
                    }
                }
            }catch (NullPointerException e){
                return null;
            }
        }
        return pFilePaths;
    }

    //采用广度搜索获取指定项目名的绝对路径
    public static String getProjectPath(String startPath, String projectName){
        Queue<File> queue = new LinkedList<>();
        File[] files = new File(startPath).listFiles();
        String filePath = "";
        for(File file : files){
            if(file.isDirectory()){
                filePath = file.getAbsolutePath();
                if(filePath.endsWith(projectName))
                    return filePath;
                else
                    queue.offer(file);
            }
        }
        while(!queue.isEmpty()){
            File[] fileListTemp = queue.poll().listFiles();
            if(fileListTemp==null)
                continue;//遇到无法访问的文件夹跳过
            for (File f : fileListTemp){
                if(f.isDirectory()){
                    filePath = f.getAbsolutePath();
                    if(filePath.endsWith(projectName))
                        return filePath;
                    else
                        queue.offer(f);
                }
            }
        }
        return filePath;
    }


    public static Map<String, String> getParamsByHead(String pFilePath, String headWord){
        Map<String, String> map = new HashMap<>();
        try { //以缓冲区方式读取文件内容
            File file = new File(pFilePath);
            BufferedReader bufferreader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file),"UTF-8"));
            String aline;
            while ((aline = bufferreader.readLine()) != null){
                if(aline.startsWith(headWord)){
                    String[] kv = aline.split("=");
                    map.put(kv[0].substring(kv[0].lastIndexOf(".") + 1), kv[1]);
                }
            }
            bufferreader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static void setProLocalPath(String path){
        ParamProperties paramProperties = new ParamProperties(path);
        if(paramProperties.isKeyExist("local.project.name") && !paramProperties.isKeyExist("hasSet")){
            paramProperties.addComment("项目默认路径，根据项目名列表以及项目本地存储路径在第一次加载时自动生成");
            String proNames = paramProperties.getKey("local.project.name");
            String[] pnList = proNames.split(",");
            int length = pnList.length;
            String[] keys = new String[length];
            String[] values = new String[length];
            String startPath = paramProperties.getKey("svn.local.address");
            for(int i=0; i<length; i++){
                keys[i] = "local.path." + pnList[i];
                values[i] = FileUtils.getProjectPath(startPath, pnList[i]);
                System.out.println(values[i]);
            }
            paramProperties.addKeyValues(keys, values);
            paramProperties.addComment("第一次自动生成项目默认路径标识，如需重新自动更新需删除该字段");
            paramProperties.addKeyValue("hasSet", "true");
        }
    }


    public static void main(String[] args){
        /*Map<String, String> map = getParamsByHead("d:\\config\\rhf-parameter.properties","deploy.path");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            File file = new File(value);
                System.out.println("key=" + key + " value=" + value);
        }
        setProLocalPath("d:\\config\\rhf-parameter.properties");*/


        StringBuilder stringBuilder = new StringBuilder();
        FileUtils obj = new FileUtils();
        File file = new File("D:/123.xls");
        List excelList = obj.readExcel(file);
        System.out.println("list中的数据打印出来");
        for (int i = 0; i < excelList.size(); i++) {
            List list = (List) excelList.get(i);
            int a = 'X' - i/10;
            char c = (char)a;
            StringBuilder s = new StringBuilder().append(c).append(i%10);
            stringBuilder.append("update customer set enterpriseshortname='" + s.toString()+"' where enterpriseshortname is null and enterprisecode='"+
            list.get(1) + "' and enterprisename='" + list.get(0) + "';" + "\r\n");

        }
        System.out.print(stringBuilder.toString());
    }
    public List readExcel(File file) {
        try {
            // 创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            // jxl提供的Workbook类
            Workbook wb = Workbook.getWorkbook(is);
            // Excel的页签数量
            int sheet_size = wb.getNumberOfSheets();
            for (int index = 0; index < sheet_size; index++) {
                List<List> outerList=new ArrayList<List>();
                // 每个页签创建一个Sheet对象
                Sheet sheet = wb.getSheet(index);
                // sheet.getRows()返回该页的总行数
                for (int i = 0; i < sheet.getRows(); i++) {
                    List innerList=new ArrayList();
                    // sheet.getColumns()返回该页的总列数
                    for (int j = 0; j < sheet.getColumns(); j++) {
                        String cellinfo = sheet.getCell(j, i).getContents();
                        if(cellinfo.isEmpty()){
                            continue;
                        }
                        innerList.add(cellinfo);
                        System.out.print(cellinfo);
                    }
                    outerList.add(i, innerList);
                    System.out.println();
                }
                return outerList;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
