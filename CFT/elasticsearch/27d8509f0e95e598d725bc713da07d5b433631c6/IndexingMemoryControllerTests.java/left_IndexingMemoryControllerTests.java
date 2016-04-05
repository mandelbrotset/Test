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

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.test.ESSingleNodeTestCase;

import java.util.*;

import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_REPLICAS;
import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_SHARDS;
import static org.hamcrest.Matchers.equalTo;

public class IndexingMemoryControllerTests extends ESSingleNodeTestCase {

    static class MockController extends IndexingMemoryController {

<<<<<<< HEAD
        final static ByteSizeValue INACTIVE = new ByteSizeValue(-1);

        final Map<IndexShard, ByteSizeValue> indexingBuffers = new HashMap<>();
        final Map<IndexShard, ByteSizeValue> translogBuffers = new HashMap<>();

        final Map<IndexShard, Long> lastIndexTimeNanos = new HashMap<>();
        final Set<IndexShard> activeShards = new HashSet<>();

        long currentTimeSec = TimeValue.timeValueNanos(System.nanoTime()).seconds();

        public MockController(Settings settings) {
            super(Settings.builder()
                    .put(SHARD_INACTIVE_INTERVAL_TIME_SETTING, "200h") // disable it
                    .put(IndexShard.INDEX_SHARD_INACTIVE_TIME_SETTING, "1ms") // nearly immediate
                    .put(settings)
                    .build(),
                null, null, 100 * 1024 * 1024); // fix jvm mem size to 100mb
        }

        public void deleteShard(IndexShard id) {
            indexingBuffers.remove(id);
            translogBuffers.remove(id);
        }

        public void assertBuffers(IndexShard id, ByteSizeValue indexing, ByteSizeValue translog) {
            assertThat(indexingBuffers.get(id), equalTo(indexing));
            assertThat(translogBuffers.get(id), equalTo(translog));
        }

        public void assertInactive(IndexShard id) {
            assertThat(indexingBuffers.get(id), equalTo(INACTIVE));
            assertThat(translogBuffers.get(id), equalTo(INACTIVE));
        }

        @Override
        protected long currentTimeInNanos() {
            return TimeValue.timeValueSeconds(currentTimeSec).nanos();
        }

        @Override
        protected List<IndexShard> availableShards() {
            return new ArrayList<>(indexingBuffers.keySet());
        }

        @Override
        protected boolean shardAvailable(IndexShard shard) {
            return indexingBuffers.containsKey(shard);
        }

        @Override
        protected void updateShardBuffers(IndexShard shard, ByteSizeValue shardIndexingBufferSize, ByteSizeValue shardTranslogBufferSize) {
            indexingBuffers.put(shard, shardIndexingBufferSize);
            translogBuffers.put(shard, shardTranslogBufferSize);
        }

