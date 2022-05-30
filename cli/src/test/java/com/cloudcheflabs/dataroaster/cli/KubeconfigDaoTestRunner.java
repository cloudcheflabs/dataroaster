package com.cloudcheflabs.dataroaster.cli;

import com.cloudcheflabs.dataroaster.cli.api.dao.KubeconfigDao;
import com.cloudcheflabs.dataroaster.cli.config.DataRoasterConfig;
import com.cloudcheflabs.dataroaster.cli.config.SpringContextSingleton;
import com.cloudcheflabs.dataroaster.cli.domain.ConfigProps;
import com.cloudcheflabs.dataroaster.cli.domain.RestResponse;
import com.cloudcheflabs.dataroaster.common.util.FileUtils;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.File;

public class KubeconfigDaoTestRunner {

    @Test
    public void createKubeconfig() throws Exception {
        long id = Long.valueOf(System.getProperty("id"));

        ConfigProps configProps = DataRoasterConfig.getConfigProps();

        File kubeconfigFile = new File(System.getProperty("user.home") + "/.kube/config");
        String kubeconfigPath = kubeconfigFile.getAbsolutePath();
        String kubeconfig = FileUtils.fileToString(kubeconfigPath, false);

        System.out.printf("kubeconfig: \n%s\n", kubeconfig);

        ApplicationContext applicationContext = SpringContextSingleton.getInstance();
        KubeconfigDao kubeconfigDao = applicationContext.getBean(KubeconfigDao.class);
        RestResponse restResponse = kubeconfigDao.createKubeconfig(configProps, id, kubeconfig);
    }
}
