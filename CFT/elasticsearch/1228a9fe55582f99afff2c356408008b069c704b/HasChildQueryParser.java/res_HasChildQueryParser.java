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

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.support.QueryInnerHits;

import java.io.IOException;

/**
 * A query parser for <tt>has_child</tt> queries.
 */
public class HasChildQueryParser extends BaseQueryParser {

    private static final ParseField QUERY_FIELD = new ParseField("query", "filter");

    @Override
    public String[] names() {
        return new String[] { HasChildQueryBuilder.NAME, Strings.toCamelCase(HasChildQueryBuilder.NAME) };
    }

    @Override
    public QueryBuilder fromXContent(QueryParseContext parseContext) throws IOException {
        XContentParser parser = parseContext.parser();
        float boost = AbstractQueryBuilder.DEFAULT_BOOST;
        String childType = null;
        ScoreMode scoreMode = HasChildQueryBuilder.DEFAULT_SCORE_MODE;
        int minChildren = HasChildQueryBuilder.DEFAULT_MIN_CHILDREN;
        int maxChildren = HasChildQueryBuilder.DEFAULT_MAX_CHILDREN;
        String queryName = null;
        QueryInnerHits queryInnerHits = null;
        String currentFieldName = null;
        XContentParser.Token token;
        QueryBuilder iqb = null;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (parseContext.isDeprecatedSetting(currentFieldName)) {
                // skip
            } else if (token == XContentParser.Token.START_OBJECT) {
                if (parseContext.parseFieldMatcher().match(currentFieldName, QUERY_FIELD)) {
                    iqb = parseContext.parseInnerQueryBuilder();
                } else if ("inner_hits".equals(currentFieldName)) {
                    queryInnerHits = new QueryInnerHits(parser);
                } else {
                    throw new ParsingException(parseContext, "[has_child] query does not support [" + currentFieldName + "]");
                }
            } else if (token.isValue()) {
                if ("type".equals(currentFieldName) || "child_type".equals(currentFieldName) || "childType".equals(currentFieldName)) {
                    childType = parser.text();
                } else if ("score_mode".equals(currentFieldName) || "scoreMode".equals(currentFieldName)) {
                    scoreMode = parseScoreMode(parser.text());
                } else if ("boost".equals(currentFieldName)) {
                    boost = parser.floatValue();
                } else if ("min_children".equals(currentFieldName) || "minChildren".equals(currentFieldName)) {
                    minChildren = parser.intValue(true);
                } else if ("max_children".equals(currentFieldName) || "maxChildren".equals(currentFieldName)) {
                    maxChildren = parser.intValue(true);
                } else if ("_name".equals(currentFieldName)) {
                    queryName = parser.text();
                } else {
                    throw new ParsingException(parseContext, "[has_child] query does not support [" + currentFieldName + "]");
                }
            }
        }
        HasChildQueryBuilder hasChildQueryBuilder = new HasChildQueryBuilder(childType, iqb, maxChildren, minChildren, scoreMode, queryInnerHits);
        hasChildQueryBuilder.queryName(queryName);
        hasChildQueryBuilder.boost(boost);
        return hasChildQueryBuilder;
    }

    public static ScoreMode parseScoreMode(String scoreModeString) {
        if ("none".equals(scoreModeString)) {
            return ScoreMode.None;
        } else if ("min".equals(scoreModeString)) {
            return ScoreMode.Min;
        } else if ("max".equals(scoreModeString)) {
            return ScoreMode.Max;
        } else if ("avg".equals(scoreModeString)) {
            return ScoreMode.Avg;
        } else if ("total".equals(scoreModeString)) {
            return ScoreMode.Total;
        }
        throw new IllegalArgumentException("No score mode for child query [" + scoreModeString + "] found");
    }

    @Override
    public HasChildQueryBuilder getBuilderPrototype() {
        return HasChildQueryBuilder.PROTOTYPE;
    }
}