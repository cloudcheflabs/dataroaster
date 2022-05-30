package com.cloudcheflabs.dataroaster.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessExecutor {

    private static Logger LOG = LoggerFactory.getLogger(ProcessExecutor.class);

    public static void doExec(String... cmd) {

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(cmd);

        boolean exceptionThrown = false;

        try {
            LOG.info("cmd: [{}]", cmd);

            Process process = processBuilder.start();

            // any error message.
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "OUTPUT");

            // any output.
            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT2");

            // kick them off
            errorGobbler.start();
            outputGobbler.start();

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                LOG.info("cmd [{}], Success!", cmd);
            } else {
                LOG.info("abnormal exit, cmd [{}]", cmd);
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("cmd: [{}], error: [{}]", cmd, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }



    private static class StreamGobbler extends Thread
    {
        InputStream is;
        String type;

        StreamGobbler(InputStream is, String type)
        {
            this.is = is;
            this.type = type;
        }

        public void run()
        {
            try
            {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ( (line = br.readLine()) != null)
                    LOG.info(type + ">" + line);
            } catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }
}
