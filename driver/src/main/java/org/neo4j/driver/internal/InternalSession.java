/**
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.driver.internal;

import java.util.Map;

import org.neo4j.driver.internal.spi.Connection;
import org.neo4j.driver.internal.spi.Logger;
import org.neo4j.driver.internal.types.InternalTypeSystem;
import org.neo4j.driver.v1.ResultCursor;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TypeSystem;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.exceptions.ClientException;

public class InternalSession implements Session
{
    private final Connection connection;

    private final Logger logger;

    /** Called when a transaction object is closed */
    private final Runnable txCleanup = new Runnable()
    {
        @Override
        public void run()
        {
            currentTransaction = null;
        }
    };

    private InternalTransaction currentTransaction;
    private boolean isOpen = true;

    public InternalSession( Connection connection, Logger logger )
    {
        this.connection = connection;
        this.logger = logger;
    }

    @Override
    public ResultCursor run( String statementText, Map<String,Value> statementParameters )
    {
        ensureConnectionIsValid();
        InternalResultCursor cursor = new InternalResultCursor( connection, statementText, statementParameters );
        connection.run( statementText, statementParameters, cursor.runResponseCollector() );
        connection.pullAll( cursor.pullAllResponseCollector() );
        connection.flush();
        return cursor;
    }

    @Override
    public ResultCursor run( String statementTemplate )
    {
        return run( statementTemplate, ParameterSupport.NO_PARAMETERS );
    }

    @Override
    public ResultCursor run( Statement statement )
    {
        return run( statement.template(), statement.parameters() );
    }

    @Override
    public boolean isOpen()
    {
        return isOpen;
    }

    @Override
    public void close()
    {
        if( !isOpen )
        {
            throw new ClientException( "This session has already been closed." );
        }
        else
        {
            isOpen = false;
            if ( currentTransaction != null )
            {
                try
                {
                    currentTransaction.close();
                }
                catch ( Throwable e )
                {
                    // Best-effort
                }
            }
            connection.close();
        }
    }

    @Override
    public Transaction beginTransaction()
    {
        ensureConnectionIsValid();
        currentTransaction = new InternalTransaction( connection, txCleanup );
        connection.onError( new Runnable() {
            @Override
            public void run()
            {
                currentTransaction.markAsRolledBack();
                currentTransaction = null;
                connection.onError( null );
            }
        });
        return currentTransaction;
    }

    @Override
    public TypeSystem typeSystem()
    {
        return InternalTypeSystem.TYPE_SYSTEM;
    }

    private void ensureConnectionIsValid()
    {
        ensureNoOpenTransaction();
        ensureConnectionIsOpen();
    }

    @Override
    protected void finalize() throws Throwable
    {
        if( isOpen )
        {
            logger.error( "Neo4j Session object leaked, please ensure that your application calls the `close` " +
                          "method on Sessions before disposing of the objects.", null );
            connection.close();
        }
        super.finalize();
    }

    private void ensureNoOpenTransaction()
    {
        if ( currentTransaction != null )
        {
            throw new ClientException( "Please close the currently open transaction object before running " +
                                       "more statements/transactions in the current session." );
        }
    }

    private void ensureConnectionIsOpen()
    {
        if ( !connection.isOpen() )
        {
            throw new ClientException( "The current session cannot be reused as the underlying connection with the " +
                                       "server has been closed due to unrecoverable errors. " +
                                       "Please close this session and retry your statement in another new session." );
        }
    }
}
