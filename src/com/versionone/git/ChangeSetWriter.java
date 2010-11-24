package com.versionone.git;

import com.versionone.Oid;
import com.versionone.apiclient.*;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.util.*;

public class ChangeSetWriter implements IChangeSetWriter {
    private final String metaUrlSuffix = "meta.v1/";
    private final String dataUrlSuffix = "rest-1.v1/";
    private final String localUrlSuffix = "loc.v1/";

    private final String changeSetTypeDef = "ChangeSet";
    private final String linkTypeDef = "Link";
    private final String primaryWorkitemTypeDef = "PrimaryWorkitem";

    private final String nameAttrDef = "Name";
    private final String descriptionAttrDef = "Description";
    private final String referenceAttrDef = "Reference";
    private final String urlAttrDef = "URL";
    private final String onMenuAttrDef = "OnMenu";
    private final String primaryWorkitemsAttrDef = "PrimaryWorkitems";
    private final String linksAttrDef = "Links.URL";
    private final String childrenMeAndDownAttrDef = "PrimaryWorkitem.ChildrenMeAndDown";
    private final String storyName = "Plural'Story";
    private final String defectName = "Plural'Defect";

    private IServices services;
    private IMetaModel metaModel;
    private ILocalizer localizer;

    private final Configuration.VersionOneConnection connectionInfo;
    private final Configuration.Link linkInfo;
    private final String referenceAttribute;
    private final Boolean isAlwaysCreate;
    private final String changeComment;
    private final Logger LOG = Logger.getLogger("GitIntegration");

    private IAssetType getChangeSetType(){
        return metaModel.getAssetType(changeSetTypeDef);
    }

    private IAssetType getPrimaryWorkitemType(){
        return metaModel.getAssetType(primaryWorkitemTypeDef);
    }

    private IAttributeDefinition getPrimaryWorkitemReference(){
        return metaModel.getAttributeDefinition(childrenMeAndDownAttrDef + "." + referenceAttribute);
    }
    
    private IAssetType getLinktType(){
        return metaModel.getAssetType(linkTypeDef);
    }

    private String getLocalizedString(String name){
        return localizer.resolve(name);
    }

    public ChangeSetWriter (Configuration config){
        connectionInfo = config.getVersionOneConnection();
        linkInfo = config.getLink();
        referenceAttribute = config.getReferenceAttribute();
        isAlwaysCreate = config.isAlwaysCreate();
        changeComment = config.getChangeComment();
    }

    public void connect() throws VersionOneException {
        try {
            V1APIConnector metaConnector = new V1APIConnector(connectionInfo.getPath() + metaUrlSuffix,
                    connectionInfo.getUserName(), connectionInfo.getPassword());
            metaModel = new MetaModel(metaConnector);

            V1APIConnector localizerConnector = new V1APIConnector(connectionInfo.getPath() + localUrlSuffix,
                    connectionInfo.getUserName(), connectionInfo.getPassword());
            localizer = new Localizer(localizerConnector);

            V1APIConnector dataConnector = new V1APIConnector(connectionInfo.getPath() + dataUrlSuffix,
                    connectionInfo.getUserName(), connectionInfo.getPassword());
            services = new Services(metaModel, dataConnector);

        } catch (Exception e) {
            String message = "Connection error: " + e.getMessage();
            LOG.fatal(message);
            throw new VersionOneException(message, e);
        }
    }

    public void publish(ChangeSetInfo changeSetInfo) throws VersionOneException {
        String message = "Error during saving changeset: ";

        List<Oid> affectedWorkitems = null;

        try {
            affectedWorkitems = getAffectedWorkitems(changeSetInfo.getReferences());

        } catch (APIException e) {
            LOG.error(message + e.getMessage());
            throw new VersionOneException(message + e.getMessage(), e);

        } catch (OidException e) {
            LOG.error(message + e.getMessage());
            throw new VersionOneException(message + e.getMessage(), e);

        } catch (ConnectionException e) {
            LOG.error(message + e.getMessage());
            throw new VersionOneException(message + e.getMessage(), e);
        }

        Asset changeSet = null;

        try {
            changeSet = getChangeSet(changeSetInfo, affectedWorkitems);

        } catch (V1Exception e) {
            LOG.error(message + e.getMessage());
            throw new VersionOneException(message + e.getMessage(), e);
        }

        if (changeSet == null) {
            return;
        }

        try {
            Asset savedAsset = saveChangeSet(changeSet, changeSetInfo, affectedWorkitems);

            if (linkInfo != null){
                saveLinkInfo(savedAsset, changeSetInfo);
            }

            LOG.info(String.format("Changeset %1$s by %2$s on %3$s was saved.", savedAsset.getOid(), changeSetInfo.getAuthor(),
                    changeSetInfo.getChangeDate()));

        } catch (APIException e) {
            LOG.error(message + e.getMessage());
            throw new VersionOneException(message + e.getMessage(), e);

        } catch (V1Exception e) {
            LOG.error(message + e.getMessage());
            throw new VersionOneException(message + e.getMessage(), e);
        }
    }

