package com.versionone.git;

import com.versionone.Oid;
import com.versionone.apiclient.*;
import com.versionone.git.configuration.Configuration;
import com.versionone.git.configuration.Link;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.util.*;

public class ChangeSetWriter implements IChangeSetWriter {
    private final String referenceAttribute;
    private final Boolean isAlwaysCreate;
    private final String changeComment;
    private final Logger LOG = Logger.getLogger("GitIntegration");
    private final Link linkSettings;

    private static final String CHANGESET_TYPE = "ChangeSet";
    private static final String LINK_TYPE = "Link";
    private static final String PRIMARY_WORKITEM_TYPE = "PrimaryWorkitem";

    private static final String NAME_ATTRIBUTE = "Name";
    private static final String DESCRIPTION_ATTRIBUTE = "Description";
    private static final String REFERENCE_ATTRIBUTE = "Reference";
    private static final String URL_ATTRIBUTE = "URL";
    private static final String ON_MENU_ATTRIBUTE = "OnMenu";
    private static final String PRIMARY_WORKITEMS_ATTRIBUTE = "PrimaryWorkitems";
    private static final String LINKS_ATTRIBUTE = "Links.URL";
    private static final String CHILDREN_ME_AND_DOWN_ATTRIBUTE_PREFIX = "PrimaryWorkitem.ChildrenMeAndDown";
    private static final String STORY_NAME = "Plural'Story";
    private static final String DEFECT_NAME = "Plural'Defect";

    private final IVersionOneConnector connector;

    private IAssetType getChangeSetType() {
        return connector.getMetaModel().getAssetType(CHANGESET_TYPE);
    }

    private IAssetType getPrimaryWorkitemType() {
        return connector.getMetaModel().getAssetType(PRIMARY_WORKITEM_TYPE);
    }

    private IAttributeDefinition getPrimaryWorkitemReference() {
        return connector.getMetaModel().getAttributeDefinition(CHILDREN_ME_AND_DOWN_ATTRIBUTE_PREFIX + "." + referenceAttribute);
    }

    private IAssetType getLinkType() {
        return connector.getMetaModel().getAssetType(LINK_TYPE);
    }

    private String getLocalizedString(String name) {
        return connector.getLocalizer().resolve(name);
    }

    public ChangeSetWriter(Configuration config, IVersionOneConnector v1Connector, Link linkSettings) {
        connector = v1Connector;
        referenceAttribute = config.getReferenceAttribute();
        isAlwaysCreate = config.isAlwaysCreate();
        changeComment = config.getChangeComment();
        this.linkSettings = linkSettings;
		
		if(isAlwaysCreate) {
			LOG.info("Always Create a VersionOne ChangeSet");
        }
    }

    public void publish(ChangeSetInfo changeSetInfo) throws VersionOneException {
        final String errorMessagePrefix = "Error during saving changeset: ";
        
        try {
            List<Oid> affectedWorkitems = getAffectedWorkitems(changeSetInfo.getReferences());
            Asset changeSet = getChangeSet(changeSetInfo, affectedWorkitems);
            
            if(changeSet == null) {
                return;
            }

            Asset savedAsset = saveChangeSet(changeSet, changeSetInfo, affectedWorkitems);

            if(linkSettings != null) {
                saveLinkInfo(changeSet, changeSetInfo, linkSettings);
            }

            LOG.info(String.format("Changeset %1$s (%2$s) by %3$s on %4$s was saved.", changeSetInfo.getRevision(),
                     savedAsset.getOid(), changeSetInfo.getAuthor(), changeSetInfo.getChangeDate()));
        } catch(Exception ex) {
            logAndThrow(errorMessagePrefix + ex.getMessage(), ex);
        }
    }

    private void logAndThrow(String errorMessagePrefix, Exception ex) throws VersionOneException {
        LOG.error(errorMessagePrefix + ex.getMessage());
        throw new VersionOneException(errorMessagePrefix + ex.getMessage(), ex);
    }

    private void saveLinkInfo(Asset changeSet, ChangeSetInfo changeSetInfo, Link linkSettings) throws V1Exception {
        String name = linkSettings.getLinkNameTemplate().replace("{0}", changeSetInfo.getRevision());
        String url = linkSettings.getLinkUrlTemplate().replace("{0}", changeSetInfo.getRevision());

        Attribute linkUrlAttribute = changeSet.getAttribute(getChangeSetType().getAttributeDefinition(LINKS_ATTRIBUTE));

        if (shouldCreateURL(url, linkUrlAttribute)) {
            Asset newLink = connector.getServices().createNew(getLinkType(), changeSet.getOid().getMomentless());
            newLink.setAttributeValue(getLinkType().getAttributeDefinition(NAME_ATTRIBUTE), name);
            newLink.setAttributeValue(getLinkType().getAttributeDefinition(URL_ATTRIBUTE), url);
            newLink.setAttributeValue(getLinkType().getAttributeDefinition(ON_MENU_ATTRIBUTE), linkSettings.isLinkOnMenu());

            connector.getServices().save(newLink, changeComment);
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
        
        connector.getServices().save(changeSet, changeComment);

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
                changeSet = connector.getServices().createNew(getChangeSetType(), Oid.Null);
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

        return connector.getServices().retrieve(q);
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

        Asset[] list = connector.getServices().retrieve(q).getAssets();

        for (Asset asset : list){
            oids.add(asset.getOid());
        }

        return oids;
    }
}
