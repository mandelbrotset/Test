/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.query;

import org.apache.lucene.search.Query;
import org.elasticsearch.action.support.ToXContentToBytes;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;

/**
 * Base class for all classes producing lucene queries.
 * Supports conversion to BytesReference and creation of lucene Query objects.
 */
public abstract class QueryBuilder extends ToXContentToBytes {

    protected QueryBuilder() {
        super(XContentType.JSON);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        doXContent(builder, params);
        builder.endObject();
        return builder;
    }

    /**
     * Converts this QueryBuilder to a lucene {@link Query}
     * @param parseContext additional information needed to construct the queries
     * @return the {@link Query}
     * @throws QueryParsingException
     * @throws IOException
     */
    //norelease to be made abstract once all query builders override toQuery providing their own specific implementation.
    public Query toQuery(QueryParseContext parseContext) throws QueryParsingException, IOException {
        return parseContext.indexQueryParserService().queryParser(parserName()).parse(parseContext);
    }

    /**
     * Temporary method that allows to retrieve the parser for each query.
     * @return the name of the parser class the default {@link #toQuery(QueryParseContext)} method delegates to
     */
    //norelease to be removed once all query builders override toQuery providing their own specific implementation.
    protected abstract String parserName();

    /**
     * Validate the query.
     * @return a {@link QueryValidationException} containing error messages, {@code null} if query is valid.
     * e.g. if fields that are needed to create the lucene query are missing.
     */
    public QueryValidationException validate() {
        // default impl does not validate, subclasses should override.
        //norelease to be possibly made abstract once all queries support validation
        return null;
    }

    protected abstract void doXContent(XContentBuilder builder, Params params) throws IOException;
}