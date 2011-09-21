/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.utils.cassandra.dao;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.ObjectSerializer;
import me.prettyprint.cassandra.serializers.SerializerTypeInferer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.HSuperColumn;
import me.prettyprint.hector.api.beans.OrderedSuperRows;
import me.prettyprint.hector.api.beans.SuperRow;
import me.prettyprint.hector.api.beans.SuperRows;
import me.prettyprint.hector.api.beans.SuperSlice;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.MultigetSuperSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSuperSlicesQuery;
import me.prettyprint.hector.api.query.SuperColumnQuery;
import me.prettyprint.hector.api.query.SuperSliceQuery;

import org.ebayopensource.turmeric.utils.cassandra.hector.HectorHelper;
import org.ebayopensource.turmeric.utils.cassandra.hector.HectorManager;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractColumnFamilyDao.
 * 
 * @param <SKeyType>
 *            the generic type
 * @param <ST>
 *            the generic type
 * @param <KeyType>
 *            the generic type
 * @param <T>
 *            the generic type
 * @author jamuguerza
 */
public abstract class AbstractSuperColumnFamilyDao<SKeyType, ST, KeyType, T> {

	/** The super key type class. */
	private final Class<SKeyType> superKeyTypeClass;

	/** The key type class. */
	private final Class<KeyType> keyTypeClass;

	/** The key space. */
	protected final Keyspace keySpace;

	/** The column family name. */
	protected final String columnFamilyName;

	/** The persistent class. */
	private final Class<T> persistentClass;

	/** The super persistent class. */
	private final Class<ST> superPersistentClass;

	/**
	 * Instantiates a new abstract column family dao.
	 * 
	 * @param clusterName
	 *            the clusterName
	 * @param host
	 *            the host
	 * @param s_keyspace
	 *            the s_keyspace
	 * @param superKeyTypeClass
	 *            the super key type class
	 * @param superPersistentClass
	 *            the super persistent class
	 * @param keyTypeClass
	 *            the key type class
	 * @param persistentClass
	 *            the persistent class
	 * @param columnFamilyName
	 *            the column family name
	 */
	public AbstractSuperColumnFamilyDao(final String clusterName,
			final String host, final String s_keyspace,
			final Class<SKeyType> superKeyTypeClass,
			final Class<ST> superPersistentClass,
			final Class<KeyType> keyTypeClass, final Class<T> persistentClass,
			final String columnFamilyName) {
		this.keySpace = new HectorManager().getKeyspace(clusterName, host,
				s_keyspace, columnFamilyName, true);

		this.superKeyTypeClass = superKeyTypeClass;
		this.keyTypeClass = keyTypeClass;
		this.columnFamilyName = columnFamilyName;
		this.superPersistentClass = superPersistentClass;
		this.persistentClass = persistentClass;

	}

	/**
	 * Save.
	 * 
	 * @param superKey
	 *            the super key
	 * @param modelMap
	 *            the model map
	 */
	public void save(SKeyType superKey, Map<String, T> modelMap) {

		Mutator<Object> mutator = HFactory.createMutator(keySpace,
				SerializerTypeInferer.getSerializer(superKeyTypeClass));

		for (String key : modelMap.keySet()) {
			T t = modelMap.get(key);
			List<HColumn<String, Object>> columns = HectorHelper
					.getObjectColumns(t);
			HSuperColumn<Object, String, Object> superColumn = HFactory
					.createSuperColumn(key, columns, SerializerTypeInferer
							.getSerializer(superKeyTypeClass), StringSerializer
							.get(), ObjectSerializer.get());

			mutator.addInsertion(superKey, columnFamilyName, superColumn);

		}
		mutator.execute();

	}

	public boolean containsKey(final SKeyType superKey) {

		MultigetSuperSliceQuery<Object, Object, String, byte[]> multigetSuperSliceQuery = HFactory
				.createMultigetSuperSliceQuery(keySpace,
						SerializerTypeInferer.getSerializer(superKeyTypeClass),
						SerializerTypeInferer.getSerializer(keyTypeClass),
						StringSerializer.get(), BytesArraySerializer.get());

		multigetSuperSliceQuery.setColumnFamily(columnFamilyName);
		multigetSuperSliceQuery.setKeys(superKey);

		multigetSuperSliceQuery.setRange("", "", false, Integer.MAX_VALUE);

		QueryResult<SuperRows<Object, Object, String, byte[]>> result = multigetSuperSliceQuery
				.execute();

		try {
			return (! result.get().getByKey(superKey).getSuperSlice()
					.getSuperColumns().isEmpty());
			
		} catch (Exception e) {
			return false;
		}	

	}

	/**
	 * Find.
	 * 
	 * @param superKey
	 *            the super key
	 * @param columnNames
	 *            Optional the column names
	 * @return the t
	 */
	public ST find(final SKeyType superKey, final String[] columnNames) {

		List<HSuperColumn<Object, String, byte[]>> superColumns = null;
	
			SuperSliceQuery<Object, Object, String, byte[]> superColumnQuery = HFactory
					.createSuperSliceQuery(keySpace, SerializerTypeInferer
							.getSerializer(superKeyTypeClass),
							SerializerTypeInferer.getSerializer(keyTypeClass),
							StringSerializer.get(), BytesArraySerializer.get());
			superColumnQuery.setColumnFamily(columnFamilyName).setKey(superKey);
			if (columnNames == null || (columnNames.length > 0 && "All".equals(columnNames[0]))){
				superColumnQuery.setRange("","" , false, 50);
			}else{
				superColumnQuery.setColumnNames(columnNames);
			}
			
			QueryResult<SuperSlice<Object, String, byte[]>> result = superColumnQuery
					.execute();

			try {
				superColumns = result.get().getSuperColumns();

				if (superColumns.isEmpty()) {
					return null;
				}
			} catch (Exception e) {
				return null;
			}

		try {
			ST st = superPersistentClass.newInstance();
			T t = persistentClass.newInstance();

			HectorHelper.populateSuperEntity(st, t, superKey, superColumns);
			return st;
		} catch (Exception e) {
			throw new RuntimeException("Error creating persistent class", e);
		}
	}

