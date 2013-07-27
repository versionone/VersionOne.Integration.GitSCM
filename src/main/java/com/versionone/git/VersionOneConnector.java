package com.versionone.git;

import com.versionone.apiclient.*;
import com.versionone.git.configuration.ProxySettings;
import com.versionone.git.configuration.VersionOneConnection;
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

    public void connect(VersionOneConnection connection) throws VersionOneException {
        try {
            String path = connection.getPath();
            ProxyProvider proxy = getProxy(connection.getProxySettings());

            V1APIConnector metaConnector = new V1APIConnector(path + META_URL_SUFFIX, proxy);
            metaModel = new MetaModel(metaConnector);

            V1APIConnector localizerConnector = new V1APIConnector(path + LOCALIZER_URL_SUFFIX, connection.getUserName(),
                    connection.getPassword(), proxy);
            localizer = new Localizer(localizerConnector);
            V1APIConnector dataConnector;

            dataConnector = getDataConnector(connection);
            services = new Services(metaModel, dataConnector);
            services.getLoggedIn();
            LOG.info("Connection to VersionOne server established successfully");
        } catch (Exception ex) {
            String message = "Connection to VersionOne server failed. Please check the address, credentials and proxy settings";
            LOG.fatal(message);
            throw new VersionOneException(message, ex);
        }
    }

    private V1APIConnector getDataConnector(VersionOneConnection connection) {
        String path = connection.getPath();
        ProxyProvider proxy = getProxy(connection.getProxySettings());
        V1APIConnector dataConnector;
        if (connection.getIntegratedAuth() != null && connection.getIntegratedAuth()) {
            LOG.info("Connecting to VersionOne using AD integrated authentication...");
            dataConnector = new V1APIConnector(path + DATA_URL_SUFFIX, proxy);
        } else {
            LOG.info(String.format("Connecting to VersionOne as '%s'...", connection.getUserName()));
            dataConnector = new V1APIConnector(path + DATA_URL_SUFFIX, connection.getUserName(),
                connection.getPassword(), proxy);
        }
        return dataConnector;
    }

    private ProxyProvider getProxy(ProxySettings settings) {
        if (settings == null || !settings.getUseProxy()) {
            return null;
        }

        try {
            LOG.info("Connecting to VersionOne via proxy server " + settings.getPath());
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
