package com.versionone.git.storage;

import static org.junit.Assert.*;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;

public class DbStorageTester {

	@Test
	public void shouldNotHoldResultsFromGetLastCommit() {
		IDbStorage storage = new DbStorage();
		String repositoryId = "testRepositoryId";
		String branchRef = "testBranchRef";
		storage.getLastCommit(repositoryId, branchRef);
		String commitHash = "1010101010";
		try {
			storage.persistLastCommit(commitHash , repositoryId, branchRef);
		} catch (Exception e) {
			fail("Storage session is still holding a reference to the last commit.");
		}
	}

}
