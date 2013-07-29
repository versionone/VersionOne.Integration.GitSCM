## Introduction

VersionOne Integration for Git creates a record of Git changesets in VersionOne so the development team can quickly view all of the code changes for a story or defect. This visibility can be useful when tracking down defects or performing code reviews. Development team members include a story or defect ID, such as `S-01454` in their Git commit messages. The integration checks each commit message for IDs and creates a changeset within VersionOne linked to the stories or defects matching the entered IDs. A changeset is the collection of changes for a single revision in Git. The changesets for each story or defect are viewable on the details page of that item in VersionOne. Additionally, the integration has the capability to create link on the changeset in VersionOne that navigates to the changeset in a Git repository viewer.

The following sequence diagram illustrates how V1Git interacts with Git and VersionOne.

<div class=wsd wsd_style="qsd">
<pre>
title Git Integration Sequence

Integration->Git: Any changes?
activate Git
Git-->Integration: No
deactivate Git

Developer->Git: Commit change
Integration->Git: Any changes?
activate Git
Git-->Integration: Yes
deactivate Git
activate Integration
Integration->VersionOne: Create ChangeSet
Integration->VersionOne: Relate ChangeSet to WorkItems
Integration->VersionOne: Link to Git
deactivate Integration

Integration->Git: Any changes?
activate Git
Git-->Integration: No
deactivate Git
</pre>
</div>
<script type="text/javascript" src="http://www.websequencediagrams.com/service.js"></script>
