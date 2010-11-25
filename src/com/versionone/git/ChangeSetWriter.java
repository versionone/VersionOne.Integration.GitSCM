package com.versionone.git;

import com.versionone.Oid;
import com.versionone.apiclient.*;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.util.*;

public class ChangeSetWriter implements IChangeSetWriter {

    private final String META_URL_SUFFIX = "meta.v1/";
    private final String DATA_URL_SUFFIX = "rest-1.v1/";
    private final String localUrlSuffix = "loc.v1/";

    private final String CHANGESET_TYPE = "ChangeSet";
    private final String LINK_TYPE = "Link";
    private final String PRIMARY_WORKITEM_TYPE = "PrimaryWorkitem";

    private final String NAME_ATTRIBUTE = "Name";
    private final String DESCRIPTION_ATTRIBUTE = "Description";
    private final String REFERENCE_ATTRIBUTE = "Reference";
    private final String URL_ATTRIBUTE = "URL";
    private final String ON_MENU_ATTRIBUTE = "OnMenu";
    private final String PRIMARY_WORKITEMS_ATTRIBUTE = "PrimaryWorkitems";
    private final String LINKS_ATTRIBUTE = "Links.URL";
    private final String CHILDREN_ME_AND_DOWN_ATTRIBUTE_PREFIX = "PrimaryWorkitem.ChildrenMeAndDown";
    private final String STORY_NAME = "Plural'Story";
    private final String DEFECT_NAME = "Plural'Defect";

    private IServices services;
    private IMetaModel metaModel;
    private ILocalizer localizer;

    private final Configuration.VersionOneConnection connectionInfo;
    private final Configuration.Link linkInfo;
    private final String referenceAttribute;
    private final Boolean isAlwaysCreate;
    private final String changeComment;
    private final Logger LOG = Logger.getLogger("GitIntegration");

    private IAssetType getChangeSetType() {
        return metaModel.getAssetType(CHANGESET_TYPE);
    }

    private IAssetType getPrimaryWorkitemType() {
        return metaModel.getAssetType(PRIMARY_WORKITEM_TYPE);
    }

    private IAttributeDefinition getPrimaryWorkitemReference() {
        return metaModel.getAttributeDefinition(CHILDREN_ME_AND_DOWN_ATTRIBUTE_PREFIX + "." + referenceAttribute);
    }
    
    private IAssetType getLinkType() {
        return metaModel.getAssetType(LINK_TYPE);
    }

    private String getLocalizedString(String name) {
        return localizer.resolve(name);
    }

    public ChangeSetWriter(Configuration config) {
        connectionInfo = config.getVersionOneConnection();
        linkInfo = config.getLink();
        referenceAttribute = config.getReferenceAttribute();
        isAlwaysCreate = config.isAlwaysCreate();
        changeComment = config.getChangeComment();
    }

