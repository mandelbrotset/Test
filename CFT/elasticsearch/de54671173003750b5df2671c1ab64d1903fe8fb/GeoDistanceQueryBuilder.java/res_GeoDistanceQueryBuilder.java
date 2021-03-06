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

import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Locale;

public class GeoDistanceQueryBuilder extends AbstractQueryBuilder<GeoDistanceQueryBuilder> {

    public static final String NAME = "geo_distance";

    private final String name;

    private String distance;

    private double lat;

    private double lon;

    private String geohash;

    private GeoDistance geoDistance;

    private String optimizeBbox;

    static final GeoDistanceQueryBuilder PROTOTYPE = new GeoDistanceQueryBuilder(null);

    private Boolean coerce;

    private Boolean ignoreMalformed;

    public GeoDistanceQueryBuilder(String name) {
        this.name = name;
    }

    public GeoDistanceQueryBuilder point(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        return this;
    }

    public GeoDistanceQueryBuilder lat(double lat) {
        this.lat = lat;
        return this;
    }

    public GeoDistanceQueryBuilder lon(double lon) {
        this.lon = lon;
        return this;
    }

    public GeoDistanceQueryBuilder distance(String distance) {
        this.distance = distance;
        return this;
    }

    public GeoDistanceQueryBuilder distance(double distance, DistanceUnit unit) {
        this.distance = unit.toString(distance);
        return this;
    }

    public GeoDistanceQueryBuilder geohash(String geohash) {
        this.geohash = geohash;
        return this;
    }

    public GeoDistanceQueryBuilder geoDistance(GeoDistance geoDistance) {
        this.geoDistance = geoDistance;
        return this;
    }

    public GeoDistanceQueryBuilder optimizeBbox(String optimizeBbox) {
        this.optimizeBbox = optimizeBbox;
        return this;
    }

    public GeoDistanceQueryBuilder coerce(boolean coerce) {
        this.coerce = coerce;
        return this;
    }

    public GeoDistanceQueryBuilder ignoreMalformed(boolean ignoreMalformed) {
        this.ignoreMalformed = ignoreMalformed;
        return this;
    }

    @Override
    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME);
        if (geohash != null) {
            builder.field(name, geohash);
        } else {
            builder.startArray(name).value(lon).value(lat).endArray();
        }
        builder.field("distance", distance);
        if (geoDistance != null) {
            builder.field("distance_type", geoDistance.name().toLowerCase(Locale.ROOT));
        }
        if (optimizeBbox != null) {
            builder.field("optimize_bbox", optimizeBbox);
        }
        if (coerce != null) {
            builder.field("coerce", coerce);
        }
        if (ignoreMalformed != null) {
            builder.field("ignore_malformed", ignoreMalformed);
        }
        printBoostAndQueryName(builder);
        builder.endObject();
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }
}