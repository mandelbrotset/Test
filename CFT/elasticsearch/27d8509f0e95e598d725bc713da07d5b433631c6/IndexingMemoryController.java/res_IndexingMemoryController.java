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

<<<<<<< HEAD
=======
import java.util.*;
import java.util.concurrent.ScheduledFuture;

import org.elasticsearch.common.Nullable;
>>>>>>> tempbranch
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.FutureUtils;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.engine.EngineClosedException;
import org.elasticsearch.index.engine.FlushNotAllowedEngineException;
import org.elasticsearch.index.shard.IndexEventListener;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.IndexShardState;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.monitor.jvm.JvmInfo;
import org.elasticsearch.threadpool.ThreadPool;

<<<<<<< HEAD
import java.util.*;
import java.util.concurrent.ScheduledFuture;

public class IndexingMemoryController extends AbstractLifecycleComponent<IndexingMemoryController> implements IndexEventListener {
=======
public class IndexingMemoryController extends AbstractLifecycleComponent<IndexingMemoryController> {
>>>>>>> tempbranch

    /** How much heap (% or bytes) we will share across all actively indexing shards on this node (default: 10%). */
    public static final String INDEX_BUFFER_SIZE_SETTING = "indices.memory.index_buffer_size";

    /** Only applies when <code>indices.memory.index_buffer_size</code> is a %, to set a floor on the actual size in bytes (default: 48 MB). */
    public static final String MIN_INDEX_BUFFER_SIZE_SETTING = "indices.memory.min_index_buffer_size";

    /** Only applies when <code>indices.memory.index_buffer_size</code> is a %, to set a ceiling on the actual size in bytes (default: not set). */
    public static final String MAX_INDEX_BUFFER_SIZE_SETTING = "indices.memory.max_index_buffer_size";

<<<<<<< HEAD
    /** Sets a floor on the per-shard index buffer size (default: 4 MB). */
    public static final String MIN_SHARD_INDEX_BUFFER_SIZE_SETTING = "indices.memory.min_shard_index_buffer_size";

    /** Sets a ceiling on the per-shard index buffer size (default: 512 MB). */
    public static final String MAX_SHARD_INDEX_BUFFER_SIZE_SETTING = "indices.memory.max_shard_index_buffer_size";

    /** How much heap (% or bytes) we will share across all actively indexing shards for the translog buffer (default: 1%). */
    public static final String TRANSLOG_BUFFER_SIZE_SETTING = "indices.memory.translog_buffer_size";

    /** Only applies when <code>indices.memory.translog_buffer_size</code> is a %, to set a floor on the actual size in bytes (default: 256 KB). */
    public static final String MIN_TRANSLOG_BUFFER_SIZE_SETTING = "indices.memory.min_translog_buffer_size";

    /** Only applies when <code>indices.memory.translog_buffer_size</code> is a %, to set a ceiling on the actual size in bytes (default: not set). */
    public static final String MAX_TRANSLOG_BUFFER_SIZE_SETTING = "indices.memory.max_translog_buffer_size";

    /** Sets a floor on the per-shard translog buffer size (default: 2 KB). */
    public static final String MIN_SHARD_TRANSLOG_BUFFER_SIZE_SETTING = "indices.memory.min_shard_translog_buffer_size";

    /** Sets a ceiling on the per-shard translog buffer size (default: 64 KB). */
    public static final String MAX_SHARD_TRANSLOG_BUFFER_SIZE_SETTING = "indices.memory.max_shard_translog_buffer_size";

    /** How frequently we check shards to find inactive ones (default: 30 seconds). */
    public static final String SHARD_INACTIVE_INTERVAL_TIME_SETTING = "indices.memory.interval";
=======
    /** If we see no indexing operations after this much time for a given shard, we consider that shard inactive (default: 5 minutes). */
    public static final String SHARD_INACTIVE_TIME_SETTING = "indices.memory.shard_inactive_time";

    /** How frequently we check indexing memory usage (default: 5 seconds). */
    public static final String SHARD_MEMORY_INTERVAL_TIME_SETTING = "indices.memory.interval";
>>>>>>> tempbranch

    /** Hardwired translog buffer size */
    public static final ByteSizeValue SHARD_TRANSLOG_BUFFER = ByteSizeValue.parseBytesSizeValue("8kb", "SHARD_TRANSLOG_BUFFER");

    private final ThreadPool threadPool;
    private final IndicesService indicesService;

    private final ByteSizeValue indexingBuffer;

    private final TimeValue interval;

    private volatile ScheduledFuture scheduler;

    private static final EnumSet<IndexShardState> CAN_UPDATE_INDEX_BUFFER_STATES = EnumSet.of(
            IndexShardState.RECOVERING, IndexShardState.POST_RECOVERY, IndexShardState.STARTED, IndexShardState.RELOCATED);

