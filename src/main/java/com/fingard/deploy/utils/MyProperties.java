

package com.fingard.deploy.utils;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

/**
 * 榛樿鏄� "parameter.properties"
 * @author yanglc
 *
 */
public final class MyProperties {
	
	private final Logger logger = Logger.getLogger(getClass());

	private static String[] paths = new String[]{};
	
	private static volatile MyProperties p = null;
	
	private final Map<Object, Object> pMap = new ConcurrentHashMap<Object, Object>();
	private String[] originalPaths;
	
	private MyProperties(String[] paths){
		originalPaths = paths;
		init(paths);
	}
	
	/**
	 * 灏唒aths鍒跺畾澶氫釜鏂囦欢鍏ㄩ儴鍔犺浇鍒板唴瀛�
	 * @param paths
	 * @author ylc
	 */
	private void init(String[] paths){
		//synchronized(MyProperties.class){
		for(String path:paths){
			FileReader reader = null;

			Properties properties = new Properties();
			try {
				reader = new FileReader(path);
				properties.load(reader);
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			int originalSize = pMap.size();
			pMap.putAll(properties);
			if(originalSize+properties.size()>pMap.size()){
				logger.warn("璇锋敞鎰廝roperties瀹氫箟key鍊煎彂鐢熶簡閲嶅彔锛乗r\n锛乗r\n锛�");
			}
		}

		//}
	}
	
	public String getProperty(String key){
		//synchronized(MyProperties.class){
			return this.pMap.get(key)==null?null:pMap.get(key).toString();
		//}
	}

	public Integer getProperty2(String key){
		return this.getProperty(key)==null?null:Integer.parseInt(this.getProperty(key));
	}

	public Integer getProperty2(String key, int defaultV){
		return this.getProperty(key)==null?defaultV:Integer.parseInt(this.getProperty(key));
	}
	
	public Boolean getProperty3(String key){
		return this.getProperty(key)==null?null:Boolean.parseBoolean(this.getProperty(key));
	}
	public Boolean getProperty3(String key, boolean defaultV){
		return this.getProperty(key)==null?defaultV:Boolean.parseBoolean(this.getProperty(key));
	}
	
	public MyProperties refresh(){
		pMap.clear();
		this.init(originalPaths);
		return getMyPropertiesInstance();
	}

	public MyProperties refresh(String[] paths){
		pMap.clear();
		this.init(paths);
		return getMyPropertiesInstance();
	}
	/**
	 * 濡傛灉鍐呭瓨涓璏yProperties鏄┖鐨勶紝鍒欏姞杞絧aths鎸囧畾鐨勬枃浠讹紝鍚﹀垯杩斿洖宸叉湁
	 * @param paths
	 * @return
	 * @author ylc
	 */
	public final static MyProperties getMyPropertiesInstance(String[] paths){
		if(paths==null||paths.length==0){
			throw new RuntimeException("娌℃湁灞炴�ф枃浠�");
		}
		if(p==null){
			synchronized(MyProperties.class){
				if(p==null){
					p = new MyProperties(paths);
				}
			}
		}
		return p;
	}
	
	/**
	 * 杩斿洖榛樿鐨凪yProperties.
	 * 榛樿鏄� "parameter.properties", "ui.properties"鏂囦欢
	 * @return
	 * @author ylc
	 */
	public final static MyProperties getMyPropertiesInstance(){
		if(p==null){
			synchronized(MyProperties.class){
				if(p==null){
					p = new MyProperties(paths);
				}
			}
		}
		return p;
	}

	/**
	 * @param args
	 * @author ylc  @version 1.0  2013-7-26
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
