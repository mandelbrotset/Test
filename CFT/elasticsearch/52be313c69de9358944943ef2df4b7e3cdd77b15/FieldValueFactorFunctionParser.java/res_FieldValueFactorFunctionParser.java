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

package org.elasticsearch.index.query.functionscore.fieldvaluefactor;

import org.apache.lucene.document.FieldType;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.common.lucene.search.function.ScoreFunction;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.fielddata.IndexNumericFieldData;
<<<<<<< HEAD
import org.elasticsearch.index.fielddata.plain.DoubleArrayIndexFieldData;
import org.elasticsearch.index.mapper.FieldMapper;
=======
>>>>>>> tempbranch
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryParsingException;
import org.elasticsearch.index.query.functionscore.ScoreFunctionParser;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;
import java.util.Locale;

/**
 * Parses out a function_score function that looks like:
 *
 * <pre>
 *     {
 *         "field_value_factor": {
 *             "field": "myfield",
 *             "factor": 1.5,
 *             "modifier": "square",
 *             "missing": 1
 *         }
 *     }
 * </pre>
 */
public class FieldValueFactorFunctionParser implements ScoreFunctionParser {
    public static String[] NAMES = { "field_value_factor", "fieldValueFactor" };

    @Override
    public ScoreFunction parse(QueryShardContext context, XContentParser parser) throws IOException, QueryParsingException {
        QueryParseContext parseContext = context.parseContext();

        String currentFieldName = null;
        String field = null;
        float boostFactor = 1;
        FieldValueFactorFunction.Modifier modifier = FieldValueFactorFunction.Modifier.NONE;
        Double missing = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token.isValue()) {
                if ("field".equals(currentFieldName)) {
                    field = parser.text();
                } else if ("factor".equals(currentFieldName)) {
                    boostFactor = parser.floatValue();
                } else if ("modifier".equals(currentFieldName)) {
                    modifier = FieldValueFactorFunction.Modifier.valueOf(parser.text().toUpperCase(Locale.ROOT));
                } else if ("missing".equals(currentFieldName)) {
                    missing = parser.doubleValue();
                } else {
                    throw new QueryParsingException(parseContext, NAMES[0] + " query does not support [" + currentFieldName + "]");
                }
            } else if("factor".equals(currentFieldName) && (token == XContentParser.Token.START_ARRAY || token == XContentParser.Token.START_OBJECT)) {
                throw new QueryParsingException(parseContext, "[" + NAMES[0] + "] field 'factor' does not support lists or objects");
            }
        }

        if (field == null) {
            throw new QueryParsingException(parseContext, "[" + NAMES[0] + "] required field 'field' missing");
        }

        SearchContext searchContext = SearchContext.current();
        MappedFieldType fieldType = searchContext.mapperService().smartNameFieldType(field);
        IndexNumericFieldData fieldData = null;
        if (fieldType == null) {
            if(missing == null) {
                throw new ElasticsearchException("Unable to find a field mapper for field [" + field + "]. No 'missing' value defined.");
            }
        } else {
            fieldData = searchContext.fieldData().getForField(fieldType);
        }
        return new FieldValueFactorFunction(field, boostFactor, modifier, missing, fieldData);
    }

    @Override
    public String[] getNames() {
        return NAMES;
    }
}