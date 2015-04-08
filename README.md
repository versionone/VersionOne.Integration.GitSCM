# VersionOne Integration.Git #
Copyright (c) 2011-2012 VersionOne, Inc.
All rights reserved.

The VersionOne Git Integration (V1Git) creates a record of Git changesets in VersionOne, so the development team can quickly view all of the code changes for a story or defect. This visibility can be useful when tracking down defects or performing code reviews.

Once V1Git has been installed, development team members include a story or defect ID, such as "S-01454" in their Git commit messages. V1Git checks each commit message for IDs and creates a changeset within VersionOne linked to the stories or defects matching the entered IDs. A changeset is the collection of changes for a single revision in Git. The changesets for each story or defect are viewable on the details page of that item in VersionOne. Additionally, V1Git has the capability to create link on the changeset in VersionOne that navigates to the changeset in a Git repository viewer.

## System Requirements ##

### VersionOne ###
12.2 or above, including Team Edition

### Integration Server ###
Java SDK 1.6 or above

### Git Server ###
Tested with GitHub

## Want to contribute?
If you are interested in contributing to this project, please contact [VersionOne openAgile Team](mailto:openAgileSupport@versionone.com).
