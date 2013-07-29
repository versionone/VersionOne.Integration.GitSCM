## Introduction

VersionOne Integration for Git creates a record of Git changesets in VersionOne so the development team can quickly view all of the code changes for a story or defect. This visibility can be useful when tracking down defects or performing code reviews. Development team members include a story or defect ID, such as `S-01454` in their Git commit messages. The integration checks each commit message for IDs and creates a changeset within VersionOne linked to the stories or defects matching the entered IDs. A changeset is the collection of changes for a single revision in Git. The changesets for each story or defect are viewable on the details page of that item in VersionOne. Additionally, the integration has the capability to create link on the changeset in VersionOne that navigates to the changeset in a Git repository viewer.

The following sequence diagram illustrates how VersionOne Integration for Git interacts with Git and VersionOne.

![Git Integration Sequence Diagram](images/Git_Integration_Sequence.png)
