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

package org.elasticsearch.action.delete;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
<<<<<<< HEAD
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.index.shard.ShardId;
=======
import org.elasticsearch.common.xcontent.StatusToXContent;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
>>>>>>> tempbranch
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

import static org.elasticsearch.rest.RestStatus.NOT_FOUND;

/**
 * The response of the delete action.
 *
 * @see org.elasticsearch.action.delete.DeleteRequest
 * @see org.elasticsearch.client.Client#delete(DeleteRequest)
 */
<<<<<<< HEAD
public class DeleteResponse extends DocWriteResponse {
=======
public class DeleteResponse extends ActionWriteResponse implements StatusToXContent {
>>>>>>> tempbranch

    private boolean found;

    public DeleteResponse() {

    }

    public DeleteResponse(ShardId shardId, String type, String id, long version, boolean found) {
        super(shardId, type, id, version);
        this.found = found;
    }


    /**
     * Returns <tt>true</tt> if a doc was found to delete.
     */
    public boolean isFound() {
        return found;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        found = in.readBoolean();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeBoolean(found);
    }

    @Override
    public RestStatus status() {
<<<<<<< HEAD
        if (found == false) {
            return RestStatus.NOT_FOUND;
        }
        return super.status();
    }

    static final class Fields {
        static final XContentBuilderString FOUND = new XContentBuilderString("found");
=======
        RestStatus status = getShardInfo().status();
        if (isFound() == false) {
            status = NOT_FOUND;
        }
        return status;
>>>>>>> tempbranch
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
<<<<<<< HEAD
        builder.field(Fields.FOUND, isFound());
        super.toXContent(builder, params);
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DeleteResponse[");
        builder.append("index=").append(getIndex());
        builder.append(",type=").append(getType());
        builder.append(",id=").append(getId());
        builder.append(",version=").append(getVersion());
        builder.append(",found=").append(found);
        builder.append(",shards=").append(getShardInfo());
        return builder.append("]").toString();
=======
        ActionWriteResponse.ShardInfo shardInfo = getShardInfo();
        builder.field(Fields.FOUND, found)
            .field(Fields._INDEX, index)
            .field(Fields._TYPE, type)
            .field(Fields._ID, id)
            .field(Fields._VERSION, version)
            .value(shardInfo);
        return builder;
    }

    static final class Fields {
        static final XContentBuilderString FOUND = new XContentBuilderString("found");
        static final XContentBuilderString _INDEX = new XContentBuilderString("_index");
        static final XContentBuilderString _TYPE = new XContentBuilderString("_type");
        static final XContentBuilderString _ID = new XContentBuilderString("_id");
        static final XContentBuilderString _VERSION = new XContentBuilderString("_version");
>>>>>>> tempbranch
    }
}