/*
 * Copyright (c) 2022 Peter G. Horvath, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.robin.jdbc.connection;

import java.sql.*;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

abstract class DelegatingConnection implements Connection {

    protected abstract Connection getDelegate() throws SQLException;

    @Override
    public Statement createStatement() throws SQLException {
        return getDelegate().createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String s) throws SQLException {
        return getDelegate().prepareStatement(s);
    }

    @Override
    public CallableStatement prepareCall(String s) throws SQLException {
        return getDelegate().prepareCall(s);
    }

    @Override
    public String nativeSQL(String s) throws SQLException {
        return getDelegate().nativeSQL(s);
    }

    @Override
    public void setAutoCommit(boolean b) throws SQLException {
        getDelegate().setAutoCommit(b);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return getDelegate().getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        getDelegate().commit();
    }

    @Override
    public void rollback() throws SQLException {
        getDelegate().rollback();
    }

    @Override
    public void close() throws SQLException {
        getDelegate().close();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return getDelegate().isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return getDelegate().getMetaData();
    }

    @Override
    public void setReadOnly(boolean b) throws SQLException {
        getDelegate().setReadOnly(b);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return getDelegate().isReadOnly();
    }

    @Override
    public void setCatalog(String s) throws SQLException {
        getDelegate().setCatalog(s);
    }

    @Override
    public String getCatalog() throws SQLException {
        return getDelegate().getCatalog();
    }

    @Override
    public void setTransactionIsolation(int i) throws SQLException {
        getDelegate().setTransactionIsolation(i);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return getDelegate().getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return getDelegate().getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        getDelegate().clearWarnings();
    }

    @Override
    public Statement createStatement(int i, int i1) throws SQLException {
        return getDelegate().createStatement(i, i1);
    }

    @Override
    public PreparedStatement prepareStatement(String s, int i, int i1) throws SQLException {
        return getDelegate().prepareStatement(s, i, i1);
    }

    @Override
    public CallableStatement prepareCall(String s, int i, int i1) throws SQLException {
        return getDelegate().prepareCall(s, i, i1);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return getDelegate().getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        getDelegate().setTypeMap(map);
    }

    @Override
    public void setHoldability(int i) throws SQLException {
        getDelegate().setHoldability(i);
    }

    @Override
    public int getHoldability() throws SQLException {
        return getDelegate().getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return getDelegate().setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String s) throws SQLException {
        return getDelegate().setSavepoint(s);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        getDelegate().rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        getDelegate().releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int i, int i1, int i2) throws SQLException {
        return getDelegate().createStatement(i, i1, i2);
    }

    @Override
    public PreparedStatement prepareStatement(String s, int i, int i1, int i2) throws SQLException {
        return getDelegate().prepareStatement(s, i, i1, i2);
    }

    @Override
    public CallableStatement prepareCall(String s, int i, int i1, int i2) throws SQLException {
        return getDelegate().prepareCall(s, i, i1, i2);
    }

    @Override
    public PreparedStatement prepareStatement(String s, int i) throws SQLException {
        return getDelegate().prepareStatement(s, i);
    }

    @Override
    public PreparedStatement prepareStatement(String s, int[] ints) throws SQLException {
        return getDelegate().prepareStatement(s, ints);
    }

    @Override
    public PreparedStatement prepareStatement(String s, String[] strings) throws SQLException {
        return getDelegate().prepareStatement(s, strings);
    }

    @Override
    public Clob createClob() throws SQLException {
        return getDelegate().createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return getDelegate().createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return getDelegate().createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return getDelegate().createSQLXML();
    }

    @Override
    public boolean isValid(int i) throws SQLException {
        return getDelegate().isValid(i);
    }

    @Override
    public void setClientInfo(String s, String s1) throws SQLClientInfoException {
        try {
            getDelegate().setClientInfo(s, s1);
        } catch (SQLException sqlException) {
            throw new SQLClientInfoException(Collections.emptyMap(), sqlException);
        }
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        try {
            getDelegate().setClientInfo(properties);
        } catch (SQLException sqlException) {
            throw new SQLClientInfoException(Collections.emptyMap(), sqlException);
        }
    }

    @Override
    public String getClientInfo(String s) throws SQLException {
        return getDelegate().getClientInfo(s);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return getDelegate().getClientInfo();
    }

    @Override
    public Array createArrayOf(String s, Object[] objects) throws SQLException {
        return getDelegate().createArrayOf(s, objects);
    }

    @Override
    public Struct createStruct(String s, Object[] objects) throws SQLException {
        return getDelegate().createStruct(s, objects);
    }

    @Override
    public void setSchema(String s) throws SQLException {
        getDelegate().setSchema(s);
    }

    @Override
    public String getSchema() throws SQLException {
        return getDelegate().getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        getDelegate().abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int i) throws SQLException {
        getDelegate().setNetworkTimeout(executor, i);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return getDelegate().getNetworkTimeout();
    }

    @Override
    public void beginRequest() throws SQLException {
        getDelegate().beginRequest();
    }

    @Override
    public void endRequest() throws SQLException {
        getDelegate().endRequest();
    }

    @Override
    public boolean setShardingKeyIfValid(ShardingKey shardingKey, ShardingKey superShardingKey, int timeout)
            throws SQLException {

        return getDelegate().setShardingKeyIfValid(shardingKey, superShardingKey, timeout);
    }

    @Override
    public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
        return getDelegate().setShardingKeyIfValid(shardingKey, timeout);
    }

    @Override
    public void setShardingKey(ShardingKey shardingKey, ShardingKey superShardingKey) throws SQLException {
        getDelegate().setShardingKey(shardingKey, superShardingKey);
    }

    @Override
    public void setShardingKey(ShardingKey shardingKey) throws SQLException {
        getDelegate().setShardingKey(shardingKey);
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return getDelegate().unwrap(aClass);
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return getDelegate().isWrapperFor(aClass);
    }

}
