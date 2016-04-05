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

package org.elasticsearch.index.query.plugin;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Weight;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
<<<<<<< HEAD
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryParser;
import org.elasticsearch.index.query.QueryParsingException;
import org.elasticsearch.indices.IndicesModule;
=======
import org.elasticsearch.index.query.*;
import org.elasticsearch.indices.query.IndicesQueriesModule;
>>>>>>> tempbranch
import org.elasticsearch.plugins.AbstractPlugin;

import java.io.IOException;

public class DummyQueryParserPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "dummy";
    }

    @Override
    public String description() {
        return "dummy query";
    }

    public void onModule(IndicesModule module) {
        module.registerQueryParser(DummyQueryParser.class);
    }

    public static class DummyQueryBuilder extends AbstractQueryBuilder<DummyQueryBuilder> {
        private static final String NAME = "dummy";

        @Override
        protected void doXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject(NAME).endObject();
        }

        @Override
        public String getWriteableName() {
            return NAME;
        }
    }

    public static class DummyQueryParser extends BaseQueryParserTemp {
        @Override
        public String[] names() {
            return new String[]{DummyQueryBuilder.NAME};
        }

        @Override
        public Query parse(QueryShardContext context) throws IOException, QueryShardException {
            XContentParser.Token token = context.parseContext().parser().nextToken();
            assert token == XContentParser.Token.END_OBJECT;
            return new DummyQuery(context.isFilter());
        }

        @Override
        public DummyQueryBuilder getBuilderPrototype() {
            return new DummyQueryBuilder();
        }
    }

    public static class DummyQuery extends Query {
        public final boolean isFilter;
        private final Query matchAllDocsQuery = new MatchAllDocsQuery();

        private DummyQuery(boolean isFilter) {
            this.isFilter = isFilter;
        }

        @Override
        public String toString(String field) {
            return getClass().getSimpleName();
        }

        @Override
        public Weight createWeight(IndexSearcher searcher, boolean needsScores) throws IOException {
            return matchAllDocsQuery.createWeight(searcher, needsScores);
        }
    }
}