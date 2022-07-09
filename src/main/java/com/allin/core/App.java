package com.allin.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class App {
  private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    initParam(args);
    MainFrame.run();
  }

  private static void initParam(String[] args) {
    LOGGER.info("开始加载初始化参数{}", args);
    InputStream is = null;
    if (args != null && args.length > 0) {
      LOGGER.info("取自定义配置文件");
      try {
        is = new FileInputStream(new File(args[0]).getAbsolutePath());
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } else {
      LOGGER.info("取默认配置文件");
      is = App.class.getClassLoader().getResourceAsStream("config.properties");
    }

    Properties prop = new Properties();
    try {
      prop.load(is);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Map<String, Object> map = CustomizedPropertyUtil.ctxPropertiesMap;
    for (Object k : prop.keySet()) {
      Object v = prop.get(k);
      String key = String.valueOf(k), value = String.valueOf(v);
      map.put(key, value);
    }
    LOGGER.info("加载初始化参数完成,{}", CustomizedPropertyUtil.ctxPropertiesMap);
  }
}
