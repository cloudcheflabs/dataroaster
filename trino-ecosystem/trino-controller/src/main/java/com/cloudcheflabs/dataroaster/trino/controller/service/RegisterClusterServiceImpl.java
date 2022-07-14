package com.cloudcheflabs.dataroaster.trino.controller.service;

import com.cloudcheflabs.dataroaster.trino.controller.api.dao.RegisterClusterDao;
import com.cloudcheflabs.dataroaster.trino.controller.api.service.RegisterClusterService;
import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class RegisterClusterServiceImpl implements RegisterClusterService {

    @Autowired
    @Qualifier("restRegisterClusterDao")
    private RegisterClusterDao registerClusterDao;

    @Override
    public RestResponse createClusterGroup(String restUri, String groupName) {
        return registerClusterDao.createClusterGroup(restUri, groupName);
    }

    @Override
    public RestResponse listClusterGroup(String restUri) {
        return registerClusterDao.listClusterGroup(restUri);
    }

    @Override
    public RestResponse deleteClusterGroup(String restUri, String groupName) {
        return registerClusterDao.deleteClusterGroup(restUri, groupName);
    }

    @Override
    public RestResponse registerCluster(String restUri, String clusterName, String clusterType, String url, boolean activated, String groupName) {
        return registerClusterDao.registerCluster(restUri, clusterName, clusterType, url, activated, groupName);
    }

    @Override
    public RestResponse updateClusterActivated(String restUri, String clusterName, boolean activated) {
        return registerClusterDao.updateClusterActivated(restUri, clusterName, activated);
    }

    @Override
    public RestResponse listClusters(String restUri) {
        return registerClusterDao.listClusters(restUri);
    }

    @Override
    public RestResponse deregisterCluster(String restUri, String clusterName) {
        return registerClusterDao.deregisterCluster(restUri, clusterName);
    }

    @Override
    public RestResponse createUser(String restUri, String user, String password, String groupName) {
        return registerClusterDao.createUser(restUri, user, password, groupName);
    }

    @Override
    public RestResponse updatePassword(String restUri, String user, String password) {
        return registerClusterDao.updatePassword(restUri, user, password);
    }

    @Override
    public RestResponse listUsers(String restUri) {
        return registerClusterDao.listUsers(restUri);
    }

    @Override
    public RestResponse deleteUser(String restUri, String user) {
        return registerClusterDao.deleteUser(restUri, user);
    }
}
