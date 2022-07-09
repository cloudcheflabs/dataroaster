package com.cloudcheflabs.dataroaster.trino.gateway.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public class DBSchemaProcessExecutor {

    private static Logger LOG = LoggerFactory.getLogger(DBSchemaProcessExecutor.class);

    private ProcessBuilder processBuilder;
    private Process process;

    public DBSchemaProcessExecutor() {
        processBuilder = new ProcessBuilder();
    }

    public void destroy() {
        if(process != null) {
            Stream<ProcessHandle> descendants = process.descendants();
            if(descendants != null) {
                descendants.forEach(p -> {
                    Stream<ProcessHandle> descendants2 = p.descendants();
                    if(descendants2 != null) {
                        descendants2.forEach(p2 -> {
                            LOG.info("process with pid [{}] will be killed", p2.pid());
                            kill(p2, p2.pid());
                        });
                    }
                    LOG.info("process with pid [{}] will be killed", p.pid());
                    kill(p, p.pid());
                });
            }
            long pid = process.pid();
            LOG.info("process with pid [{}] will be killed", pid);
            try {
                Runtime.getRuntime().exec("kill -SIGINT " + pid);
                if(process != null) {
                    process.destroy();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LOG.error("current process is null.");
        }
    }

    private void kill(ProcessHandle process, long pid) {
        try {
            Runtime.getRuntime().exec("kill -SIGINT " + pid);
            if(process != null) {
                process.destroy();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doExec(String... cmd) {
        processBuilder.command(cmd);

        try {
            LOG.info("cmd: [{}]", cmd);

            process = processBuilder.start();

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

            // destroy process.
            this.destroy();

        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("cmd: [{}], error: [{}]", cmd, e.getMessage());

            // destroy process.
            this.destroy();
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
