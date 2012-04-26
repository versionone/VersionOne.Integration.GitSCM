package com.versionone.git;


import com.versionone.Oid;
import com.versionone.apiclient.APIException;
import com.versionone.apiclient.Asset;
import com.versionone.apiclient.ConnectionException;
import com.versionone.apiclient.IAssetType;
import com.versionone.apiclient.IAttributeDefinition;
import com.versionone.apiclient.IMetaModel;
import com.versionone.apiclient.IServices;
import com.versionone.apiclient.OidException;
import com.versionone.apiclient.Query;
import com.versionone.apiclient.QueryResult;
import com.versionone.apiclient.V1Exception;
import com.versionone.git.configuration.Configuration;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ChangeSetWriterUnitTester {

    //private JUnit4Mockery context;
    private Mockery mockery = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    private IGitConnector gitConnectorMock;
    private IServices v1ServiceMock;
    private IMetaModel v1MetaModel;
    private VersionOneConnector v1ConnectorMock;
    private QueryResult v1QueryResultMock;

    @Before
    public void before() {
        //context = new JUnit4Mockery();
        gitConnectorMock = mockery.mock(IGitConnector.class);
        v1ServiceMock = mockery.mock(IServices.class);
        v1ConnectorMock = mockery.mock(VersionOneConnector.class);
        v1QueryResultMock = mockery.mock(QueryResult.class);
        v1MetaModel = mockery.mock(IMetaModel.class);
    }

    @Test
    public void publish() throws VersionOneException, V1Exception, APIException, ConnectionException {
        Configuration config = Configuration.getInstance(ConfigurationTester.class.getResource("test_configuration.xml").getPath());
        ChangeSetWriter writer = new ChangeSetWriter(config, v1ConnectorMock);
        List<String> references = Arrays.asList("D-00001");
        ChangeSetInfo changeSetInfo = new ChangeSetInfo("author", "message", new ArrayList<String>(), "123", new Date(),  references);

        final Asset affectedWorkitem = mockery.mock(Asset.class, "Asset 1");
        final Oid Oid1 = mockery.mock(Oid.class, "Oid for Asset 1");
        
        final IAssetType assetType = mockery.mock(IAssetType.class);
        final IAttributeDefinition attributeDefinitions = mockery.mock(IAttributeDefinition.class);
        final Asset[] affectedWorkitems = new Asset[] {affectedWorkitem};
        final Asset newAsset = mockery.mock(Asset.class, "New Asset");
        final Oid Oid2 = mockery.mock(Oid.class, "Oid for New Asset");

        mockery.checking(new Expectations() {{
            allowing(v1ConnectorMock).getMetaModel();
                will(returnValue(v1MetaModel));
            allowing(v1MetaModel).getAssetType(with(any(String.class)));
                will(returnValue(assetType));
            allowing(assetType).getAttributeDefinition(with(any(String.class)));
                will(returnValue(attributeDefinitions));
            allowing(v1MetaModel).getAttributeDefinition(with(any(String.class)));
                will(returnValue(attributeDefinitions));

            oneOf(v1ConnectorMock).getServices();
                will(returnValue(v1ServiceMock));
            oneOf(v1ServiceMock).retrieve(with(aNonNull(Query.class)));
                will(returnValue(v1QueryResultMock));
            oneOf(v1QueryResultMock).getAssets();
                will(returnValue(affectedWorkitems));
            oneOf(affectedWorkitem).getOid();
                will(returnValue(Oid1));

            oneOf(v1ConnectorMock).getServices();
                will(returnValue(v1ServiceMock));
            oneOf(v1ServiceMock).retrieve(with(aNonNull(Query.class)));
                will(returnValue(v1QueryResultMock));
            oneOf(v1QueryResultMock).getAssets();
                will(returnValue(new Asset[0]));

            //Create new ChangeSet
            oneOf(v1ConnectorMock).getServices();
                will(returnValue(v1ServiceMock));
            oneOf(v1ServiceMock).createNew(with(aNonNull(IAssetType.class)), with(aNonNull(Oid.class)));
                will(returnValue(newAsset));
            allowing(newAsset).setAttributeValue(with(aNonNull(IAttributeDefinition.class)), with(aNonNull(String.class)));
            allowing(newAsset).addAttributeValue(with(aNonNull(IAttributeDefinition.class)), with(aNonNull(String.class)));

            oneOf(v1ConnectorMock).getServices();
                will(returnValue(v1ServiceMock));
            oneOf(v1ServiceMock).save(with(aNonNull(Asset.class)), with(aNonNull(String.class)));

            allowing(newAsset).getAttribute(with(aNonNull(IAttributeDefinition.class)));
                will(returnValue(null));

            //Create Link
            oneOf(v1ConnectorMock).getServices();
                will(returnValue(v1ServiceMock));
            oneOf(newAsset).getOid();
                will(returnValue(Oid2));
            oneOf(Oid2).getMomentless();
                will(returnValue(Oid2));
            oneOf(v1ServiceMock).createNew(with(aNonNull(IAssetType.class)), with(aNonNull(Oid.class)));
                will(returnValue(newAsset));
            oneOf(v1ConnectorMock).getServices();
                will(returnValue(v1ServiceMock));
            oneOf(v1ServiceMock).save(with(aNonNull(Asset.class)), with(aNonNull(String.class)));

            oneOf(v1ConnectorMock).getServices();
                will(returnValue(v1ServiceMock));
            oneOf(newAsset).getOid();
                will(returnValue(Oid2));
        }});

        writer.publish(changeSetInfo);
    }
}