        @Override
        protected boolean checkIdle(IndexShard shard) {
            final TimeValue inactiveTime = settings.getAsTime(IndexShard.INDEX_SHARD_INACTIVE_TIME_SETTING, TimeValue.timeValueMinutes(5));
            Long ns = lastIndexTimeNanos.get(shard);
            if (ns == null) {
                return true;
            } else if (currentTimeInNanos() - ns >= inactiveTime.nanos()) {
                indexingBuffers.put(shard, INACTIVE);
                translogBuffers.put(shard, INACTIVE);
                activeShards.remove(shard);
                return true;
            } else {
                return false;
            }
=======
        final Map<ShardId, Long> indexBufferRAMBytesUsed = new HashMap<>();

        public MockController(Settings settings) {
            super(Settings.builder()
                            .put(SHARD_MEMORY_INTERVAL_TIME_SETTING, "200h") // disable it
                            .put(settings)
                            .build(),
                    null, null, 100 * 1024 * 1024); // fix jvm mem size to 100mb
        }

        public void deleteShard(ShardId shardId) {
            indexBufferRAMBytesUsed.remove(shardId);
        }

        @Override
        protected List<ShardId> availableShards() {
            return new ArrayList<>(indexBufferRAMBytesUsed.keySet());
        }

        @Override
        protected boolean shardAvailable(ShardId shardId) {
            return indexBufferRAMBytesUsed.containsKey(shardId);
        }

        @Override
        protected long getIndexBufferRAMBytesUsed(ShardId shardId) {
            Long used = indexBufferRAMBytesUsed.get(shardId);
            if (used == null) {
                return 0;
            } else {
                return used;
            }
        }

        @Override
        protected void checkIdle(ShardId shardId, long inactiveTimeNS) {
        }

        @Override
        public void refreshShardAsync(ShardId shardId) {
            indexBufferRAMBytesUsed.put(shardId, 0L);
>>>>>>> tempbranch
        }

        public void assertBuffer(ShardId shardId, ByteSizeValue expected) {
            Long actual = indexBufferRAMBytesUsed.get(shardId);
            assertEquals(expected.bytes(), actual.longValue());
        }

<<<<<<< HEAD
        public void simulateIndexing(IndexShard shard) {
            lastIndexTimeNanos.put(shard, currentTimeInNanos());
            if (indexingBuffers.containsKey(shard) == false) {
                // First time we are seeing this shard; start it off with inactive buffers as IndexShard does:
                indexingBuffers.put(shard, IndexingMemoryController.INACTIVE_SHARD_INDEXING_BUFFER);
                translogBuffers.put(shard, IndexingMemoryController.INACTIVE_SHARD_TRANSLOG_BUFFER);
            }
            activeShards.add(shard);
=======
        public void simulateIndexing(ShardId shardId) {
            Long bytes = indexBufferRAMBytesUsed.get(shardId);
            if (bytes == null) {
                bytes = 0L;
            }
            // Each doc we index takes up a megabyte!
            bytes += 1024*1024;
            indexBufferRAMBytesUsed.put(shardId, bytes);
>>>>>>> tempbranch
            forceCheck();
        }
    }

    public void testShardAdditionAndRemoval() {
        createIndex("test", Settings.builder().put(SETTING_NUMBER_OF_SHARDS, 3).put(SETTING_NUMBER_OF_REPLICAS, 0).build());
        IndicesService indicesService = getInstanceFromNode(IndicesService.class);
        IndexService test = indicesService.indexService("test");

        MockController controller = new MockController(Settings.builder()
<<<<<<< HEAD
            .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "10mb")
            .put(IndexingMemoryController.TRANSLOG_BUFFER_SIZE_SETTING, "100kb").build());
        IndexShard shard0 = test.getShard(0);
        controller.simulateIndexing(shard0);
        controller.assertBuffers(shard0, new ByteSizeValue(10, ByteSizeUnit.MB), new ByteSizeValue(64, ByteSizeUnit.KB)); // translog is maxed at 64K

        // add another shard
        IndexShard shard1 = test.getShard(1);
        controller.simulateIndexing(shard1);
        controller.assertBuffers(shard0, new ByteSizeValue(5, ByteSizeUnit.MB), new ByteSizeValue(50, ByteSizeUnit.KB));
        controller.assertBuffers(shard1, new ByteSizeValue(5, ByteSizeUnit.MB), new ByteSizeValue(50, ByteSizeUnit.KB));
=======
                .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "4mb").build());
        final ShardId shard1 = new ShardId("test", 1);
        controller.simulateIndexing(shard1);
        controller.assertBuffer(shard1, new ByteSizeValue(1, ByteSizeUnit.MB));

        // add another shard
        final ShardId shard2 = new ShardId("test", 2);
        controller.simulateIndexing(shard2);
        controller.assertBuffer(shard1, new ByteSizeValue(1, ByteSizeUnit.MB));
        controller.assertBuffer(shard2, new ByteSizeValue(1, ByteSizeUnit.MB));
>>>>>>> tempbranch

        // remove first shard
        controller.deleteShard(shard0);
        controller.forceCheck();
<<<<<<< HEAD
        controller.assertBuffers(shard1, new ByteSizeValue(10, ByteSizeUnit.MB), new ByteSizeValue(64, ByteSizeUnit.KB)); // translog is maxed at 64K
=======
        controller.assertBuffer(shard2, new ByteSizeValue(1, ByteSizeUnit.MB));
>>>>>>> tempbranch

        // remove second shard
        controller.deleteShard(shard1);
        controller.forceCheck();