    private final ShardsIndicesStatusChecker statusChecker;

    /** How many bytes we are currently moving to disk by the engine to refresh */
    private final AtomicLong bytesRefreshingNow = new AtomicLong();

    private final Map<ShardId,Long> refreshingBytes = new ConcurrentHashMap<>();

    @Inject
    public IndexingMemoryController(Settings settings, ThreadPool threadPool, IndicesService indicesService) {
        this(settings, threadPool, indicesService, JvmInfo.jvmInfo().getMem().getHeapMax().bytes());
    }

    // for testing
    protected IndexingMemoryController(Settings settings, ThreadPool threadPool, IndicesService indicesService, long jvmMemoryInBytes) {
        super(settings);
        this.threadPool = threadPool;
        this.indicesService = indicesService;

        ByteSizeValue indexingBuffer;
        String indexingBufferSetting = this.settings.get(INDEX_BUFFER_SIZE_SETTING, "10%");
        if (indexingBufferSetting.endsWith("%")) {
            double percent = Double.parseDouble(indexingBufferSetting.substring(0, indexingBufferSetting.length() - 1));
            indexingBuffer = new ByteSizeValue((long) (((double) jvmMemoryInBytes) * (percent / 100)));
            ByteSizeValue minIndexingBuffer = this.settings.getAsBytesSize(MIN_INDEX_BUFFER_SIZE_SETTING, new ByteSizeValue(48, ByteSizeUnit.MB));
            ByteSizeValue maxIndexingBuffer = this.settings.getAsBytesSize(MAX_INDEX_BUFFER_SIZE_SETTING, null);

            if (indexingBuffer.bytes() < minIndexingBuffer.bytes()) {
                indexingBuffer = minIndexingBuffer;
            }
            if (maxIndexingBuffer != null && indexingBuffer.bytes() > maxIndexingBuffer.bytes()) {
                indexingBuffer = maxIndexingBuffer;
            }
        } else {
            indexingBuffer = ByteSizeValue.parseBytesSizeValue(indexingBufferSetting, INDEX_BUFFER_SIZE_SETTING);
        }
        this.indexingBuffer = indexingBuffer;

<<<<<<< HEAD
        // we need to have this relatively small to move a shard from inactive to active fast (enough)
        this.interval = this.settings.getAsTime(SHARD_INACTIVE_INTERVAL_TIME_SETTING, TimeValue.timeValueSeconds(30));

        this.statusChecker = new ShardsIndicesStatusChecker();

        logger.debug("using indexing buffer size [{}], with {} [{}], {} [{}], {} [{}]",
                this.indexingBuffer,
                MIN_SHARD_INDEX_BUFFER_SIZE_SETTING, this.minShardIndexBufferSize,
                MAX_SHARD_INDEX_BUFFER_SIZE_SETTING, this.maxShardIndexBufferSize,
                SHARD_INACTIVE_INTERVAL_TIME_SETTING, this.interval);
=======
        this.inactiveTime = this.settings.getAsTime(SHARD_INACTIVE_TIME_SETTING, TimeValue.timeValueMinutes(5));
        // we need to have this relatively small to free up heap quickly enough
        this.interval = this.settings.getAsTime(SHARD_MEMORY_INTERVAL_TIME_SETTING, TimeValue.timeValueSeconds(5));

        this.statusChecker = new ShardsIndicesStatusChecker();

        logger.debug("using indexing buffer size [{}] with {} [{}], {} [{}]",
                     this.indexingBuffer,
                     SHARD_INACTIVE_TIME_SETTING, this.inactiveTime,
                     SHARD_MEMORY_INTERVAL_TIME_SETTING, this.interval);
    }

    public void addRefreshingBytes(ShardId shardId, long numBytes) {
        refreshingBytes.put(shardId, numBytes);
    }

    public void removeRefreshingBytes(ShardId shardId, long numBytes) {
        boolean result = refreshingBytes.remove(shardId);
        assert result;
>>>>>>> tempbranch
    }

    @Override
    protected void doStart() {
        // it's fine to run it on the scheduler thread, no busy work
        this.scheduler = threadPool.scheduleWithFixedDelay(statusChecker, interval);
    }

    @Override
    protected void doStop() {
        FutureUtils.cancel(scheduler);
        scheduler = null;
    }

    @Override
    protected void doClose() {
    }

    /**
     * returns the current budget for the total amount of indexing buffers of
     * active shards on this node
     */
    public ByteSizeValue indexingBufferSize() {
        return indexingBuffer;
    }

<<<<<<< HEAD
    /**
     * returns the current budget for the total amount of translog buffers of
     * active shards on this node
     */
    public ByteSizeValue translogBufferSize() {
        return translogBuffer;
    }

