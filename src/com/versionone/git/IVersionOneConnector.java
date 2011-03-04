package com.versionone.git;

import com.versionone.apiclient.ILocalizer;
import com.versionone.apiclient.IMetaModel;
import com.versionone.apiclient.IServices;
import com.versionone.git.configuration.VersionOneSettings;

public interface IVersionOneConnector {
    void connect(VersionOneSettings connectionInfo) throws VersionOneException;
    IMetaModel getMetaModel();
    IServices getServices();
    ILocalizer getLocalizer();
}