        // add a new one
<<<<<<< HEAD
        IndexShard shard2 = test.getShard(2);
        controller.simulateIndexing(shard2);
        controller.assertBuffers(shard2, new ByteSizeValue(10, ByteSizeUnit.MB), new ByteSizeValue(64, ByteSizeUnit.KB)); // translog is maxed at 64K
    }

    public void testActiveInactive() {
        createIndex("test", Settings.builder().put(SETTING_NUMBER_OF_SHARDS, 2).put(SETTING_NUMBER_OF_REPLICAS, 0).build());
        IndicesService indicesService = getInstanceFromNode(IndicesService.class);
        IndexService test = indicesService.indexService("test");
=======
        final ShardId shard3 = new ShardId("test", 3);
        controller.simulateIndexing(shard3);
        controller.assertBuffer(shard3, new ByteSizeValue(1, ByteSizeUnit.MB));
    }

    public void testActiveInactive() {
        MockController controller = new MockController(Settings.builder()
                .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "5mb")
                .build());
>>>>>>> tempbranch

        MockController controller = new MockController(Settings.builder()
            .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "10mb")
            .put(IndexingMemoryController.TRANSLOG_BUFFER_SIZE_SETTING, "100kb")
            .put(IndexShard.INDEX_SHARD_INACTIVE_TIME_SETTING, "5s")
            .build());

        IndexShard shard0 = test.getShard(0);
        controller.simulateIndexing(shard0);
        IndexShard shard1 = test.getShard(1);
        controller.simulateIndexing(shard1);
<<<<<<< HEAD
        controller.assertBuffers(shard0, new ByteSizeValue(5, ByteSizeUnit.MB), new ByteSizeValue(50, ByteSizeUnit.KB));
        controller.assertBuffers(shard1, new ByteSizeValue(5, ByteSizeUnit.MB), new ByteSizeValue(50, ByteSizeUnit.KB));

        // index into both shards, move the clock and see that they are still active
        controller.simulateIndexing(shard0);
=======
        final ShardId shard2 = new ShardId("test", 2);
        controller.simulateIndexing(shard2);
        controller.assertBuffer(shard1, new ByteSizeValue(1, ByteSizeUnit.MB));
        controller.assertBuffer(shard2, new ByteSizeValue(1, ByteSizeUnit.MB));

>>>>>>> tempbranch
        controller.simulateIndexing(shard1);

<<<<<<< HEAD
        controller.incrementTimeSec(10);
        controller.forceCheck();

        // both shards now inactive
        controller.assertInactive(shard0);
        controller.assertInactive(shard1);

        // index into one shard only, see it becomes active
        controller.simulateIndexing(shard0);
        controller.assertBuffers(shard0, new ByteSizeValue(10, ByteSizeUnit.MB), new ByteSizeValue(64, ByteSizeUnit.KB));
        controller.assertInactive(shard1);

        controller.incrementTimeSec(3); // increment but not enough to become inactive
        controller.forceCheck();
        controller.assertBuffers(shard0, new ByteSizeValue(10, ByteSizeUnit.MB), new ByteSizeValue(64, ByteSizeUnit.KB));
        controller.assertInactive(shard1);

        controller.incrementTimeSec(3); // increment some more
        controller.forceCheck();
        controller.assertInactive(shard0);
        controller.assertInactive(shard1);

        // index some and shard becomes immediately active
        controller.simulateIndexing(shard1);
        controller.assertInactive(shard0);
        controller.assertBuffers(shard1, new ByteSizeValue(10, ByteSizeUnit.MB), new ByteSizeValue(64, ByteSizeUnit.KB));
    }

    public void testMinShardBufferSizes() {
        MockController controller = new MockController(Settings.builder()
            .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "10mb")
            .put(IndexingMemoryController.TRANSLOG_BUFFER_SIZE_SETTING, "50kb")
            .put(IndexingMemoryController.MIN_SHARD_INDEX_BUFFER_SIZE_SETTING, "6mb")
            .put(IndexingMemoryController.MIN_SHARD_TRANSLOG_BUFFER_SIZE_SETTING, "40kb").build());

        assertTwoActiveShards(controller, new ByteSizeValue(6, ByteSizeUnit.MB), new ByteSizeValue(40, ByteSizeUnit.KB));
    }

    public void testMaxShardBufferSizes() {
        MockController controller = new MockController(Settings.builder()
            .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "10mb")
            .put(IndexingMemoryController.TRANSLOG_BUFFER_SIZE_SETTING, "50kb")
            .put(IndexingMemoryController.MAX_SHARD_INDEX_BUFFER_SIZE_SETTING, "3mb")
            .put(IndexingMemoryController.MAX_SHARD_TRANSLOG_BUFFER_SIZE_SETTING, "10kb").build());

        assertTwoActiveShards(controller, new ByteSizeValue(3, ByteSizeUnit.MB), new ByteSizeValue(10, ByteSizeUnit.KB));
