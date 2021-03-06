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
package org.neo4j.driver.v1.tck;

import cucumber.api.CucumberOptions;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import org.neo4j.driver.v1.util.Neo4jSettings;
import org.neo4j.driver.v1.util.TestNeo4j;

/**
 * The tls tck needs to access the server rather than a session, therefore we pull tls tck outside specially
 */
@RunWith( DriverCucumberAdapter.class )
@CucumberOptions(
        features = {"target/resources/features/TransportLayerSecurity.feature"},
        tags={"@tls"},
        format = {"pretty"})
public class DriverSecurityComplianceIT
{
    @ClassRule
    public static TestNeo4j neo4j = new TestNeo4j();

    public DriverSecurityComplianceIT() throws IOException {}

    public static void updateEncryptionKeyAndCert( File key, File cert ) throws Exception
    {
        neo4j.restartServerOnEmptyDatabase(
                Neo4jSettings.DEFAULT.usingEncryptionKeyAndCert( key, cert ) );
    }

    public static void useDefaultEncryptionKeyAndCert() throws Exception
    {
        neo4j.restartServerOnEmptyDatabase( Neo4jSettings.DEFAULT );
    }
}