    public void connect() throws VersionOneException {
        try {
            V1APIConnector metaConnector = new V1APIConnector(connectionInfo.getPath() + META_URL_SUFFIX,
                    connectionInfo.getUserName(), connectionInfo.getPassword());
            metaModel = new MetaModel(metaConnector);

            V1APIConnector localizerConnector = new V1APIConnector(connectionInfo.getPath() + localUrlSuffix,
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

    public void publish(ChangeSetInfo changeSetInfo) throws VersionOneException {
        final String errorMessagePrefix = "Error during saving changeset: ";

        List<Oid> affectedWorkitems = null;

        try {
            affectedWorkitems = getAffectedWorkitems(changeSetInfo.getReferences());
        } catch (APIException ex) {
            logAndThrow(errorMessagePrefix + ex.getMessage(), ex);
        } catch (OidException ex) {
            logAndThrow(errorMessagePrefix + ex.getMessage(), ex);
        } catch (ConnectionException ex) {
            logAndThrow(errorMessagePrefix + ex.getMessage(), ex);
        }

        Asset changeSet = null;

        try {
            changeSet = getChangeSet(changeSetInfo, affectedWorkitems);
        } catch (V1Exception ex) {
           logAndThrow(errorMessagePrefix + ex.getMessage(), ex);
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
        } catch (V1Exception ex) {
            logAndThrow(errorMessagePrefix + ex.getMessage(), ex);
        }
    }

    private void logAndThrow(String errorMessagePrefix, Exception ex) throws VersionOneException {
        LOG.error(errorMessagePrefix + ex.getMessage());
        throw new VersionOneException(errorMessagePrefix + ex.getMessage(), ex);
    }

    private void saveLinkInfo(Asset changeSet, ChangeSetInfo changeSetInfo) throws V1Exception {
        String name = linkInfo.getLinkNameTemplate().replace("{0}", changeSetInfo.getRevision());
        String url = linkInfo.getLinkUrlTemplate().replace("{0}", changeSetInfo.getRevision());

        Attribute linkUrlAttribute = changeSet.getAttribute(getChangeSetType().getAttributeDefinition(LINKS_ATTRIBUTE));

        if (shouldCreateURL(url, linkUrlAttribute)) {
            Asset newLink = services.createNew(getLinkType(), changeSet.getOid().getMomentless());
            newLink.setAttributeValue(getLinkType().getAttributeDefinition(NAME_ATTRIBUTE), name);
            newLink.setAttributeValue(getLinkType().getAttributeDefinition(URL_ATTRIBUTE), url);
            newLink.setAttributeValue(getLinkType().getAttributeDefinition(ON_MENU_ATTRIBUTE), linkInfo.isLinkOnMenu());

            services.save(newLink, changeComment);
        }
    }

    private Asset saveChangeSet(Asset changeSet, ChangeSetInfo changeSetInfo, List<Oid> workitems) throws V1Exception {
        changeSet.setAttributeValue(getChangeSetType().getAttributeDefinition(NAME_ATTRIBUTE),
                String.format("'%1$s' on '%2$s'", changeSetInfo.getAuthor(),
                getFormattedTime(changeSetInfo.getChangeDate())));

        changeSet.setAttributeValue(getChangeSetType().getAttributeDefinition(DESCRIPTION_ATTRIBUTE),
                changeSetInfo.getMessage());

        for (Oid oid : workitems) {
            changeSet.addAttributeValue(getChangeSetType().getAttributeDefinition(PRIMARY_WORKITEMS_ATTRIBUTE), oid);
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
        DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
        String formattedChangeDate = dateFormatter.format(changeDate);
        return String.format("%1$s UTC%2$tz", formattedChangeDate, changeDate);
    }

    private Asset getChangeSet(ChangeSetInfo changeSetInfo, List<Oid> affectedWorkitems) throws V1Exception {
        Asset changeSet = null;

        Asset[] list = findExistingChangeset(changeSetInfo.getRevision()).getAssets();

        if (list.length > 0) {
            changeSet = list[0];

            LOG.info(String.format("Using existing Change Set: %1$s (%2$s)", changeSetInfo.getRevision(),
                    changeSet.getOid()));
        } else {
            if (shouldCreate(affectedWorkitems)) {
                changeSet = services.createNew(getChangeSetType(), Oid.Null);
                changeSet.setAttributeValue(getChangeSetType().getAttributeDefinition(REFERENCE_ATTRIBUTE),
                        changeSetInfo.getRevision());
            } else {
                LOG.info("No Change Set References. Ignoring Change Set: " + changeSetInfo.getRevision());
            }
        }

        return changeSet;
    }

    private QueryResult findExistingChangeset(String revision) throws OidException, APIException, ConnectionException {
        FilterTerm term = new FilterTerm(getChangeSetType().getAttributeDefinition(REFERENCE_ATTRIBUTE));
        term.Equal(revision);

        Query q = new Query(getChangeSetType());
        q.getSelection().add(getChangeSetType().getAttributeDefinition(REFERENCE_ATTRIBUTE));
        q.getSelection().add(getChangeSetType().getAttributeDefinition(LINKS_ATTRIBUTE));
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
                LOG.info(String.format("No %1$s or %2$s related to reference: %3$s", getLocalizedString(STORY_NAME),
                        getLocalizedString(DEFECT_NAME), ref));
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