    protected List<IndexShard> availableShards() {
        List<IndexShard> availableShards = new ArrayList<>();
=======
    protected List<ShardId> availableShards() {
        ArrayList<ShardId> list = new ArrayList<>();
>>>>>>> tempbranch

        for (IndexService indexService : indicesService) {
            for (IndexShard shard : indexService) {
                if (shardAvailable(shard)) {
                    availableShards.add(shard);
                }
            }
        }
        return availableShards;
    }

    /** returns how much heap this shard is using for its indexing buffer */
    protected long getIndexBufferRAMBytesUsed(ShardId shardId) {
        IndexShard shard = getShard(shardId);
        if (shard == null) {
            return 0;
        }

        return shard.getIndexBufferRAMBytesUsed();
    }

    /** ask this shard to refresh, in the background, to free up heap */
    protected void refreshShardAsync(ShardId shardId) {
        IndexShard shard = getShard(shardId);
        if (shard != null) {
            shard.refreshAsync("memory");
        }
    }

    /** returns true if shard exists and is availabe for updates */
    protected boolean shardAvailable(IndexShard shard) {
        // shadow replica doesn't have an indexing buffer
<<<<<<< HEAD
        return shard.canIndex() && CAN_UPDATE_INDEX_BUFFER_STATES.contains(shard.state());
    }

    /** set new indexing and translog buffers on this shard.  this may cause the shard to refresh to free up heap. */
    protected void updateShardBuffers(IndexShard shard, ByteSizeValue shardIndexingBufferSize, ByteSizeValue shardTranslogBufferSize) {
        try {
            shard.updateBufferSize(shardIndexingBufferSize, shardTranslogBufferSize);
        } catch (EngineClosedException | FlushNotAllowedEngineException e) {
            // ignore
        } catch (Exception e) {
            logger.warn("failed to set shard {} index buffer to [{}]", e, shard.shardId(), shardIndexingBufferSize);
        }
    }

=======
        return shard != null && shard.canIndex() && CAN_UPDATE_INDEX_BUFFER_STATES.contains(shard.state());
    }

    /** ask this shard to check now whether it is inactive, and reduces its indexing and translog buffers if so.  returns Boolean.TRUE if
     *  it did deactive, Boolean.FALSE if it did not, and null if the shard is unknown */
    protected void checkIdle(ShardId shardId, long inactiveTimeNS) {
        final IndexShard shard = getShard(shardId);
        if (shard != null) {
            shard.checkIdle(inactiveTimeNS);
        }
    }

