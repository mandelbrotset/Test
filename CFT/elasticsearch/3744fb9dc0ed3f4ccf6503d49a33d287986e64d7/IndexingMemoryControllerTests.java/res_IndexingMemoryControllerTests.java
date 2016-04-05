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
package org.elasticsearch.indices.memory;

import java.util.*;

import org.apache.lucene.index.DirectoryReader;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.test.ESSingleNodeTestCase;

<<<<<<< HEAD
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

=======
>>>>>>> tempbranch
import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_REPLICAS;
import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_SHARDS;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertNoFailures;
import static org.hamcrest.Matchers.equalTo;

public class IndexingMemoryControllerTests extends ESSingleNodeTestCase {

    static class MockController extends IndexingMemoryController {

<<<<<<< HEAD
        final static ByteSizeValue INACTIVE = new ByteSizeValue(-1);

        final Map<IndexShard, ByteSizeValue> indexingBuffers = new HashMap<>();
=======
        // Size of each shard's indexing buffer
        final Map<IndexShard, Long> indexBufferRAMBytesUsed = new HashMap<>();
>>>>>>> tempbranch

        // How many bytes this shard is currently moving to disk
        final Map<IndexShard, Long> writingBytes = new HashMap<>();

        // Shards that are currently throttled
        final Set<IndexShard> throttled = new HashSet<>();

        public MockController(Settings settings) {
            super(Settings.builder()
                            .put(SHARD_MEMORY_INTERVAL_TIME_SETTING, "200h") // disable it
                            .put(settings)
                            .build(),
                    null, null, 100 * 1024 * 1024); // fix jvm mem size to 100mb
        }

<<<<<<< HEAD
        public void deleteShard(IndexShard id) {
            indexingBuffers.remove(id);
        }

        public void assertBuffers(IndexShard id, ByteSizeValue indexing) {
            assertThat(indexingBuffers.get(id), equalTo(indexing));
        }

