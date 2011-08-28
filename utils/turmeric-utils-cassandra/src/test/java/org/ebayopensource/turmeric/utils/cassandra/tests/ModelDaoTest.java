/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.utils.cassandra.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.Set;

import org.ebayopensource.turmeric.utils.cassandra.dao.ModelDao;
import org.ebayopensource.turmeric.utils.cassandra.dao.ModelDaoImpl;
import org.ebayopensource.turmeric.utils.cassandra.model.Model;
import org.ebayopensource.turmeric.utils.cassandra.server.CassandraTestManager;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class ModelDaoTest.
 * 
 * @author jamuguerza
 */
public class ModelDaoTest extends BaseTest {

	/** The test model dao. */
	private ModelDao testModelDao;

	/** The KEY. */
	private static String KEY = "testModel_002";

	/**
	 * Before.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void before() throws Exception {
		CassandraTestManager.initialize();
		testModelDao = new ModelDaoImpl(TURMERIC_TEST_CLUSTER, HOST, KEY_SPACE,
				"TestCF");

	}

	/**
	 * Life cycle.
	 */
	@Test
	public void lifeCycle() {
		Model testModel = createModel();

		// save
		testModelDao.save(testModel);

		// find
		testModel = testModelDao.find(KEY);
		assertNotNull(testModel);

		// contains
		assertTrue(testModelDao.containsKey(KEY));
		assertFalse(testModelDao.containsKey(KEY+"111111"));
		
		// delete
		testModelDao.delete(testModel);
		testModel = testModelDao.find(KEY);
		assertTrue(testModel == null);

		//	gelAllKeys
		 testModel = createModel();
		 
		 for (int i = 1; i < 20; i++) {
			 testModel.setKey(KEY + i);
			 testModelDao.save(testModel);
		 }
		 Set<String> allKeys = testModelDao.getAllKeys();
		 
		 assertEquals(20, allKeys.size());
		 assertTrue(allKeys.contains(testModel.getKey()));
		 
	}

	private Model createModel() {
		Model testModel = new Model();
		testModel.setKey(KEY);
		testModel.setBooleanData(Boolean.TRUE);
		testModel.setIntData(Integer.MAX_VALUE);
		testModel.setLongData(Long.MAX_VALUE);
		testModel.setStringData("any String");
		testModel.setTimeData(new Date(System.currentTimeMillis()));
		return testModel;
	}

}