    /** gets an {@link IndexShard} instance for the given shard. returns null if the shard doesn't exist */
    protected IndexShard getShard(ShardId shardId) {
        IndexService indexService = indicesService.indexService(shardId.index().name());
        if (indexService != null) {
            IndexShard indexShard = indexService.getShardOrNull(shardId.id());
            return indexShard;
        }
        return null;
    }

>>>>>>> tempbranch
    /** check if any shards active status changed, now. */
    public void forceCheck() {
        statusChecker.run();
    }

<<<<<<< HEAD
    class ShardsIndicesStatusChecker implements Runnable {
        @Override
        public synchronized void run() {
            List<IndexShard> availableShards = availableShards();
            List<IndexShard> activeShards = new ArrayList<>();
            for (IndexShard shard : availableShards) {
                if (!checkIdle(shard)) {
                    activeShards.add(shard);
                }
            }
            int activeShardCount = activeShards.size();

            // TODO: we could be smarter here by taking into account how RAM the IndexWriter on each shard
            // is actually using (using IW.ramBytesUsed), so that small indices (e.g. Marvel) would not
            // get the same indexing buffer as large indices.  But it quickly gets tricky...
            if (activeShardCount == 0) {
                return;
            }
=======
    long startMS = System.currentTimeMillis();

    /** called by IndexShard to record that this many bytes were written to translog */
    public void bytesWritten(int bytes) {
        statusChecker.bytesWritten(bytes);
    }

    static final class ShardAndBytesUsed implements Comparable<ShardAndBytesUsed> {
        final long bytesUsed;
        final ShardId shardId;

        public ShardAndBytesUsed(long bytesUsed, ShardId shardId) {
            this.bytesUsed = bytesUsed;
            this.shardId = shardId;
        }

        @Override
        public int compareTo(ShardAndBytesUsed other) {
            // Sort larger shards first:
            return Long.compare(other.bytesUsed, bytesUsed);
        }
    }

    class ShardsIndicesStatusChecker implements Runnable {

        long bytesWrittenSinceCheck;

        public synchronized void bytesWritten(int bytes) {
            bytesWrittenSinceCheck += bytes;
            if (bytesWrittenSinceCheck > indexingBuffer.bytes()/20) {
                // NOTE: this is only an approximate check, because bytes written is to the translog, vs indexing memory buffer which is
                // typically smaller.  But this logic is here only as a safety against thread starvation or too infrequent checking,
                // to ensure we are still checking in proportion to bytes processed by indexing:
                System.out.println(((System.currentTimeMillis() - startMS)/1000.0) + ": NOW CHECK xlog=" + bytesWrittenSinceCheck);
                run();
            }
        }

        @Override
        public synchronized void run() {

            // nocommit add defensive try/catch-everything here?  bad if an errant EngineClosedExc kills off this thread!!

            // Fast check to sum up how much heap all shards' indexing buffers are using now:
            long totalBytesUsed = 0;
            for (ShardId shardId : availableShards()) {
                Long refreshingBytes = refreshingBytes.get(shardId);

                // Give shard a chance to transition to inactive so sync'd flush can happen:
                checkIdle(shardId, inactiveTime.nanos());

                // nocommit explain why order is important here!
                Long refreshingBytes = refreshingBytes.get(shardId);

                long shardBytesUsed = getIndexBufferRAMBytesUsed(shardId);

                if (refreshingBytes != null) {
                    // Only count up bytes not already being refreshed:
                    shardBytesUsed -= refreshingBytes;

                    // If the refresh completed just after we pulled refreshingBytes and before we pulled index buffer bytes, then we could
                    // have a negative value here:
                    if (shardBytesUsed < 0) {
                        continue;
                    }
                }

                totalBytesUsed += shardBytesUsed;
                System.out.println("IMC:   " + shardId + " using " + (shardBytesUsed/1024./1024.) + " MB");
            }

            System.out.println(((System.currentTimeMillis() - startMS)/1000.0) + ": TOT=" + totalBytesUsed + " vs " + indexingBuffer.bytes());

            if (totalBytesUsed - bytesRefreshingNow.get() > indexingBuffer.bytes()) {
                // OK we are using too much; make a queue and ask largest shard(s) to refresh:
                logger.debug("now refreshing some shards: total indexing bytes used [{}] vs index_buffer_size [{}]", new ByteSizeValue(totalBytesUsed), indexingBuffer);
                PriorityQueue<ShardAndBytesUsed> queue = new PriorityQueue<>();
                for (ShardId shardId : availableShards()) {
                    // nocommit explain why order is important here!
                    Long refreshingBytes = refreshingBytes.get(shardId);

                    long shardBytesUsed = getIndexBufferRAMBytesUsed(shardId);
>>>>>>> tempbranch

                    if (refreshingBytes != null) {
                        // Only count up bytes not already being refreshed:
                        shardBytesUsed -= refreshingBytes;

                        // If the refresh completed just after we pulled refreshingBytes and before we pulled index buffer bytes, then we could
                        // have a negative value here:
                        if (shardBytesUsed < 0) {
                            continue;
                        }
                    }

<<<<<<< HEAD
            logger.debug("recalculating shard indexing buffer, total is [{}] with [{}] active shards, each shard set to indexing=[{}], translog=[{}]", indexingBuffer, activeShardCount, shardIndexingBufferSize, shardTranslogBufferSize);

            for (IndexShard shard : activeShards) {
                updateShardBuffers(shard, shardIndexingBufferSize, shardTranslogBufferSize);
=======
                    if (shardBytesUsed > 0) {
                        queue.add(new ShardAndBytesUsed(shardBytesUsed, shardId));
                    }
                }

                while (totalBytesUsed > indexingBuffer.bytes() && queue.isEmpty() == false) {
                    ShardAndBytesUsed largest = queue.poll();
                    System.out.println("IMC: write " + largest.shardId + ": " + (largest.bytesUsed/1024./1024.) + " MB");
                    logger.debug("refresh shard [{}] to free up its [{}] indexing buffer", largest.shardId, new ByteSizeValue(largest.bytesUsed));
                    refreshShardAsync(largest.shardId);
                    totalBytesUsed -= largest.bytesUsed;
                }
>>>>>>> tempbranch
            }

<<<<<<< HEAD
    protected long currentTimeInNanos() {
        return System.nanoTime();
    }

    /**
     * ask this shard to check now whether it is inactive, and reduces its indexing and translog buffers if so.
     * return false if the shard is not idle, otherwise true
     */
    protected boolean checkIdle(IndexShard shard) {
        try {
            return shard.checkIdle();
        } catch (EngineClosedException | FlushNotAllowedEngineException e) {
            logger.trace("ignore [{}] while marking shard {} as inactive", e.getClass().getSimpleName(), shard.shardId());
            return true;
        }
    }

    @Override
    public void onShardActive(IndexShard indexShard) {
        // At least one shard used to be inactive ie. a new write operation just showed up.
        // We try to fix the shards indexing buffer immediately. We could do this async instead, but cost should
        // be low, and it's rare this happens.
        forceCheck();
=======
            bytesWrittenSinceCheck = 0;
        }
>>>>>>> tempbranch
    }
}