        public void assertInactive(IndexShard id) {
            assertThat(indexingBuffers.get(id), equalTo(INACTIVE));
=======
        public void deleteShard(IndexShard shard) {
            indexBufferRAMBytesUsed.remove(shard);
            writingBytes.remove(shard);
        }

        @Override
        protected List<IndexShard> availableShards() {
            return new ArrayList<>(indexBufferRAMBytesUsed.keySet());
        }

        @Override
        protected long getIndexBufferRAMBytesUsed(IndexShard shard) {
            return indexBufferRAMBytesUsed.get(shard) + writingBytes.get(shard);
>>>>>>> tempbranch
        }

        @Override
        protected long getShardWritingBytes(IndexShard shard) {
            Long bytes = writingBytes.get(shard);
            if (bytes == null) {
                return 0;
            } else {
                return bytes;
            }
        }

        @Override
        protected void checkIdle(IndexShard shard, long inactiveTimeNS) {
        }

        @Override
        public void writeIndexingBufferAsync(IndexShard shard) {
            long bytes = indexBufferRAMBytesUsed.put(shard, 0L);
            writingBytes.put(shard, writingBytes.get(shard) + bytes);
            indexBufferRAMBytesUsed.put(shard, 0L);
        }

        @Override
<<<<<<< HEAD
        protected void updateShardBuffers(IndexShard shard, ByteSizeValue shardIndexingBufferSize) {
            indexingBuffers.put(shard, shardIndexingBufferSize);
        }

        @Override
        protected boolean checkIdle(IndexShard shard) {
            final TimeValue inactiveTime = settings.getAsTime(IndexShard.INDEX_SHARD_INACTIVE_TIME_SETTING, TimeValue.timeValueMinutes(5));
            Long ns = lastIndexTimeNanos.get(shard);
            if (ns == null) {
                return true;
            } else if (currentTimeInNanos() - ns >= inactiveTime.nanos()) {
                indexingBuffers.put(shard, INACTIVE);
                activeShards.remove(shard);
                return true;
            } else {
                return false;
=======
        public void activateThrottling(IndexShard shard) {
            assertTrue(throttled.add(shard));
        }

        @Override
        public void deactivateThrottling(IndexShard shard) {
            assertTrue(throttled.remove(shard));
        }

        public void doneWriting(IndexShard shard) {
            writingBytes.put(shard, 0L);
        }

        public void assertBuffer(IndexShard shard, int expectedMB) {
            Long actual = indexBufferRAMBytesUsed.get(shard);
            if (actual == null) {
                actual = 0L;
>>>>>>> tempbranch
            }
            assertEquals(expectedMB * 1024 * 1024, actual.longValue());
        }

        public void assertThrottled(IndexShard shard) {
            assertTrue(throttled.contains(shard));
        }

        public void assertNotThrottled(IndexShard shard) {
            assertFalse(throttled.contains(shard));
        }

        public void assertWriting(IndexShard shard, int expectedMB) {
            Long actual = writingBytes.get(shard);
            if (actual == null) {
                actual = 0L;
            }
            assertEquals(expectedMB * 1024 * 1024, actual.longValue());
        }

        public void simulateIndexing(IndexShard shard) {
<<<<<<< HEAD
            lastIndexTimeNanos.put(shard, currentTimeInNanos());
            if (indexingBuffers.containsKey(shard) == false) {
                // First time we are seeing this shard; start it off with inactive buffers as IndexShard does:
                indexingBuffers.put(shard, IndexingMemoryController.INACTIVE_SHARD_INDEXING_BUFFER);
=======
            Long bytes = indexBufferRAMBytesUsed.get(shard);
            if (bytes == null) {
                bytes = 0L;
                // First time we are seeing this shard:
                writingBytes.put(shard, 0L);
>>>>>>> tempbranch
            }
            // Each doc we index takes up a megabyte!
            bytes += 1024*1024;
            indexBufferRAMBytesUsed.put(shard, bytes);
            forceCheck();
        }
    }

    public void testShardAdditionAndRemoval() {
        createIndex("test", Settings.builder().put(SETTING_NUMBER_OF_SHARDS, 3).put(SETTING_NUMBER_OF_REPLICAS, 0).build());
        IndicesService indicesService = getInstanceFromNode(IndicesService.class);
        IndexService test = indicesService.indexService("test");

        MockController controller = new MockController(Settings.builder()
<<<<<<< HEAD
            .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "10mb").build());
        IndexShard shard0 = test.getShard(0);
        controller.simulateIndexing(shard0);
        controller.assertBuffers(shard0, new ByteSizeValue(10, ByteSizeUnit.MB)); // translog is maxed at 64K
=======
                .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "4mb").build());
        IndexShard shard0 = test.getShard(0);
        controller.simulateIndexing(shard0);
        controller.assertBuffer(shard0, 1);
>>>>>>> tempbranch

        // add another shard
        IndexShard shard1 = test.getShard(1);
        controller.simulateIndexing(shard1);
<<<<<<< HEAD
        controller.assertBuffers(shard0, new ByteSizeValue(5, ByteSizeUnit.MB));
        controller.assertBuffers(shard1, new ByteSizeValue(5, ByteSizeUnit.MB));
=======
        controller.assertBuffer(shard0, 1);
        controller.assertBuffer(shard1, 1);
>>>>>>> tempbranch

        // remove first shard
        controller.deleteShard(shard0);
        controller.forceCheck();
<<<<<<< HEAD
        controller.assertBuffers(shard1, new ByteSizeValue(10, ByteSizeUnit.MB)); // translog is maxed at 64K
=======
        controller.assertBuffer(shard1, 1);
>>>>>>> tempbranch

        // remove second shard
        controller.deleteShard(shard1);
        controller.forceCheck();

        // add a new one
        IndexShard shard2 = test.getShard(2);
        controller.simulateIndexing(shard2);
<<<<<<< HEAD
        controller.assertBuffers(shard2, new ByteSizeValue(10, ByteSizeUnit.MB)); // translog is maxed at 64K
=======
        controller.assertBuffer(shard2, 1);
>>>>>>> tempbranch
    }

    public void testActiveInactive() {

        createIndex("test", Settings.builder().put(SETTING_NUMBER_OF_SHARDS, 2).put(SETTING_NUMBER_OF_REPLICAS, 0).build());
        IndicesService indicesService = getInstanceFromNode(IndicesService.class);
        IndexService test = indicesService.indexService("test");

        MockController controller = new MockController(Settings.builder()
<<<<<<< HEAD
            .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "10mb")
            .put(IndexShard.INDEX_SHARD_INACTIVE_TIME_SETTING, "5s")
            .build());
=======
                .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "5mb")
                .build());
>>>>>>> tempbranch

        IndexShard shard0 = test.getShard(0);
        controller.simulateIndexing(shard0);
        IndexShard shard1 = test.getShard(1);
        controller.simulateIndexing(shard1);
<<<<<<< HEAD
        controller.assertBuffers(shard0, new ByteSizeValue(5, ByteSizeUnit.MB));
        controller.assertBuffers(shard1, new ByteSizeValue(5, ByteSizeUnit.MB));