    private void saveLinkInfo(Asset changeSet, ChangeSetInfo changeSetInfo) throws V1Exception, ConnectionException {
        String name = linkInfo.getLinkNameTemplate().replace("{0}", changeSetInfo.getRevision());
        String url = linkInfo.getLinkUrlTemplate().replace("{0}", changeSetInfo.getRevision());

        Attribute linkUrlAttribute = changeSet.getAttribute(getChangeSetType().getAttributeDefinition(linksAttrDef));

        if (shouldCreateURL(url, linkUrlAttribute)) {
            Asset newLink = services.createNew(getLinktType(), changeSet.getOid().getMomentless());
            newLink.setAttributeValue(getLinktType().getAttributeDefinition(nameAttrDef), name);
            newLink.setAttributeValue(getLinktType().getAttributeDefinition(urlAttrDef), url);
            newLink.setAttributeValue(getLinktType().getAttributeDefinition(onMenuAttrDef), linkInfo.isLinkOnMenu());

            services.save(newLink, changeComment);
        }
    }

    private Asset saveChangeSet(Asset changeSet, ChangeSetInfo changeSetInfo, List<Oid> workitems) throws V1Exception {
        changeSet.setAttributeValue(getChangeSetType().getAttributeDefinition(nameAttrDef),
                String.format("'%1$s' on '%2$s'", changeSetInfo.getAuthor(),
                getFormattedTime(changeSetInfo.getChangeDate())));

        changeSet.setAttributeValue(getChangeSetType().getAttributeDefinition(descriptionAttrDef),
                changeSetInfo.getMessage());

        for (Oid oid : workitems) {
            changeSet.addAttributeValue(getChangeSetType().getAttributeDefinition(primaryWorkitemsAttrDef), oid);
        }
        
        services.save(changeSet, changeComment);

        return changeSet;
    }

    private Boolean shouldCreateURL(String url, Attribute linkUrlAttribute) {
        if (linkUrlAttribute == null) {
            return true;
        }

        for (Object value : linkUrlAttribute.getValues()) {
            String strValue = value.toString();
            if (strValue.compareToIgnoreCase(url) == 0) {
                return false;
            }
        }

        return true;
    }

    private String getFormattedTime(Date changeDate) {
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
        String formattedChangeDate = dateFormatter.format(changeDate);
        return String.format("%1$s UTC%2$tz", formattedChangeDate, changeDate);
    }

    private Asset getChangeSet(ChangeSetInfo changeSetInfo, List<Oid> affectedWorkitems) throws V1Exception, OidException, ConnectionException {
        Asset changeSet = null;

        Asset[] list = findExistingChangeset(changeSetInfo.getRevision()).getAssets();

        if (list.length > 0) {
            changeSet = list[0]; //TODO: why the first element?

            LOG.info(String.format("Using existing Change Set: %1$s (%2$s)", changeSetInfo.getRevision(),
                    changeSet.getOid()));
        } else {
            if (shouldCreate(affectedWorkitems)) {
                changeSet = services.createNew(getChangeSetType(), Oid.Null);
                changeSet.setAttributeValue(getChangeSetType().getAttributeDefinition(referenceAttrDef),
                        changeSetInfo.getRevision());
            } else {
                LOG.info("No Change Set References. Ignoring Change Set: " + changeSetInfo.getRevision());
            }
        }

        return changeSet;
    }

    private QueryResult findExistingChangeset(String revision) throws OidException, APIException, ConnectionException {
        FilterTerm term = new FilterTerm(getChangeSetType().getAttributeDefinition(referenceAttrDef));
        term.Equal(revision);

        Query q = new Query(getChangeSetType());
        q.getSelection().add(getChangeSetType().getAttributeDefinition(referenceAttrDef));
        q.getSelection().add(getChangeSetType().getAttributeDefinition(linksAttrDef));
        q.setFilter(term);
        q.setPaging(new Paging(0, 1));

        return services.retrieve(q);
    }

    private boolean shouldCreate(List<Oid> affectedWorkitems) {
        return isAlwaysCreate || !affectedWorkitems.isEmpty();
    }

    private List<Oid> getAffectedWorkitems(List<String> references) throws APIException, OidException, ConnectionException {
        List<Oid> primaryWorkitems = new LinkedList<Oid>();

        for(String ref : references) {
            List<Oid> workitemOids = findWorkitemOid(ref);

            if (workitemOids.isEmpty()) {
                LOG.info(String.format("No %1$s or %2$s related to reference: %3", getLocalizedString(storyName),
                        getLocalizedString(defectName), ref));
                continue;
            }
            primaryWorkitems.addAll(workitemOids);
        }
        return primaryWorkitems;
    }

    private List<Oid> findWorkitemOid(String reference) throws OidException, APIException, ConnectionException {
        List<Oid> oids = new LinkedList<Oid>();

        Query q = new Query(getPrimaryWorkitemType());
        FilterTerm term = new FilterTerm(getPrimaryWorkitemReference());
        term.Equal(reference);
        q.setFilter(term);

        Asset[] list = services.retrieve(q).getAssets();

        for (Asset asset : list){
            oids.add(asset.getOid());
        }

        return oids;
    }
}
