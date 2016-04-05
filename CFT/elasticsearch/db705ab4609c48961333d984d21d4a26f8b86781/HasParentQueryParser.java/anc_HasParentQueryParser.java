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

import org.apache.lucene.search.*;
<<<<<<< HEAD
=======
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.Version;
>>>>>>> tempbranch
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.lucene.search.Queries;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.fielddata.plain.ParentChildIndexFieldData;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.internal.ParentFieldMapper;
import org.elasticsearch.index.query.support.InnerHitsQueryParserHelper;
import org.elasticsearch.index.query.support.QueryInnerHits;
import org.elasticsearch.index.query.support.XContentStructure;
import org.elasticsearch.search.fetch.innerhits.InnerHitsContext;
import org.elasticsearch.search.fetch.innerhits.InnerHitsSubSearchContext;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


public class HasParentQueryParser extends BaseQueryParser  {

    private static final HasParentQueryBuilder PROTOTYPE = new HasParentQueryBuilder("", EmptyQueryBuilder.PROTOTYPE);
    private static final ParseField QUERY_FIELD = new ParseField("query", "filter");
    private static final ParseField SCORE_FIELD = new ParseField("score_type", "score_mode").withAllDeprecated("score");
    private static final ParseField TYPE_FIELD = new ParseField("parent_type", "type");

    @Override
    public String[] names() {
        return new String[]{HasParentQueryBuilder.NAME, Strings.toCamelCase(HasParentQueryBuilder.NAME)};
    }

    @Override
    public QueryBuilder fromXContent(QueryParseContext parseContext) throws IOException, QueryParsingException {
        XContentParser parser = parseContext.parser();

        float boost = AbstractQueryBuilder.DEFAULT_BOOST;
        String parentType = null;
        boolean score = false;
        String queryName = null;
        QueryInnerHits innerHits = null;

        String currentFieldName = null;
        XContentParser.Token token;
        QueryBuilder iqb = null;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_OBJECT) {
                // Usually, the query would be parsed here, but the child
                // type may not have been extracted yet, so use the
                // XContentStructure.<type> facade to parse if available,
                // or delay parsing if not.
                if (parseContext.parseFieldMatcher().match(currentFieldName, QUERY_FIELD)) {
                    iqb = parseContext.parseInnerQueryBuilder();
                } else if ("inner_hits".equals(currentFieldName)) {
                    innerHits = new QueryInnerHits(parser);
                } else {
                    throw new QueryParsingException(parseContext, "[has_parent] query does not support [" + currentFieldName + "]");
                }
            } else if (token.isValue()) {
                if (parseContext.parseFieldMatcher().match(currentFieldName, TYPE_FIELD)) {
                    parentType = parser.text();
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, SCORE_FIELD)) {
                    // deprecated we use a boolean now
                    String scoreTypeValue = parser.text();
                    if ("score".equals(scoreTypeValue)) {
                        score = true;
                    } else if ("none".equals(scoreTypeValue)) {
                        score = false;
                    }
                } else if ("score".equals(currentFieldName)) {
                    score = parser.booleanValue();
                } else if ("boost".equals(currentFieldName)) {
                    boost = parser.floatValue();
                } else if ("_name".equals(currentFieldName)) {
                    queryName = parser.text();
                } else {
                    throw new QueryParsingException(parseContext, "[has_parent] query does not support [" + currentFieldName + "]");
                }
            }
        }
        return new HasParentQueryBuilder(parentType, iqb, score, innerHits).queryName(queryName).boost(boost);
    }

<<<<<<< HEAD
    static Query createParentQuery(Query innerQuery, String parentType, boolean score, QueryParseContext parseContext, InnerHitsSubSearchContext innerHits) throws IOException {
        DocumentMapper parentDocMapper = parseContext.mapperService().documentMapper(parentType);
        if (parentDocMapper == null) {
            throw new QueryParsingException(parseContext, "[has_parent] query configured 'parent_type' [" + parentType
                    + "] is not a valid type");
        }

        if (innerHits != null) {
            ParsedQuery parsedQuery = new ParsedQuery(innerQuery, parseContext.copyNamedQueries());
            InnerHitsContext.ParentChildInnerHits parentChildInnerHits = new InnerHitsContext.ParentChildInnerHits(innerHits.getSubSearchContext(), parsedQuery, null, parseContext.mapperService(), parentDocMapper);
            String name = innerHits.getName() != null ? innerHits.getName() : parentType;
            parseContext.addInnerHits(name, parentChildInnerHits);
        }

        Set<String> parentTypes = new HashSet<>(5);
        parentTypes.add(parentDocMapper.type());
        ParentChildIndexFieldData parentChildIndexFieldData = null;
        for (DocumentMapper documentMapper : parseContext.mapperService().docMappers(false)) {
            ParentFieldMapper parentFieldMapper = documentMapper.parentFieldMapper();
            if (parentFieldMapper.active()) {
                DocumentMapper parentTypeDocumentMapper = parseContext.mapperService().documentMapper(parentFieldMapper.type());
                parentChildIndexFieldData = parseContext.getForField(parentFieldMapper.fieldType());
                if (parentTypeDocumentMapper == null) {
                    // Only add this, if this parentFieldMapper (also a parent)  isn't a child of another parent.
                    parentTypes.add(parentFieldMapper.type());
                }
            }
        }
        if (parentChildIndexFieldData == null) {
            throw new QueryParsingException(parseContext, "[has_parent] no _parent field configured");
        }

        Query parentTypeQuery = null;
        if (parentTypes.size() == 1) {
            DocumentMapper documentMapper = parseContext.mapperService().documentMapper(parentTypes.iterator().next());
            if (documentMapper != null) {
                parentTypeQuery = documentMapper.typeFilter();
            }
        } else {
            BooleanQuery.Builder parentsFilter = new BooleanQuery.Builder();
            for (String parentTypeStr : parentTypes) {
                DocumentMapper documentMapper = parseContext.mapperService().documentMapper(parentTypeStr);
                if (documentMapper != null) {
                    parentsFilter.add(documentMapper.typeFilter(), BooleanClause.Occur.SHOULD);
                }
            }
            parentTypeQuery = parentsFilter.build();
        }

        if (parentTypeQuery == null) {
            return null;
        }

        // wrap the query with type query
        innerQuery = Queries.filtered(innerQuery, parentDocMapper.typeFilter());
        Query childrenFilter = Queries.not(parentTypeQuery);
        ScoreType scoreMode = score ? ScoreType.MAX : ScoreType.NONE;
        return joinUtilHelper(parentType, parentChildIndexFieldData, childrenFilter, scoreMode, innerQuery, 0, Integer.MAX_VALUE);
=======
    @Override
    public HasParentQueryBuilder getBuilderPrototype() {
        return PROTOTYPE;
>>>>>>> tempbranch
    }
}