package com.cloudcheflabs.dataroaster.trino.controller.util;

import com.cedarsoftware.util.io.JsonWriter;
import com.cloudcheflabs.dataroaster.common.util.TemplateUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TemplateUtilsTestRunner {

    @Test
    public void replace() throws Exception{
        Map<String, Object> kv = new HashMap<>();
        kv.put("customResourceNamespace", "trino-controller");
        String nginxCrString =
                TemplateUtils.replace("/templates/cr/nginx-ingress-controller.yaml", true, kv);
        System.out.printf("nginxCrString: \n%s", nginxCrString);
    }
}
