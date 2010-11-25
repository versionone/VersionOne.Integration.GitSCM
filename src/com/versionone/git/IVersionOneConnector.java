package com.versionone.git;

import com.versionone.apiclient.ILocalizer;
import com.versionone.apiclient.IMetaModel;
import com.versionone.apiclient.IServices;

public interface IVersionOneConnector {

    void connect(Configuration.VersionOneConnection connectionInfo) throws VersionOneException;
    IMetaModel getMetaModel();
    IServices getServices();
    ILocalizer getLocalizer();
}