=======
        controller.assertBuffer(shard1, new ByteSizeValue(2, ByteSizeUnit.MB));
        controller.assertBuffer(shard2, new ByteSizeValue(2, ByteSizeUnit.MB));

        // index into one shard only, crosses the 5mb limit, so shard1 is refreshed
        controller.simulateIndexing(shard1);
        controller.simulateIndexing(shard1);
        controller.assertBuffer(shard1, new ByteSizeValue(0, ByteSizeUnit.MB));
        controller.assertBuffer(shard2, new ByteSizeValue(2, ByteSizeUnit.MB));

        controller.simulateIndexing(shard2);
        controller.simulateIndexing(shard2);
        controller.assertBuffer(shard2, new ByteSizeValue(4, ByteSizeUnit.MB));
        controller.simulateIndexing(shard2);
        controller.simulateIndexing(shard2);
        // shard2 crossed 5 mb and is now cleared:
        controller.assertBuffer(shard2, new ByteSizeValue(0, ByteSizeUnit.MB));
>>>>>>> tempbranch
    }

    public void testRelativeBufferSizes() {
        MockController controller = new MockController(Settings.builder()
<<<<<<< HEAD
            .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "50%")
            .put(IndexingMemoryController.TRANSLOG_BUFFER_SIZE_SETTING, "0.5%")
            .build());
=======
                .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "50%")
                .build());
>>>>>>> tempbranch

        assertThat(controller.indexingBufferSize(), equalTo(new ByteSizeValue(50, ByteSizeUnit.MB)));
    }


    public void testMinBufferSizes() {
        MockController controller = new MockController(Settings.builder()
<<<<<<< HEAD
            .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "0.001%")
            .put(IndexingMemoryController.TRANSLOG_BUFFER_SIZE_SETTING, "0.001%")
            .put(IndexingMemoryController.MIN_INDEX_BUFFER_SIZE_SETTING, "6mb")
            .put(IndexingMemoryController.MIN_TRANSLOG_BUFFER_SIZE_SETTING, "512kb").build());
=======
                .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "0.001%")
                .put(IndexingMemoryController.MIN_INDEX_BUFFER_SIZE_SETTING, "6mb").build());
>>>>>>> tempbranch

        assertThat(controller.indexingBufferSize(), equalTo(new ByteSizeValue(6, ByteSizeUnit.MB)));
    }

    public void testMaxBufferSizes() {
        MockController controller = new MockController(Settings.builder()
<<<<<<< HEAD
            .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "90%")
            .put(IndexingMemoryController.TRANSLOG_BUFFER_SIZE_SETTING, "90%")
            .put(IndexingMemoryController.MAX_INDEX_BUFFER_SIZE_SETTING, "6mb")
            .put(IndexingMemoryController.MAX_TRANSLOG_BUFFER_SIZE_SETTING, "512kb").build());

        assertThat(controller.indexingBufferSize(), equalTo(new ByteSizeValue(6, ByteSizeUnit.MB)));
        assertThat(controller.translogBufferSize(), equalTo(new ByteSizeValue(512, ByteSizeUnit.KB)));
    }

    protected void assertTwoActiveShards(MockController controller, ByteSizeValue indexBufferSize, ByteSizeValue translogBufferSize) {
        createIndex("test", Settings.builder().put(SETTING_NUMBER_OF_SHARDS, 2).put(SETTING_NUMBER_OF_REPLICAS, 0).build());
        IndicesService indicesService = getInstanceFromNode(IndicesService.class);
        IndexService test = indicesService.indexService("test");
        IndexShard shard0 = test.getShard(0);
        controller.simulateIndexing(shard0);
        IndexShard shard1 = test.getShard(1);
        controller.simulateIndexing(shard1);
        controller.assertBuffers(shard0, indexBufferSize, translogBufferSize);
        controller.assertBuffers(shard1, indexBufferSize, translogBufferSize);
=======
                .put(IndexingMemoryController.INDEX_BUFFER_SIZE_SETTING, "90%")
                .put(IndexingMemoryController.MAX_INDEX_BUFFER_SIZE_SETTING, "6mb").build());

        assertThat(controller.indexingBufferSize(), equalTo(new ByteSizeValue(6, ByteSizeUnit.MB)));
>>>>>>> tempbranch
    }
}