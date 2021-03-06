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

import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;

/**
 *
 */
public class NotQueryParser extends BaseQueryParser {

    private static final ParseField QUERY_FIELD = new ParseField("filter", "query");

    @Inject
    public NotQueryParser() {
    }

    @Override
    public String[] names() {
        return new String[]{NotQueryBuilder.NAME};
    }

    @Override
    public QueryBuilder fromXContent(QueryParseContext parseContext) throws IOException, QueryParsingException {
        XContentParser parser = parseContext.parser();

        QueryBuilder query = null;
        boolean queryFound = false;

        String queryName = null;
        String currentFieldName = null;
        float boost = AbstractQueryBuilder.DEFAULT_BOOST;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (parseContext.isDeprecatedSetting(currentFieldName)) {
                // skip
            } else if (token == XContentParser.Token.START_OBJECT) {
                if (QUERY_FIELD.match(currentFieldName)) {
                    query = parseContext.parseInnerFilterToQueryBuilder();
                    queryFound = true;
                } else {
                    queryFound = true;
                    // its the filter, and the name is the field
                    query = parseContext.parseInnerFilterToQueryBuilder(currentFieldName);
                }
            } else if (token == XContentParser.Token.START_ARRAY) {
                queryFound = true;
                // its the filter, and the name is the field
                query = parseContext.parseInnerFilterToQueryBuilder(currentFieldName);
            } else if (token.isValue()) {
                if ("_name".equals(currentFieldName)) {
                    queryName = parser.text();
                } else if ("boost".equals(currentFieldName)) {
                    boost = parser.floatValue();
                } else {
                    throw new QueryParsingException(parseContext, "[not] query does not support [" + currentFieldName + "]");
                }
            }
        }

        if (!queryFound) {
            throw new QueryParsingException(parseContext, "filter is required when using `not` query");
        }

        NotQueryBuilder notQueryBuilder = new NotQueryBuilder(query);
        notQueryBuilder.queryName(queryName);
        notQueryBuilder.boost(boost);
        return notQueryBuilder;
    }

    @Override
    public NotQueryBuilder getBuilderPrototype() {
        return NotQueryBuilder.PROTOTYPE;
    }
}