package com.allin.core;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class DubboUtil {
  static InputStream inputStream;
  static OutputStream outputStream;

  static {
    TelnetClient tc = new TelnetClient();
    try {
      String ip =
          String.valueOf(
              CustomizedPropertyUtil.ctxPropertiesMap.getOrDefault("server.ip", "172.28.3.23"));
      int port =
          Integer.parseInt(
              String.valueOf(
                  CustomizedPropertyUtil.ctxPropertiesMap.getOrDefault("server.port", 20892)));
      tc.connect(ip, port);
    } catch (IOException e) {
      e.printStackTrace();
    }

    inputStream = tc.getInputStream();
    outputStream = tc.getOutputStream();
  }

  public static void main(String[] args) {
    JSONObject obj = new JSONObject();
    obj.put("contractId", "12345");
    String str = invoke("com.mljr.bgrk.service.IFightFraudOutService", "receiptContract", obj);
    System.out.println("<>>>>>>" + str);
  }

  public static String invoke(String interfaceName, String method, Object value) {
    writeUtil(
        "invoke " + interfaceName + "." + method + "(" + JSONObject.toJSONString(value) + ")",
        outputStream);
    String str = readUntil(">", inputStream);
    String[] strs = str.split("\r\n");
    if (strs.length > 0) {
      for (String string : strs) {
        if (string.startsWith("{")) {
          str = string;
          break;
        }
      }
    }
    return str;
  }

  public static List<String> lsAllInterface() {
    writeUtil("ls", outputStream);
    String str = readUntil(">", inputStream);
    String[] strs = str.split("\r\n");
    List<String> list = Arrays.asList(strs);
    if (list.size() > 0) {
      return list.subList(0, list.size() - 1);
    }
    return list;
  }

  public static void cdInterface(String interfaceName) {
    if (interfaceName.equals("PROVIDER:")) return;
    interfaceName = interfaceName.split(":")[0];
    interfaceName = interfaceName.substring(interfaceName.lastIndexOf(".") + 1);
    writeUtil("cd " + interfaceName, outputStream);
    System.out.println(readUntil(">", inputStream));
  }

  public static List<String> lsAllMethod(String interfaceName) {
    cdInterface(interfaceName);
    writeUtil("ls", outputStream);
    String str = readUntil(">", inputStream);
    String[] strs = str.split("\r\n");
    List<String> list = Arrays.asList(strs);
    if (list.size() > 0) {
      return list.subList(2, list.size() - 1);
    }
    return list;
  }

  public static void writeUtil(String cmd, OutputStream os) {
    try {
      cmd = cmd + "\n";
      os.write(cmd.getBytes());
      os.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String readUntil(String endFlag, InputStream in) {
    InputStreamReader isr = new InputStreamReader(in);
    char[] charBytes = new char[1024];
    int n;
    boolean flag = false;
    StringBuilder str = new StringBuilder();
    try {
      while ((n = isr.read(charBytes)) != -1) {
        for (int i = 0; i < n; i++) {
          char c = charBytes[i];
          str.append(c);
          // 当拼接的字符串以指定的字符串结尾时,不在继续读
          if (str.toString().endsWith(endFlag)) {
            flag = true;
            break;
          }
        }
        if (flag) {
          break;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return str.toString();
  }
}
