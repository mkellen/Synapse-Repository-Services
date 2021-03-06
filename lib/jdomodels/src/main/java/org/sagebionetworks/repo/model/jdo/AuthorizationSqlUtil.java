package org.sagebionetworks.repo.model.jdo;

import org.sagebionetworks.repo.model.query.jdo.SqlConstants;

public class AuthorizationSqlUtil {
	
	/*
	 * select acl.id from jdoaccesscontrollist acl, jdoresourceaccess ra, access_type at
	 * where
	 * ra.oid_id=acl.id and ra.groupId in :groups and at.oid_id=ra.id and at.type=:type
	 */
	private static final String AUTHORIZATION_SQL_1 = 
		"select distinct acl."+SqlConstants.ACL_OWNER_ID_COLUMN+" from "+SqlConstants.TABLE_ACCESS_CONTROL_LIST+" acl, "+
		SqlConstants.TABLE_RESOURCE_ACCESS+" ra, "+
		SqlConstants.TABLE_RESOURCE_ACCESS_TYPE+" at where ra."+
		SqlConstants.COL_RESOURCE_ACCESS_OWNER+
		"=acl."+SqlConstants.COL_ACL_ID+" and (ra."+SqlConstants.COL_RESOURCE_ACCESS_GROUP_ID+
		" in (";

	/**
	 * The bind variable used to set the access type for authorization filters.
	 */
	public static final String ACCESS_TYPE_BIND_VAR = "type";
	public static final String NODE_ID_BIND_VAR = "nodeId";
	
	private static final String AUTHORIZATION_SQL_2 = 
		"))"+
		" and at."+SqlConstants.COL_RESOURCE_ACCESS_TYPE_ID+"=ra."+SqlConstants.COL_NODE_ID+
		" and at."+SqlConstants.COL_RESOURCE_ACCESS_TYPE_ELEMENT+"=:"+ACCESS_TYPE_BIND_VAR;
	
	private static final String CAN_ACCESS_SQL_1 ="select count(n."+SqlConstants.COL_NODE_ID+")  from "+SqlConstants.TABLE_ACCESS_CONTROL_LIST+" acl, "+SqlConstants.TABLE_RESOURCE_ACCESS+" ra, "+SqlConstants.TABLE_RESOURCE_ACCESS_TYPE+" at, "+SqlConstants.TABLE_NODE+" n where ra."+SqlConstants.COL_RESOURCE_ACCESS_OWNER+"=acl."+SqlConstants.COL_ACL_ID+" and (ra."+SqlConstants.COL_RESOURCE_ACCESS_GROUP_ID+" in (";
	private static final String CAN_ACCESS_SQL_2 =")) and at."+SqlConstants.COL_RESOURCE_ACCESS_TYPE_ID+"=ra."+SqlConstants.COL_RESOURCE_ACCESS_ID+" and at."+SqlConstants.COL_RESOURCE_ACCESS_TYPE_ELEMENT+"=:"+ACCESS_TYPE_BIND_VAR+" AND acl."+SqlConstants.ACL_OWNER_ID_COLUMN+" = n."+SqlConstants.COL_NODE_BENEFACTOR_ID+" and n."+SqlConstants.COL_NODE_ID+" =:"+NODE_ID_BIND_VAR;

	/**
	 * The bind variable prefix used for group ID for the authorization SQL.
	 */
	public static final String BIND_VAR_PREFIX = "g";


	/**
	 * @return the SQL to find the root-accessible nodes that a specified user-group list can access
	 * using a specified access type
	 * 
	 * Can't bind a collection to a variable in the string, so we have to create n bind variables 
	 * for a collection of length n.  :^(
	 */
	public static String authorizationSQL(int n) {
		StringBuilder sb = new StringBuilder(AUTHORIZATION_SQL_1);
		for (int i=0; i<n; i++) {
			if (i>0) sb.append(",");
			sb.append(":");
			sb.append(BIND_VAR_PREFIX);
			sb.append(i);
		}
		sb.append(AUTHORIZATION_SQL_2);
		return sb.toString();
	}
	
	/**
	 * Create the canAccess Sql
	 * @param numberUserGroups
	 * @return
	 */
	public static String authorizationCanAccessSQL(int numberUserGroups){
		StringBuilder sb = new StringBuilder(CAN_ACCESS_SQL_1);
		for (int i=0; i<numberUserGroups; i++) {
			if (i>0) sb.append(",");
			sb.append(":");
			sb.append(BIND_VAR_PREFIX);
			sb.append(i);
		}
		sb.append(CAN_ACCESS_SQL_2);
		return sb.toString();
	}

}
