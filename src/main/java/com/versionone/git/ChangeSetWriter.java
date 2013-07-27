package com.versionone.git;

import com.versionone.Oid;
import com.versionone.apiclient.*;
import com.versionone.git.configuration.ChangeSet;

import org.apache.log4j.Logger;
import org.eclipse.jgit.util.StringUtils;

import java.util.*;

public class ChangeSetWriter implements IChangeSetWriter {
    private final Logger LOG = Logger.getLogger("GitIntegration");

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

    private final ChangeSet changeSetConfig;
    private final IVersionOneConnector connector;

    private IAssetType getChangeSetType() {
        return connector.getMetaModel().getAssetType(CHANGESET_TYPE);
    }

    private IAssetType getPrimaryWorkitemType() {
        return connector.getMetaModel().getAssetType(PRIMARY_WORKITEM_TYPE);
    }

    private IAttributeDefinition getPrimaryWorkitemReference() {
        return connector.getMetaModel().getAttributeDefinition(CHILDREN_ME_AND_DOWN_ATTRIBUTE_PREFIX + "." + changeSetConfig.getReferenceAttribute());
    }

    private IAssetType getLinkType() {
        return connector.getMetaModel().getAssetType(LINK_TYPE);
    }

    private String getLocalizedString(String name) {
        return connector.getLocalizer().resolve(name);
    }

    public ChangeSetWriter(ChangeSet changeSetConfig, IVersionOneConnector connector) {
        this.changeSetConfig = changeSetConfig;
        this.connector = connector;

		if(changeSetConfig.isAlwaysCreate()) {
			LOG.info("Always creating new VersionOne changesets instead of updating existing ones");
        }
    }

    public void publish(ChangeSetInfo changeSetInfo) throws VersionOneException {
        final String errorMessagePrefix = String.format("An error occurred while saving changeset for commit %1$s", changeSetInfo.getRevision());

        List<Oid> affectedWorkitems = null;

        try {
            affectedWorkitems = getAffectedWorkitems(changeSetInfo.getReferences());
        } catch (Exception ex) {
            logAndThrow(errorMessagePrefix + ex.getMessage(), ex);
        }

        Asset changeSet = null;

        try {
            changeSet = getChangeSet(changeSetInfo, affectedWorkitems);
        } catch (Exception ex) {
           logAndThrow(errorMessagePrefix + ex.getMessage(), ex);
        }

        if (changeSet == null) {
            return;
        }

        try {
            Asset savedAsset = saveChangeSet(changeSet, changeSetInfo, affectedWorkitems);

            if (changeSetInfo.getLinkName() != null && changeSetInfo.getLinkUrl() != null){
                saveLink(savedAsset, changeSetInfo);
            }

            LOG.info(String.format("Saved %1$s to %2$s for commit %3$s by %4$s on %5$s successfully",
                    savedAsset.getOid(),
                    StringUtils.join(changeSetInfo.getReferences(), ", "),
                    changeSetInfo.getRevision(),
                    changeSetInfo.getAuthor(),
                    changeSetInfo.getChangeDate()));
        } catch (Exception ex) {
            logAndThrow(errorMessagePrefix + ex.getMessage(), ex);
        }
    }

    private void logAndThrow(String errorMessagePrefix, Exception ex) throws VersionOneException {
        LOG.error(errorMessagePrefix + ex.getMessage());
        throw new VersionOneException(errorMessagePrefix + ex.getMessage(), ex);
    }

    private void saveLink(Asset changeSet, ChangeSetInfo changeSetInfo) throws V1Exception {

        Attribute linkUrlAttribute = changeSet.getAttribute(getChangeSetType().getAttributeDefinition(LINKS_ATTRIBUTE));

        if (shouldCreateURL(changeSetInfo.getLinkUrl(), linkUrlAttribute)) {
            Asset newLink = connector.getServices().createNew(getLinkType(), changeSet.getOid().getMomentless());
            newLink.setAttributeValue(getLinkType().getAttributeDefinition(NAME_ATTRIBUTE), changeSetInfo.getLinkName());
            newLink.setAttributeValue(getLinkType().getAttributeDefinition(URL_ATTRIBUTE), changeSetInfo.getLinkUrl());
            newLink.setAttributeValue(getLinkType().getAttributeDefinition(ON_MENU_ATTRIBUTE), changeSetInfo.isLinkOnMenu());

            connector.getServices().save(newLink, changeSetConfig.getChangeComment());
        }
    }

    private Asset saveChangeSet(Asset changeSet, ChangeSetInfo changeSetInfo, List<Oid> workitems) throws V1Exception {

        changeSet.setAttributeValue(getChangeSetType().getAttributeDefinition(NAME_ATTRIBUTE), changeSetInfo.getName(changeSetConfig));
        changeSet.setAttributeValue(getChangeSetType().getAttributeDefinition(DESCRIPTION_ATTRIBUTE), changeSetInfo.getMessage());

        for (Oid oid : workitems) {
            changeSet.addAttributeValue(getChangeSetType().getAttributeDefinition(PRIMARY_WORKITEMS_ATTRIBUTE), oid);
        }
        
        connector.getServices().save(changeSet, changeSetConfig.getChangeComment());

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

    private Asset getChangeSet(ChangeSetInfo changeSetInfo, List<Oid> affectedWorkitems) throws V1Exception {
        Asset changeSet = null;

        Asset[] list = findExistingChangeset(changeSetInfo.getRevision()).getAssets();

        if (list.length > 0) {
            changeSet = list[0];

            LOG.info(String.format("Using existing %1$s for commit %2$s",
                    changeSet.getOid(),
                    changeSetInfo.getRevision()));
        } else {
            if (shouldCreate(affectedWorkitems)) {
                changeSet = connector.getServices().createNew(getChangeSetType(), Oid.Null);
                changeSet.setAttributeValue(getChangeSetType().getAttributeDefinition(REFERENCE_ATTRIBUTE),
                        changeSetInfo.getRevision());
            } else {
                LOG.warn("Ignoring changeset for commit " + changeSetInfo.getRevision() + " as no affected workitems were found");
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
        return changeSetConfig.isAlwaysCreate() || !affectedWorkitems.isEmpty();
    }

    private List<Oid> getAffectedWorkitems(List<String> references) throws APIException, OidException, ConnectionException {
        List<Oid> primaryWorkitems = new LinkedList<Oid>();

        for(String ref : references) {
            List<Oid> workitemOids = findWorkitemOid(ref);

            if (workitemOids.isEmpty()) {
                LOG.warn(String.format("No %1$s or %2$s found with ID %3$s",
                        getLocalizedString(STORY_NAME),
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
