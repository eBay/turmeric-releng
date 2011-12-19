package org.ebayopensource.turmeric.utils.cassandra.model;

import java.util.Map;

public class ModelWithDefaultAndMultiParamConstructor<K> {

	/** The key. */
	private K key;

	/** The columns. */
	private Map<String, Object> columns;

	public ModelWithDefaultAndMultiParamConstructor(String param1, Long param2,
			Float param3) {

	}

	public ModelWithDefaultAndMultiParamConstructor() {
		System.out
				.println("ModelWithDefaultAndMultiParamConstructor: default constrcutor called");
	}

	/**
	 * Sets the key.
	 * 
	 * @param key
	 *            the new key
	 */
	public void setKey(K key) {
		this.key = key;
	}

	/**
	 * Gets the key.
	 * 
	 * @return the key
	 */
	public K getKey() {
		return key;
	}

	/**
	 * Sets the columns.
	 * 
	 * @param columns
	 *            the columns
	 */
	public void setColumns(Map<String, Object> columns) {
		this.columns = columns;
	}

	/**
	 * Gets the columns.
	 * 
	 * @return the columns
	 */
	public Map<String, Object> getColumns() {
		return columns;
	}

}
