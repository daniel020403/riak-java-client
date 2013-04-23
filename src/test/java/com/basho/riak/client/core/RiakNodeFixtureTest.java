/*
 * Copyright 2013 Brian Roach <roach at basho dot com>.
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
package com.basho.riak.client.core;

import com.basho.riak.client.RiakObject;
import com.basho.riak.client.core.converters.GetRespConverter;
import com.basho.riak.client.core.fixture.NetworkTestFixture;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 *
 * @author Brian Roach <roach at basho dot com>
 * @since 2.0
 */
public class RiakNodeFixtureTest extends FixtureTest
{
    
    
    @Test
    public void operationSuccess() throws UnknownHostException, InterruptedException, ExecutionException
    {
        RiakNode node = 
            new RiakNode.Builder(Protocol.HTTP)
                        .withRemoteAddress("127.0.0.1")
                        .withPort(Protocol.HTTP, NetworkTestFixture.HTTP_FULL_WRITE_STAY_OPEN)
                        .build();
        
        node.start();
        GetOperation operation = new GetOperation();
        boolean accepted = node.execute(operation);
        assertTrue(accepted);
        RiakObject response = operation.get();
        assertEquals(response.getValueAsString().trim(), "not found");
    }
    
    @Test(expected=ExecutionException.class)
    public void operationFail() throws UnknownHostException, InterruptedException, ExecutionException
    {
        RiakNode node = 
            new RiakNode.Builder(Protocol.HTTP)
                        .withRemoteAddress("127.0.0.1")
                        .withPort(Protocol.HTTP, NetworkTestFixture.ACCEPT_THEN_CLOSE)
                        .build();
        node.start();
        GetOperation operation = new GetOperation();
        boolean accepted = node.execute(operation);
        RiakObject response = operation.get();
    }
    
    @Test
    public void nodeChangesStateOnPoolState() throws UnknownHostException, IOException, InterruptedException
    {
        RiakNode node = 
            new RiakNode.Builder(Protocol.HTTP)
                        .withRemoteAddress("127.0.0.1")
                        .withMinConnections(Protocol.HTTP, 10)
                        .withPort(Protocol.HTTP, NetworkTestFixture.PB_FULL_WRITE_STAY_OPEN)
                        .build();

        node.start();
        NodeStateListener mockListener = mock(NodeStateListener.class);
        node.addStateListener(mockListener);
        
        try
        {
            fixture.shutdown();
            Thread.sleep(2000);
            verify(mockListener).nodeStateChanged(node, RiakNode.State.HEALTH_CHECKING);
            assertEquals(RiakNode.State.HEALTH_CHECKING, node.getNodeState());
        }
        finally
        {
            fixture = new NetworkTestFixture();
            new Thread(fixture).start();
        }
        
    }
    
    
    static class GetOperation extends FutureOperation<RiakObject>
    {
        @Override
        protected RiakObject convert(RiakResponse rawResponse)
        {
            RiakObject ro = rawResponse.convertResponse(new GetRespConverter());
            return ro;
        }

        @Override
        protected Object createChannelMessage(Protocol p)
        {
            HttpRequest message = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
            message.headers().set(HttpHeaders.Names.HOST, "localhost");
            return message;
        }
    }
}