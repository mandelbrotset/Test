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
package org.elasticsearch.search.suggest;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.util.CollectionUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.search.suggest.SuggestionSearchContext.SuggestionContext;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.ESIntegTestCase.ClusterScope;
import org.elasticsearch.test.ESIntegTestCase.Scope;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 *
 */
@ClusterScope(scope= Scope.SUITE, numDataNodes =1)
public class CustomSuggesterSearchIT extends ESIntegTestCase {
    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return pluginList(CustomSuggesterPlugin.class);
    }

    public void testThatCustomSuggestersCanBeRegisteredAndWork() throws Exception {
        createIndex("test");
        client().prepareIndex("test", "test", "1").setSource(jsonBuilder()
                .startObject()
                .field("name", "arbitrary content")
                .endObject())
                .setRefresh(true).execute().actionGet();
        ensureYellow();

        String randomText = randomAsciiOfLength(10);
        String randomField = randomAsciiOfLength(10);
        String randomSuffix = randomAsciiOfLength(10);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion(new CustomSuggestionBuilder("someName", randomField, randomSuffix).text(randomText));
        SearchRequestBuilder searchRequestBuilder = client().prepareSearch("test").setTypes("test").setFrom(0).setSize(1)
                .suggest(suggestBuilder);

        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

        // TODO: infer type once JI-9019884 is fixed
        // TODO: see also JDK-8039214
        List<Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> suggestions
                = CollectionUtils.<Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>>iterableAsArrayList(searchResponse.getSuggest().getSuggestion("someName"));
        assertThat(suggestions, hasSize(2));
        assertThat(suggestions.get(0).getText().string(), is(String.format(Locale.ROOT, "%s-%s-%s-12", randomText, randomField, randomSuffix)));
        assertThat(suggestions.get(1).getText().string(), is(String.format(Locale.ROOT, "%s-%s-%s-123", randomText, randomField, randomSuffix)));
    }

    static class CustomSuggestionBuilder extends SuggestionBuilder<CustomSuggestionBuilder> {

        public final static CustomSuggestionBuilder PROTOTYPE = new CustomSuggestionBuilder("_na_", "_na_", "_na_");

        private String randomField;
        private String randomSuffix;

        public CustomSuggestionBuilder(String name, String randomField, String randomSuffix) {
            super(name);
            this.randomField = randomField;
            this.randomSuffix = randomSuffix;
        }

        @Override
        protected XContentBuilder innerToXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field("field", randomField);
            builder.field("suffix", randomSuffix);
            return builder;
        }

        @Override
        public String getWriteableName() {
            return "custom";
        }

        @Override
        public void doWriteTo(StreamOutput out) throws IOException {
            out.writeString(randomField);
            out.writeString(randomSuffix);
        }

        @Override
        public CustomSuggestionBuilder doReadFrom(StreamInput in, String name) throws IOException {
            return new CustomSuggestionBuilder(in.readString(), in.readString(), in.readString());
        }

        @Override
        protected boolean doEquals(CustomSuggestionBuilder other) {
            return Objects.equals(randomField, other.randomField) &&
                    Objects.equals(randomSuffix, other.randomSuffix);
        }

        @Override
        protected int doHashCode() {
            return Objects.hash(randomField, randomSuffix);
        }

        @Override
        protected CustomSuggestionBuilder innerFromXContent(QueryParseContext parseContext, String name)
                throws IOException {
            // TODO some parsing
            return new CustomSuggestionBuilder(name, randomField, randomSuffix);
        }

        @Override
        protected SuggestionContext innerBuild(QueryShardContext context) throws IOException {
            Map<String, Object> options = new HashMap<>();
            options.put("field", randomField);
            options.put("suffix", randomSuffix);
            return new CustomSuggester.CustomSuggestionsContext(new CustomSuggester(), options);
        }

    }

}