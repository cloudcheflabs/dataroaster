package com.cloudcheflabs.dataroaster.trino.controller.api.service;

import com.cloudcheflabs.dataroaster.trino.controller.domain.RestResponse;

public interface RegisterClusterService {
    RestResponse createClusterGroup(String restUri, String groupName);
    RestResponse listClusterGroup(String restUri);
    RestResponse deleteClusterGroup(String restUri, String groupName);

    RestResponse registerCluster(String restUri, String clusterName, String clusterType, String url, boolean activated, String groupName);
    RestResponse updateClusterActivated(String restUri, String clusterName, boolean activated);
    RestResponse listClusters(String restUri);
    RestResponse deregisterCluster(String restUri, String clusterName);

    RestResponse createUser(String restUri, String user, String password, String groupName);
    RestResponse updatePassword(String restUri, String user, String password);
    RestResponse listUsers(String restUri);
    RestResponse deleteUser(String restUri, String user);
}
