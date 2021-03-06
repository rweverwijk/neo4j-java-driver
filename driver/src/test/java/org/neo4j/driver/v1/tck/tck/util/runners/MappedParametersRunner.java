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
package org.neo4j.driver.v1.tck.tck.util.runners;

import java.util.Collections;
import java.util.Map;

import org.neo4j.driver.v1.ResultCursor;
import org.neo4j.driver.v1.Value;

import static org.junit.Assert.assertNotNull;
import static org.neo4j.driver.v1.tck.DriverComplianceIT.session;

public class MappedParametersRunner implements CypherStatementRunner
{
    private String statement;
    private Map<String,Value> parameters;
    private ResultCursor result;

    public MappedParametersRunner( String st, String key, Value value )
    {
        statement = st;
        parameters = Collections.singletonMap( key, value );
    }

    public MappedParametersRunner( String st, Map<String,Value> params )
    {
        statement = st;
        parameters = params;
    }

    @Override
    public CypherStatementRunner runCypherStatement()
    {
        assertNotNull( session() );
        result = session.run( statement, parameters );
        return this;
    }

    @Override
    public ResultCursor result()
    {
        return result;
    }
}
