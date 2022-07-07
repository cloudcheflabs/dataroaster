package com.cloudcheflabs.dataroaster.operators.trino.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class RegExpTestRunner {

    private static Logger LOG = LoggerFactory.getLogger(RegExpTestRunner.class);

    @Test
    public void regExp() throws Exception {
        String str = "" +
                "          -server\n" +
                "          -Xmx8G\n" +
                "          -XX:+UseG1GC\n" +
                "          -XX:G1HeapRegionSize=32M\n" +
                "          -XX:+UseGCOverheadLimit\n" +
                "          -XX:+ExplicitGCInvokesConcurrent\n" +
                "          -XX:+HeapDumpOnOutOfMemoryError\n" +
                "          -XX:+ExitOnOutOfMemoryError\n" +
                "          -Djdk.attach.allowAttachSelf=true\n" +
                "          -XX:-UseBiasedLocking\n" +
                "          -XX:ReservedCodeCacheSize=512M\n" +
                "          -XX:PerMethodRecompilationCutoff=10000\n" +
                "          -XX:PerBytecodeRecompilationCutoff=10000\n" +
                "          -Djdk.nio.maxCachedBufferSize=2000000\n" +
                "          -XX:+UnlockDiagnosticVMOptions\n" +
                "          -XX:+UseAESCTRIntrinsics\n" +
                "          -javaagent:/home/opc/jmx-exporter/jmx_prometheus_javaagent-0.17.0.jar=9090:/home/opc/jmx-exporter/config.yaml\n" +
                "          -Dcom.sun.management.jmxremote\n" +
                "          -Dcom.sun.management.jmxremote.authenticate=false\n" +
                "          -Dcom.sun.management.jmxremote.ssl=false\n" +
                "          -Dcom.sun.management.jmxremote.rmi.port=9081";

        int jmxExporterPort = -1;

        for(String s : str.lines().collect(Collectors.toList())) {
            if(s.contains("-javaagent")) {
                String[] lineTokens = s.split("=");
                if(lineTokens.length == 2) {
                    String portPart = lineTokens[1];
                    jmxExporterPort = Integer.valueOf(portPart.split(":")[0]);
                    LOG.info("jmxExporterPort: {}", jmxExporterPort);
                }
            }
        }
    }
}