	/**
	 * Find super items.
	 * 
	 * @param superKeys
	 *            the super keys
	 * @param columnNames
	 *            Optional the column names
	 * @return the map
	 */
	public Map<String, ST> findItems(final List<SKeyType> superKeys,
			final String[] columnNames) {

		Map<String, ST> result = new HashMap<String, ST>();
		for (SKeyType superKey : superKeys) {
			result.put((String) superKey, find(superKey, columnNames));
		}

		// Map<String, ST> items = new HashMap<String, ST>();
		//
		//
		// List<HSuperColumn<Object, String, byte[]>> superColumns = null;
		// if (columnNames != null && columnNames.length > 0) {
		//
		// SuperSliceQuery<Object, Object, String, byte[]> superColumnQuery =
		// HFactory
		// .createSuperSliceQuery(keySpace, SerializerTypeInferer
		// .getSerializer(superKeyTypeClass),
		// SerializerTypeInferer.getSerializer(keyTypeClass),
		// StringSerializer.get(), BytesArraySerializer.get());
		// superColumnQuery.setColumnFamily(columnFamilyName).setKey(superKey)
		// .setColumnNames(columnNames);
		//
		//
		// QueryResult<SuperSlice<Object, String, byte[]>> result =
		// superColumnQuery
		// .execute();
		//
		// try {
		// superColumns = result.get().getSuperColumns();
		//
		// if (superColumns.isEmpty()) {
		// return null;
		// }
		// } catch (Exception e) {
		// return null;
		// }
		//
		// } else {
		//
		// MultigetSuperSliceQuery<Object, Object, String, byte[]>
		// multigetSuperSliceQuery = HFactory
		// .createMultigetSuperSliceQuery(keySpace,
		// SerializerTypeInferer
		// .getSerializer(superKeyTypeClass),
		// SerializerTypeInferer.getSerializer(keyTypeClass),
		// StringSerializer.get(), BytesArraySerializer.get());
		//
		// multigetSuperSliceQuery.setColumnFamily(columnFamilyName);
		// multigetSuperSliceQuery.setKeys(superKey);
		//
		// multigetSuperSliceQuery.setRange("", "", false, Integer.MAX_VALUE);
		//
		// QueryResult<SuperRows<Object, Object, String, byte[]>> result =
		// multigetSuperSliceQuery
		// .execute();
		//
		// try {
		// superColumns = result.get().getByKey(superKeys).getSuperSlice()
		// .getSuperColumns();
		// if (superColumns.isEmpty()) {
		// return null;
		// }
		// } catch (Exception e) {
		// return null;
		// }
		// }
		//
		// try {
		// ST st = superPersistentClass.newInstance();
		// T t = persistentClass.newInstance();
		// HectorHelper.populateSuperEntity(st, t, superKey, superColumns);
		// return st;
		// } catch (Exception e) {
		// throw new RuntimeException("Error creating persistent class", e);
		// }
		//
		// return items;
		return result;
	}

	/**
	 * Delete.
	 * 
	 * @param superKey
	 *            the super key
	 * @see http://wiki.apache.org/cassandra/DistributedDeletes
	 */
	public void delete(SKeyType superKey) {
		Mutator<Object> mutator = HFactory.createMutator(keySpace,
				SerializerTypeInferer.getSerializer(superKeyTypeClass));
		mutator.delete(superKey, columnFamilyName, null,
				SerializerTypeInferer.getSerializer(superKeyTypeClass));
	}

	/**
	 * Gets the keys.
	 * 
	 * @return the keys
	 */
	public Set<String> getKeys() {
		int rows = 0;
		int pagination = 50;
		Set<String> rowKeys = new HashSet<String>();

		SuperRow<Object, String, String, byte[]> lastRow = null;

		do {
			RangeSuperSlicesQuery<Object, String, String, byte[]> rangeSuperSliceQuery = HFactory
					.createRangeSuperSlicesQuery(keySpace,
							SerializerTypeInferer
									.getSerializer(superKeyTypeClass),
							StringSerializer.get(), StringSerializer.get(),
							BytesArraySerializer.get());

			rangeSuperSliceQuery.setColumnFamily(columnFamilyName);
			if (lastRow != null) {
				rangeSuperSliceQuery.setKeys(lastRow.getKey(), "");
			} else {
				rangeSuperSliceQuery.setKeys("", "");
			}
			rangeSuperSliceQuery.setRange("", "", false, 2);
			rangeSuperSliceQuery.setRowCount(pagination);

			QueryResult<OrderedSuperRows<Object, String, String, byte[]>> result = rangeSuperSliceQuery
					.execute();
			OrderedSuperRows<Object, String, String, byte[]> orderedSuperRows = result
					.get();
			rows = orderedSuperRows.getCount();

			for (SuperRow<Object, String, String, byte[]> row : orderedSuperRows) {
				if (!row.getSuperSlice().getSuperColumns().isEmpty()) {
					rowKeys.add((String) row.getKey());
					lastRow = orderedSuperRows.getList().get(rows - 1);
				}
			}

		} while (rows == pagination);

		return rowKeys;

	}

}