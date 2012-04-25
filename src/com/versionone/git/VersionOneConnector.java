package com.versionone.git;

import com.versionone.apiclient.*;
import com.versionone.git.configuration.ProxySettings;
import com.versionone.git.configuration.VersionOneSettings;
import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

public class VersionOneConnector implements IVersionOneConnector {
    private final String META_URL_SUFFIX = "meta.v1/";
    private final String DATA_URL_SUFFIX = "rest-1.v1/";
    private final String LOCALIZER_URL_SUFFIX = "loc.v1/";

    private IServices services;
    private IMetaModel metaModel;
    private ILocalizer localizer;

    private final Logger LOG = Logger.getLogger("GitIntegration");

    public void connect(VersionOneSettings connectionInfo) throws VersionOneException {
        try {
            String path = connectionInfo.getPath();
            ProxyProvider proxy = getProxy(connectionInfo.getProxySettings());

            V1APIConnector metaConnector = new V1APIConnector(path + META_URL_SUFFIX, proxy);
            metaModel = new MetaModel(metaConnector);

            V1APIConnector localizerConnector = new V1APIConnector(path + LOCALIZER_URL_SUFFIX, connectionInfo.getUserName(),
                    connectionInfo.getPassword(), proxy);
            localizer = new Localizer(localizerConnector);
            V1APIConnector dataConnector;

            dataConnector = getDataConnector(connectionInfo);
            services = new Services(metaModel, dataConnector);
            services.getLoggedIn();
            LOG.info("Connection to VersionOne server established.");
        } catch (Exception ex) {
            String message = "Connection to VersionOne server failed. Please, check address, credentials and proxy settings.";
            LOG.fatal(message);
            throw new VersionOneException(message, ex);
        }
    }

    private V1APIConnector getDataConnector(VersionOneSettings connectionInfo) {
        String path = connectionInfo.getPath();
        ProxyProvider proxy = getProxy(connectionInfo.getProxySettings());
        V1APIConnector dataConnector;
        if (connectionInfo.getIntegratedAuth() != null && connectionInfo.getIntegratedAuth()) {
            dataConnector = new V1APIConnector(path + DATA_URL_SUFFIX, proxy);
        } else {
            dataConnector = new V1APIConnector(path + DATA_URL_SUFFIX, connectionInfo.getUserName(),
                connectionInfo.getPassword(), proxy);
        }
        return dataConnector;
    }

    private ProxyProvider getProxy(ProxySettings settings) {
        if (settings == null || !settings.getUseProxy()) {
            return null;
        }

        try {
            URI uri = new URI(settings.getPath());
            return new ProxyProvider(uri, settings.getUserName(), settings.getPassword());
        } catch (URISyntaxException ex) {
            LOG.error("Failed to create proxy URI", ex);
            return null;
        }
    }

    public IMetaModel getMetaModel() {
        return metaModel;
    }

    public IServices getServices() {
        return services;
    }

    public ILocalizer getLocalizer() {
        return localizer;
    }
}
