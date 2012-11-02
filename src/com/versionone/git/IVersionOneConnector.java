package com.versionone.git;

import com.versionone.apiclient.ILocalizer;
import com.versionone.apiclient.IMetaModel;
import com.versionone.apiclient.IServices;
import com.versionone.git.configuration.VersionOneConnection;

public interface IVersionOneConnector {
    void connect(VersionOneConnection connection) throws VersionOneException;
    IMetaModel getMetaModel();
    IServices getServices();
    ILocalizer getLocalizer();
}
