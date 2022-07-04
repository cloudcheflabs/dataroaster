package com.cloudcheflabs.dataroaster.trino.monitor.util;

import com.cloudcheflabs.dataroaster.common.util.JsonUtils;
import com.j256.simplejmx.client.JmxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.util.*;

public class JmxUtils {

    private static Logger LOG = LoggerFactory.getLogger(JmxUtils.class);

    public static String getValue(String host, String port, String objectName, String attribute) {
        return getValue(host, port, objectName, attribute, null);
    }

    public static String getValue(String host, String port, String objectName, String attribute, String compositeKey) {
        try {
            JmxClient client = new JmxClient(host, Integer.valueOf(port));
            Set<ObjectName> names = client.getBeanNames();
            for (ObjectName name : names) {
                // domain.
                String domain = name.getDomain();
                Hashtable<String, String> kv = name.getKeyPropertyList();
                for (String key : kv.keySet()) {
                    // key properties.
                    String value = kv.get(key);
                    String currentObjectName = domain + ":" + key + "=" + value;
                    if(currentObjectName.equals(objectName)) {
                        try {
                            ObjectName tempObjectName = new ObjectName(currentObjectName);
                            MBeanAttributeInfo[] infos = client.getAttributesInfo(tempObjectName);
                            for (MBeanAttributeInfo info : infos) {
                                // attribute.
                                if(attribute.equals(info.getName())) {
                                    Object attributeValue = client.getAttribute(tempObjectName, attribute);
                                    if(compositeKey != null) {
                                        // in case of composite data.
                                        if (attributeValue instanceof CompositeData) {
                                            CompositeData compositeData = (CompositeData) attributeValue;
                                            for (String tempCompositeKey : compositeData.getCompositeType().keySet()) {
                                                if(tempCompositeKey.equals(compositeKey)) {
                                                    return String.valueOf(compositeData.get(compositeKey));
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        return String.valueOf(attributeValue);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LOG.error("jmx attribute error", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public static String printAllMBeanValues(String host, String port) {
        StringBuffer sb = new StringBuffer();
        try {
            JmxClient client = new JmxClient(host, Integer.valueOf(port));
            Set<ObjectName> names = client.getBeanNames();
            for (ObjectName name : names) {
                // domain.
                String domain = name.getDomain();
                sb.append(domain).append("\n");
                Hashtable<String, String> kv = name.getKeyPropertyList();
                for (String key : kv.keySet()) {
                    // key properties.
                    String value = kv.get(key);
                    sb.append("\t").append(key).append("=").append(value).append("\n");
                    ObjectName objectName = new ObjectName(domain + ":" + key + "=" + value);
                    try {
                        MBeanAttributeInfo[] infos = client.getAttributesInfo(objectName);
                        for (MBeanAttributeInfo info : infos) {
                            // attribute.
                            String attribute = info.getName();
                            Object attributeValue = client.getAttribute(objectName, attribute);
                            // in case of composite data.
                            if (attributeValue instanceof CompositeData) {
                                CompositeData compositeData = (CompositeData) attributeValue;
                                sb.append("\t\t").append(attribute).append("\n");
                                for (String compositeKey : compositeData.getCompositeType().keySet()) {
                                    sb.append("\t\t\t").append(compositeKey).append("=").append(compositeData.get(compositeKey)).append("\n");
                                }
                            } else {
                                sb.append("\t\t").append(attribute).append("=").append(attributeValue).append("\n");
                            }
                        }
                    } catch (Exception e) {
                        //System.err.println(e.getMessage());
                    }
                }
            }

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static String listAllMBeanValues(String host, String port) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            JmxClient client = new JmxClient(host, Integer.valueOf(port));
            Set<ObjectName> names = client.getBeanNames();
            for (ObjectName name : names) {
                Map<String, Object> domainMap = new HashMap<>();
                // domain.
                String domain = name.getDomain();
                domainMap.put("domain", domain);

                Hashtable<String, String> kv = name.getKeyPropertyList();
                List<Map<String, Object>> keyPropList = new ArrayList<>();
                for (String key : kv.keySet()) {
                    // key properties.
                    String value = kv.get(key);
                    Map<String, Object> keyPropMap = new HashMap<>();
                    keyPropMap.put("key", key);
                    keyPropMap.put("property", value);

                    ObjectName objectName = new ObjectName(domain + ":" + key + "=" + value);
                    try {
                        MBeanAttributeInfo[] infos = client.getAttributesInfo(objectName);

                        List<Map<String, Object>> attributeList = new ArrayList<>();

                        for (MBeanAttributeInfo info : infos) {
                            Map<String, Object> attributeMap = new HashMap<>();

                            // attribute.
                            String attribute = info.getName();
                            Object attributeValue = client.getAttribute(objectName, attribute);

                            // in case of composite data.
                            if (attributeValue instanceof CompositeData) {
                                List<Map<String, Object>> compositeList = new ArrayList<>();
                                CompositeData compositeData = (CompositeData) attributeValue;
                                for (String compositeKey : compositeData.getCompositeType().keySet()) {
                                    Map<String, Object> compositeMap = new HashMap<>();
                                    compositeMap.put("compositeKey", compositeKey);
                                    compositeMap.put("compositeValue", compositeData.get(compositeKey));
                                    compositeList.add(compositeMap);
                                }

                                attributeMap.put("attribute", attribute);
                                attributeMap.put("attributeValue", compositeList);
                            } else {
                                attributeMap.put("attribute", attribute);
                                attributeMap.put("attributeValue", attributeValue);
                            }
                            attributeList.add(attributeMap);
                        }
                        keyPropMap.put("attributes", attributeList);
                    } catch (Exception e) {
                        //System.err.println(e.getMessage());
                    }
                    keyPropList.add(keyPropMap);
                }
                domainMap.put("keyPropertyList", keyPropList);
                list.add(domainMap);
            }
            return JsonUtils.toJson(list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
