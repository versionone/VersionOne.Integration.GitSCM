package com.versionone.git;

import com.versionone.apiclient.*;
import org.junit.*;

import java.text.DateFormat;
import java.util.*;

public class ChangeSetWriterTester {
    private Configuration config;
    private IAssetType changeSetType;

    private final String dataUrlSuffix = "rest-1.v1/";
    private final String metaUrlSuffix = "meta.v1/";
    private final String changeSetTypeDef = "ChangeSet";
    private final String referenceAttrDef = "Reference";
    private final String linksAttrDef = "Links.URL";
    private final String nameAttrDef = "Name";
    private final String descriptionAttrDef = "Description";

    private IVersionOneConnector connector;

    @Before
    public void before() throws VersionOneException {
        config = Configuration.getInstance(ConfigurationTester.class.getResource("test_configuration_changesetwriter.xml").getPath());
        Configuration.VersionOneConnection connectionInfo = config.getVersionOneConnection();
        connector = new VersionOneConnector();
        connector.connect(connectionInfo);
        changeSetType = connector.getMetaModel().getAssetType(changeSetTypeDef);    
    }

    @Test
    @Ignore
    public void publishTest() throws VersionOneException, APIException, OidException, ConnectionException {
        List<String> refs = new LinkedList<String>();
        refs.add("B-01112");

        Calendar cal =  GregorianCalendar.getInstance();
        cal.set(2010, 11, 1, 14, 43, 56);
        Date date = cal.getTime();

        ChangeSetInfo changeSet = new ChangeSetInfo("test author", "test message",  new LinkedList<String>(),
                "test_revision", date, refs);

        ChangeSetWriter writer = new ChangeSetWriter(config, connector);
        writer.publish(changeSet);

        Asset[] list = findExistingChangeset(changeSet.getRevision()).getAssets();
        Assert.assertEquals("Affected change sets count is incorrect:", 1, list.length);

        Asset changeSetFromV1 = list[0];

        String changeSetName = String.format("'%1$s' on '%2$s'", changeSet.getAuthor(),
                getFormattedTime(changeSet.getChangeDate()));

        Assert.assertEquals("Name is incorrect:",
                changeSetFromV1.getAttribute(changeSetType.getAttributeDefinition(nameAttrDef)).getValue().toString(),
                changeSetName);

        Assert.assertEquals("Description is incorrect:",
                changeSetFromV1.getAttribute(changeSetType.getAttributeDefinition(descriptionAttrDef)).getValue().toString(),
                changeSet.getMessage());

        Assert.assertEquals("Reference is incorrect:",
                changeSetFromV1.getAttribute(changeSetType.getAttributeDefinition(referenceAttrDef)).getValue().toString(),
                changeSet.getRevision());

        Assert.assertNotNull("Link is null:",
                changeSetFromV1.getAttribute(changeSetType.getAttributeDefinition(linksAttrDef)).getValue().toString());
    }

     private QueryResult findExistingChangeset(String revision) throws OidException, APIException, ConnectionException {

        FilterTerm term = new FilterTerm(changeSetType.getAttributeDefinition(referenceAttrDef));
        term.Equal(revision);

        Query q = new Query(changeSetType);
        q.getSelection().add(changeSetType.getAttributeDefinition(nameAttrDef));
        q.getSelection().add(changeSetType.getAttributeDefinition(descriptionAttrDef));
        q.getSelection().add(changeSetType.getAttributeDefinition(referenceAttrDef));
        q.getSelection().add(changeSetType.getAttributeDefinition(linksAttrDef));
        q.setFilter(term);
        q.setPaging(new Paging(0, 1));

        return connector.getServices().retrieve(q);
    }

    private String getFormattedTime(Date changeDate) {
        DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
        String formattedChangeDate = dateFormatter.format(changeDate);
        return String.format("%1$s UTC%2$tz", formattedChangeDate, changeDate);
    }
    
    @After
    public void after(){
        
    }
}
