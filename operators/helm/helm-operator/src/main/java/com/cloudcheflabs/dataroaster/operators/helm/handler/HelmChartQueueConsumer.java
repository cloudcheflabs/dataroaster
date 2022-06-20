package com.cloudcheflabs.dataroaster.operators.helm.handler;

import com.cloudcheflabs.dataroaster.operators.helm.crd.HelmChart;
import com.cloudcheflabs.dataroaster.operators.helm.util.YamlUtils;
import io.fabric8.kubernetes.client.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class HelmChartQueueConsumer implements Runnable{

    private static Logger LOG = LoggerFactory.getLogger(HelmChartQueueConsumer.class);

    private BlockingQueue<HelmChartActionEvent> queue;
    private ActionHandler<HelmChart> actionHandler;

    public HelmChartQueueConsumer(BlockingQueue<HelmChartActionEvent> queue, ActionHandler<HelmChart> actionHandler) {
        this.queue = queue;
        this.actionHandler = actionHandler;
    }

    @Override
    public void run() {
        while (true) {
            try {
                HelmChartActionEvent actionEvent = queue.take();
                if(actionEvent != null) {
                    Watcher.Action action = actionEvent.getAction();
                    HelmChart helmChart = actionEvent.getHelmChart();
                    if(action.name().equals("ADDED")) {
                        LOG.info("add helm chart: \n{}", YamlUtils.objectToYaml(helmChart));
                        actionHandler.create(helmChart);
                        LOG.info("[{}] created...", helmChart.getMetadata().getName());
                    } else if(action.name().equals("MODIFIED")) {
                        LOG.info("upgrade helm chart: \n{}", YamlUtils.objectToYaml(helmChart));
                        actionHandler.upgrade(helmChart);
                    }
                    else if(action.name().equals("DELETED")) {
                        LOG.info("delete helm chart: \n{}", YamlUtils.objectToYaml(helmChart));
                        actionHandler.destroy(helmChart);
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
