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
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.GeoUtils;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentParser.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * {
 *     "pin.location" : {
 *         "points" : [
 *              { "lat" : 12, "lon" : 40},
 *              {}
 *         ]
 *     }
 * }
 * </pre>
 */
public class GeoPolygonQueryParser extends BaseQueryParser<GeoPolygonQueryBuilder> {

    public static final ParseField COERCE_FIELD = new ParseField("coerce", "normalize");
    public static final ParseField IGNORE_MALFORMED_FIELD = new ParseField("ignore_malformed");
    public static final ParseField POINTS_FIELD = new ParseField("points");

    @Override
    public String[] names() {
        return new String[]{GeoPolygonQueryBuilder.NAME, "geoPolygon"};
    }

    @Override
    public GeoPolygonQueryBuilder fromXContent(QueryParseContext parseContext) throws IOException {
        XContentParser parser = parseContext.parser();

        String fieldName = null;

        List<GeoPoint> shell = null;

        Float boost = null;
        Boolean coerce = null;
        Boolean ignoreMalformed = null;
        String queryName = null;
        String currentFieldName = null;
        XContentParser.Token token;

        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (parseContext.isDeprecatedSetting(currentFieldName)) {
                // skip
            } else if (token == XContentParser.Token.START_OBJECT) {
                fieldName = currentFieldName;

                while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                    if (token == XContentParser.Token.FIELD_NAME) {
                        currentFieldName = parser.currentName();
                    } else if (token == XContentParser.Token.START_ARRAY) {
                        if (parseContext.parseFieldMatcher().match(currentFieldName, POINTS_FIELD)) {
                            shell = new ArrayList<GeoPoint>();
                            while ((token = parser.nextToken()) != Token.END_ARRAY) {
                                shell.add(GeoUtils.parseGeoPoint(parser));
                            }
                        } else {
                            throw new ParsingException(parseContext, "[geo_polygon] query does not support [" + currentFieldName
                                    + "]");
                        }
                    } else {
                        throw new ParsingException(parseContext, "[geo_polygon] query does not support token type [" + token.name()
                                + "] under [" + currentFieldName + "]");
                    }
                }
            } else if (token.isValue()) {
                if ("_name".equals(currentFieldName)) {
                    queryName = parser.text();
                } else if ("boost".equals(currentFieldName)) {
                    boost = parser.floatValue();
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, COERCE_FIELD)) {
                    coerce = parser.booleanValue();
                    if (coerce == true) {
                        ignoreMalformed = true;
                    }
                } else if (parseContext.parseFieldMatcher().match(currentFieldName, IGNORE_MALFORMED_FIELD)) {
                    ignoreMalformed = parser.booleanValue();
                } else {
                    throw new ParsingException(parseContext, "[geo_polygon] query does not support [" + currentFieldName + "]");
                }
            } else {
                throw new ParsingException(parseContext, "[geo_polygon] unexpected token type [" + token.name() + "]");
            }
        }
        GeoPolygonQueryBuilder builder = new GeoPolygonQueryBuilder(fieldName, shell);
        if (coerce != null) {
            builder.coerce(coerce);
        }
        if (ignoreMalformed != null) {
            builder.ignoreMalformed(ignoreMalformed);
        }
        if (queryName != null) {
            builder.queryName(queryName);
        }
        if (boost != null) {
            builder.boost(boost);
        }
        return builder;
    }

    @Override
    public GeoPolygonQueryBuilder getBuilderPrototype() {
        return GeoPolygonQueryBuilder.PROTOTYPE;
    }
}