=======
>>>>>>> tempbranch

        controller.assertBuffer(shard0, 1);
        controller.assertBuffer(shard1, 1);

        controller.simulateIndexing(shard0);
        controller.simulateIndexing(shard1);

        controller.assertBuffer(shard0, 2);
        controller.assertBuffer(shard1, 2);

        // index into one shard only, crosses the 5mb limit, so shard1 is refreshed
        controller.simulateIndexing(shard0);
<<<<<<< HEAD
        controller.assertBuffers(shard0, new ByteSizeValue(10, ByteSizeUnit.MB));
        controller.assertInactive(shard1);

        controller.incrementTimeSec(3); // increment but not enough to become inactive
        controller.forceCheck();
        controller.assertBuffers(shard0, new ByteSizeValue(10, ByteSizeUnit.MB));
        controller.assertInactive(shard1);

        controller.incrementTimeSec(3); // increment some more
        controller.forceCheck();
        controller.assertInactive(shard0);
        controller.assertInactive(shard1);
=======
        controller.simulateIndexing(shard0);
        controller.assertBuffer(shard0, 0);
        controller.assertBuffer(shard1, 2);
>>>>>>> tempbranch

        controller.simulateIndexing(shard1);
<<<<<<< HEAD
        controller.assertInactive(shard0);
        controller.assertBuffers(shard1, new ByteSizeValue(10, ByteSizeUnit.MB));
    }

    public void testMinShardBufferSizes() {
        MockController controller = new MockController(Settings.builder()
            .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "10mb")
            .put(IndexingMemoryController.MIN_SHARD_INDEX_BUFFER_SIZE_SETTING, "6mb")
            .put(IndexingMemoryController.MIN_SHARD_TRANSLOG_BUFFER_SIZE_SETTING, "40kb").build());

        assertTwoActiveShards(controller, new ByteSizeValue(6, ByteSizeUnit.MB), new ByteSizeValue(40, ByteSizeUnit.KB));
    }

    public void testMaxShardBufferSizes() {
        MockController controller = new MockController(Settings.builder()
            .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "10mb")
            .put(IndexingMemoryController.MAX_SHARD_INDEX_BUFFER_SIZE_SETTING, "3mb")
            .put(IndexingMemoryController.MAX_SHARD_TRANSLOG_BUFFER_SIZE_SETTING, "10kb").build());

        assertTwoActiveShards(controller, new ByteSizeValue(3, ByteSizeUnit.MB), new ByteSizeValue(10, ByteSizeUnit.KB));
    }

    public void testRelativeBufferSizes() {
        MockController controller = new MockController(Settings.builder()
            .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "50%")
            .build());

        assertThat(controller.indexingBufferSize(), equalTo(new ByteSizeValue(50, ByteSizeUnit.MB)));
=======
        controller.simulateIndexing(shard1);
        controller.assertBuffer(shard1, 4);
        controller.simulateIndexing(shard1);
        controller.simulateIndexing(shard1);
        // shard1 crossed 5 mb and is now cleared:
        controller.assertBuffer(shard1, 0);
>>>>>>> tempbranch
    }

    public void testMinBufferSizes() {
        MockController controller = new MockController(Settings.builder()
            .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "0.001%")
            .put(IndexingMemoryController.MIN_INDEX_BUFFER_SIZE_SETTING, "6mb").build());

        assertThat(controller.indexingBufferSize(), equalTo(new ByteSizeValue(6, ByteSizeUnit.MB)));
    }

    public void testMaxBufferSizes() {
        MockController controller = new MockController(Settings.builder()
<<<<<<< HEAD
            .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "90%")
            .put(IndexingMemoryController.MAX_INDEX_BUFFER_SIZE_SETTING, "6mb").build());
=======
                .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "90%")
                .put(IndexingMemoryController.MAX_INDEX_BUFFER_SIZE_SETTING, "6mb").build());
>>>>>>> tempbranch

        assertThat(controller.indexingBufferSize(), equalTo(new ByteSizeValue(6, ByteSizeUnit.MB)));
    }

    public void testThrottling() throws Exception {
        createIndex("test", Settings.builder().put(SETTING_NUMBER_OF_SHARDS, 3).put(SETTING_NUMBER_OF_REPLICAS, 0).build());
        IndicesService indicesService = getInstanceFromNode(IndicesService.class);
        IndexService test = indicesService.indexService("test");

        MockController controller = new MockController(Settings.builder()
                .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "4mb").build());
        IndexShard shard0 = test.getShard(0);
        IndexShard shard1 = test.getShard(1);
        IndexShard shard2 = test.getShard(2);
        controller.simulateIndexing(shard0);
        controller.simulateIndexing(shard0);
        controller.simulateIndexing(shard0);
        controller.assertBuffer(shard0, 3);
        controller.simulateIndexing(shard1);
        controller.simulateIndexing(shard1);
