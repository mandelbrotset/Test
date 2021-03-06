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

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.Script.ScriptField;
import org.elasticsearch.script.ScriptParameterParser;
import org.elasticsearch.script.ScriptParameterParser.ScriptParameterValue;
import org.elasticsearch.script.SearchScript;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static com.google.common.collect.Maps.newHashMap;


/**
 * Parser for script query
 */
public class ScriptQueryParser extends BaseQueryParser<ScriptQueryBuilder> {

    @Inject
    public ScriptQueryParser() {
    }

    @Override
    public String[] names() {
        return new String[]{ScriptQueryBuilder.NAME};
    }

    @Override
    public ScriptQueryBuilder fromXContent(QueryParseContext parseContext) throws IOException, QueryParsingException {
        XContentParser parser = parseContext.parser();
        ScriptParameterParser scriptParameterParser = new ScriptParameterParser();
        
        // also, when caching, since its isCacheable is false, will result in loading all bit set...
        Script script = null;
        Map<String, Object> params = null;

        float boost = AbstractQueryBuilder.DEFAULT_BOOST;
        String queryName = null;

        XContentParser.Token token;
        String currentFieldName = null;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (parseContext.isDeprecatedSetting(currentFieldName)) {
                // skip
            } else if (token == XContentParser.Token.START_OBJECT) {
                if (parseContext.parseFieldMatcher().match(currentFieldName, ScriptField.SCRIPT)) {
                    script = Script.parse(parser, parseContext.parseFieldMatcher());
                } else if ("params".equals(currentFieldName)) { // TODO remove in 3.0 (here to support old script APIs)
                    params = parser.map();
                } else {
                    throw new QueryParsingException(parseContext, "[script] query does not support [" + currentFieldName + "]");
                }
            } else if (token.isValue()) {
                if ("_name".equals(currentFieldName)) {
                    queryName = parser.text();
                } else if ("boost".equals(currentFieldName)) {
                    boost = parser.floatValue();
                } else if (!scriptParameterParser.token(currentFieldName, token, parser, parseContext.parseFieldMatcher())) {
                    throw new QueryParsingException(parseContext, "[script] query does not support [" + currentFieldName + "]");
                }
            }
        }

        if (script == null) { // Didn't find anything using the new API so try using the old one instead
            ScriptParameterValue scriptValue = scriptParameterParser.getDefaultScriptParameterValue();
            if (scriptValue != null) {
                if (params == null) {
                    params = newHashMap();
                }
                script = new Script(scriptValue.script(), scriptValue.scriptType(), scriptParameterParser.lang(), params);
            }
        } else if (params != null) {
            throw new QueryParsingException(parseContext, "script params must be specified inside script object in a [script] filter");
        }

        if (script == null) {
            throw new QueryParsingException(parseContext, "script must be provided with a [script] filter");
        }

        return new ScriptQueryBuilder(script)
                .boost(boost)
                .queryName(queryName);
    }

    @Override
    public ScriptQueryBuilder getBuilderPrototype() {
        return ScriptQueryBuilder.PROTOTYPE;
    }
}