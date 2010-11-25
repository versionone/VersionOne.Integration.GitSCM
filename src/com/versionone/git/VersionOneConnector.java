package com.versionone.git;

import com.versionone.apiclient.*;
import org.apache.log4j.Logger;

public class VersionOneConnector implements IVersionOneConnector {

    private final String META_URL_SUFFIX = "meta.v1/";
    private final String DATA_URL_SUFFIX = "rest-1.v1/";
    private final String LOCALIZER_URL_SUFFIX = "loc.v1/";

    private IServices services;
    private IMetaModel metaModel;
    private ILocalizer localizer;

    private final Logger LOG = Logger.getLogger("GitIntegration");

    public void connect(Configuration.VersionOneConnection connectionInfo) throws VersionOneException {
        try {
            V1APIConnector metaConnector = new V1APIConnector(connectionInfo.getPath() + META_URL_SUFFIX,
                    connectionInfo.getUserName(), connectionInfo.getPassword());
            metaModel = new MetaModel(metaConnector);

            V1APIConnector localizerConnector = new V1APIConnector(connectionInfo.getPath() + LOCALIZER_URL_SUFFIX,
                    connectionInfo.getUserName(), connectionInfo.getPassword());
            localizer = new Localizer(localizerConnector);

            V1APIConnector dataConnector = new V1APIConnector(connectionInfo.getPath() + DATA_URL_SUFFIX,
                    connectionInfo.getUserName(), connectionInfo.getPassword());
            services = new Services(metaModel, dataConnector);
        } catch (Exception ex) {
            String message = "Connection error: " + ex.getMessage();
            LOG.fatal(message);
            throw new VersionOneException(message, ex);
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
