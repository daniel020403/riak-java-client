/*
 * Copyright 2013 Basho Technologies Inc
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
package com.basho.riak.client.operations;

import com.basho.riak.client.util.ByteArrayWrapper;

import java.nio.ByteBuffer;

public abstract class IndexType<T>
{

    public static final IndexType<Integer> INT = new IndexType<Integer>()
    {
        @Override
        public Integer convert(ByteArrayWrapper input)
        {
            return ByteBuffer.wrap(input.unsafeGetValue()).getInt();
        }
    };

    public static final IndexType<ByteArrayWrapper> BIN = new IndexType<ByteArrayWrapper>()
    {
        @Override
        public ByteArrayWrapper convert(ByteArrayWrapper input)
        {
            return input;
        }
    };

    abstract T convert(ByteArrayWrapper input);

}
