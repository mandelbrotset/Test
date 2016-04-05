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

<<<<<<< HEAD
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiDocValues;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;
=======
>>>>>>> tempbranch
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentParser;
<<<<<<< HEAD
import org.elasticsearch.index.fielddata.IndexParentChildFieldData;
import org.elasticsearch.index.fielddata.plain.ParentChildIndexFieldData;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.internal.ParentFieldMapper;
import org.elasticsearch.index.query.support.InnerHitsQueryParserHelper;
import org.elasticsearch.index.query.support.XContentStructure;
import org.elasticsearch.search.fetch.innerhits.InnerHitsContext;
import org.elasticsearch.search.fetch.innerhits.InnerHitsSubSearchContext;
import org.elasticsearch.search.internal.SearchContext;
=======
import org.elasticsearch.index.query.support.QueryInnerHits;
import org.elasticsearch.index.search.child.ScoreType;
>>>>>>> tempbranch

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
    public QueryBuilder fromXContent(QueryParseContext parseContext) throws IOException, QueryParsingException {
        XContentParser parser = parseContext.parser();
        float boost = AbstractQueryBuilder.DEFAULT_BOOST;
        String childType = null;
        ScoreType scoreType = ScoreType.NONE;
<<<<<<< HEAD
        int minChildren = 0;
        int maxChildren = 0;
=======
        int minChildren = HasChildQueryBuilder.DEFAULT_MIN_CHILDREN;
        int maxChildren = HasChildQueryBuilder.DEFAULT_MAX_CHILDREN;
        int shortCircuitParentDocSet = HasChildQueryBuilder.DEFAULT_SHORT_CIRCUIT_CUTOFF;
>>>>>>> tempbranch
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
                    throw new QueryParsingException(parseContext, "[has_child] query does not support [" + currentFieldName + "]");
                }
            } else if (token.isValue()) {
                if ("type".equals(currentFieldName) || "child_type".equals(currentFieldName) || "childType".equals(currentFieldName)) {
                    childType = parser.text();
                } else if ("score_type".equals(currentFieldName) || "scoreType".equals(currentFieldName)) {
                    scoreType = ScoreType.fromString(parser.text());
                } else if ("score_mode".equals(currentFieldName) || "scoreMode".equals(currentFieldName)) {
                    scoreType = ScoreType.fromString(parser.text());
                } else if ("boost".equals(currentFieldName)) {
                    boost = parser.floatValue();
                } else if ("min_children".equals(currentFieldName) || "minChildren".equals(currentFieldName)) {
                    minChildren = parser.intValue(true);
                } else if ("max_children".equals(currentFieldName) || "maxChildren".equals(currentFieldName)) {
                    maxChildren = parser.intValue(true);
                } else if ("_name".equals(currentFieldName)) {
                    queryName = parser.text();
                } else {
                    throw new QueryParsingException(parseContext, "[has_child] query does not support [" + currentFieldName + "]");
                }
            }
        }
<<<<<<< HEAD
        if (!queryFound) {
            throw new QueryParsingException(parseContext, "[has_child] requires 'query' field");
        }
        if (childType == null) {
            throw new QueryParsingException(parseContext, "[has_child] requires 'type' field");
        }

        Query innerQuery = iq.asQuery(childType);

        if (innerQuery == null) {
            return null;
        }
        innerQuery.setBoost(boost);

        DocumentMapper childDocMapper = parseContext.mapperService().documentMapper(childType);
        if (childDocMapper == null) {
            throw new QueryParsingException(parseContext, "[has_child] No mapping for for type [" + childType + "]");
        }
        ParentFieldMapper parentFieldMapper = childDocMapper.parentFieldMapper();
        if (parentFieldMapper.active() == false) {
            throw new QueryParsingException(parseContext, "[has_child] _parent field has no parent type configured");
        }

        if (innerHits != null) {
            ParsedQuery parsedQuery = new ParsedQuery(innerQuery, parseContext.copyNamedQueries());
            InnerHitsContext.ParentChildInnerHits parentChildInnerHits = new InnerHitsContext.ParentChildInnerHits(innerHits.getSubSearchContext(), parsedQuery, null, parseContext.mapperService(), childDocMapper);
            String name = innerHits.getName() != null ? innerHits.getName() : childType;
            parseContext.addInnerHits(name, parentChildInnerHits);
        }

        String parentType = parentFieldMapper.type();
        DocumentMapper parentDocMapper = parseContext.mapperService().documentMapper(parentType);
        if (parentDocMapper == null) {
            throw new QueryParsingException(parseContext, "[has_child]  Type [" + childType + "] points to a non existent parent type ["
                    + parentType + "]");
        }

        if (maxChildren > 0 && maxChildren < minChildren) {
            throw new QueryParsingException(parseContext, "[has_child] 'max_children' is less than 'min_children'");
        }

        // wrap the query with type query
        innerQuery = Queries.filtered(innerQuery, childDocMapper.typeFilter());

        final Query query;
        final ParentChildIndexFieldData parentChildIndexFieldData = parseContext.getForField(parentFieldMapper.fieldType());
        query = joinUtilHelper(parentType, parentChildIndexFieldData, parentDocMapper.typeFilter(), scoreType, innerQuery, minChildren, maxChildren);
        if (queryName != null) {
            parseContext.addNamedQuery(queryName, query);
        }
        query.setBoost(boost);
        return query;
    }

    public static Query joinUtilHelper(String parentType, ParentChildIndexFieldData parentChildIndexFieldData, Query toQuery, ScoreType scoreType, Query innerQuery, int minChildren, int maxChildren) throws IOException {
        ScoreMode scoreMode;
        // TODO: move entirely over from ScoreType to org.apache.lucene.join.ScoreMode, when we drop the 1.x parent child code.
        switch (scoreType) {
            case NONE:
                scoreMode = ScoreMode.None;
                break;
            case MIN:
                scoreMode = ScoreMode.Min;
                break;
            case MAX:
                scoreMode = ScoreMode.Max;
                break;
            case SUM:
                scoreMode = ScoreMode.Total;
                break;
            case AVG:
                scoreMode = ScoreMode.Avg;
                break;
            default:
                throw new UnsupportedOperationException("score type [" + scoreType + "] not supported");
        }
        // 0 in pre 2.x p/c impl means unbounded
        if (maxChildren == 0) {
            maxChildren = Integer.MAX_VALUE;
        }
        return new LateParsingQuery(toQuery, innerQuery, minChildren, maxChildren, parentType, scoreMode, parentChildIndexFieldData);
=======
        HasChildQueryBuilder hasChildQueryBuilder = new HasChildQueryBuilder(childType, iqb, maxChildren, minChildren, shortCircuitParentDocSet, scoreType, queryInnerHits);
        hasChildQueryBuilder.queryName(queryName);
        hasChildQueryBuilder.boost(boost);
        return hasChildQueryBuilder;
>>>>>>> tempbranch
    }

    @Override
    public HasChildQueryBuilder getBuilderPrototype() {
        return HasChildQueryBuilder.PROTOTYPE;
    }
}