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

package org.elasticsearch.search.aggregations.metrics;

import org.elasticsearch.search.aggregations.BaseAggregationTestCase;
import org.elasticsearch.search.aggregations.metrics.geobounds.GeoBoundsAggregator;
import org.elasticsearch.search.aggregations.metrics.geobounds.GeoBoundsAggregator.GeoBoundsAggregatorBuilder;

<<<<<<< HEAD:core/src/main/java/org/elasticsearch/search/aggregations/metrics/MetricsAggregationBuilder.java
import java.io.IOException;
import java.util.Map;

/**
 * Base builder for metrics aggregations.
 */
public abstract class MetricsAggregationBuilder<B extends MetricsAggregationBuilder<B>> extends AbstractAggregationBuilder {

    private Map<String, Object> metaData;

    public MetricsAggregationBuilder(String name, String type) {
        super(name, type);
    }
=======
public class GeoBoundsTests extends BaseAggregationTestCase<GeoBoundsAggregator.GeoBoundsAggregatorBuilder> {
>>>>>>> tempbranch:core/src/test/java/org/elasticsearch/search/aggregations/metrics/GeoBoundsTests.java

    /**
     * Sets the meta data to be included in the metric aggregator's response
     */
    public B setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
        return (B) this;
    }

    @Override
<<<<<<< HEAD:core/src/main/java/org/elasticsearch/search/aggregations/metrics/MetricsAggregationBuilder.java
    public final XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(getName());
        if (this.metaData != null) {
            builder.field("meta", this.metaData);
        }
        builder.startObject(type);
        internalXContent(builder, params);
        return builder.endObject().endObject();
=======
    protected GeoBoundsAggregatorBuilder createTestAggregatorBuilder() {
        GeoBoundsAggregatorBuilder factory = new GeoBoundsAggregatorBuilder(randomAsciiOfLengthBetween(1, 20));
        String field = randomAsciiOfLengthBetween(3, 20);
        factory.field(field);
        if (randomBoolean()) {
            factory.wrapLongitude(randomBoolean());
        }
        if (randomBoolean()) {
            factory.missing("0,0");
        }
        return factory;
>>>>>>> tempbranch:core/src/test/java/org/elasticsearch/search/aggregations/metrics/GeoBoundsTests.java
    }

}