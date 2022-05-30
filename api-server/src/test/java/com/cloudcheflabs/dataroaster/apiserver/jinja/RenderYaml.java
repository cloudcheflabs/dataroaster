package com.cloudcheflabs.dataroaster.apiserver.jinja;

import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import com.hubspot.jinjava.Jinjava;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:application-test.properties"
})
public class RenderYaml {
    private static Logger LOG = LoggerFactory.getLogger(RenderYaml.class);

    @Test
    public void renderLoop() throws Exception {
        String yaml = FileUtils.fileToString("/templates/pod-log-monitoring/1.0.0/logstash/dataroaster-values.yaml", true);

        Map<String, Object> kv = new HashMap<>();
        kv.put("logstashNamespace", "dataroaster-logstash");

        List<String> elasticsearchHosts = new ArrayList<>();
        elasticsearchHosts.add("192.168.10.10:9200");
        elasticsearchHosts.add("192.168.10.134:9200");
        elasticsearchHosts.add("192.168.10.145:9200");

        kv.put("elasticsearchHosts", elasticsearchHosts);


        Jinjava jinjava = new Jinjava();
        String renderedYaml = jinjava.render(yaml, kv);
        LOG.info("renderedYaml: \n{}", renderedYaml);
    }
}