<<<<<<< HEAD
        controller.assertBuffers(shard0, indexBufferSize);
        controller.assertBuffers(shard1, indexBufferSize);
=======

        // We are now using 5 MB, so we should be writing shard0 since it's using the most heap:
        controller.assertWriting(shard0, 3);
        controller.assertWriting(shard1, 0);
        controller.assertBuffer(shard0, 0);
        controller.assertBuffer(shard1, 2);

        controller.simulateIndexing(shard0);
        controller.simulateIndexing(shard1);
        controller.simulateIndexing(shard1);

        // Now we are still writing 3 MB (shard0), and using 5 MB index buffers, so we should now 1) be writing shard1, and 2) be throttling shard1:
        controller.assertWriting(shard0, 3);
        controller.assertWriting(shard1, 4);
        controller.assertBuffer(shard0, 1);
        controller.assertBuffer(shard1, 0);

        controller.assertNotThrottled(shard0);
        controller.assertThrottled(shard1);

        System.out.println("TEST: now index more");

        // More indexing to shard0
        controller.simulateIndexing(shard0);
        controller.simulateIndexing(shard0);
        controller.simulateIndexing(shard0);
        controller.simulateIndexing(shard0);

        // Now we are using 5 MB again, so shard0 should also be writing and now also be throttled:
        controller.assertWriting(shard0, 8);
        controller.assertWriting(shard1, 4);
        controller.assertBuffer(shard0, 0);
        controller.assertBuffer(shard1, 0);

        controller.assertThrottled(shard0);
        controller.assertThrottled(shard1);

        // Both shards finally finish writing, and throttling should stop:
        controller.doneWriting(shard0);
        controller.doneWriting(shard1);
        controller.forceCheck();
        controller.assertNotThrottled(shard0);
        controller.assertNotThrottled(shard1);
    }

    // #10312
    public void testDeletesAloneCanTriggerRefresh() throws Exception {
        createIndex("index",
                    Settings.builder().put(SETTING_NUMBER_OF_SHARDS, 1)
                                      .put(SETTING_NUMBER_OF_REPLICAS, 0)
                                      .put("index.refresh_interval", -1)
                                      .build());
        ensureGreen();

        IndicesService indicesService = getInstanceFromNode(IndicesService.class);
        IndexService indexService = indicesService.indexService("index");
        IndexShard shard = indexService.getShardOrNull(0);
        assertNotNull(shard);

        for (int i = 0; i < 100; i++) {
            String id = Integer.toString(i);
            client().prepareIndex("index", "type", id).setSource("field", "value").get();
        }

        // Force merge so we know all merges are done before we start deleting:
        ForceMergeResponse r = client().admin().indices().prepareForceMerge().setMaxNumSegments(1).execute().actionGet();
        assertNoFailures(r);

        // Make a shell of an IMC to check up on indexing buffer usage:
        Settings settings = Settings.builder().put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "1kb").build();

        // TODO: would be cleaner if I could pass this 1kb setting to the single node this test created....
        IndexingMemoryController imc = new IndexingMemoryController(settings, null, null) {
            @Override
            protected List<IndexShard> availableShards() {
                return Collections.singletonList(shard);
            }

            @Override
            protected long getIndexBufferRAMBytesUsed(IndexShard shard) {
                return shard.getIndexBufferRAMBytesUsed();
            }   

            @Override
            protected void writeIndexingBufferAsync(IndexShard shard) {
                // just do it sync'd for this test
                shard.writeIndexingBuffer();
            }

        };

        for (int i = 0; i < 100; i++) {
            String id = Integer.toString(i);
            client().prepareDelete("index", "type", id).get();
        }

        final long indexingBufferBytes1 = shard.getIndexBufferRAMBytesUsed();

        imc.forceCheck();

        // We must assertBusy because the writeIndexingBufferAsync is done in background (REFRESH) thread pool:
        assertBusy(new Runnable() {
            @Override
            public void run() {
                try (Engine.Searcher s2 = shard.acquireSearcher("index")) {
                    // 100 buffered deletes will easily exceed our 1 KB indexing buffer so it should trigger a write:
                    final long indexingBufferBytes2 = shard.getIndexBufferRAMBytesUsed();
                    assertTrue(indexingBufferBytes2 < indexingBufferBytes1);
                }
            }
        });
>>>>>>> tempbranch
    }
}