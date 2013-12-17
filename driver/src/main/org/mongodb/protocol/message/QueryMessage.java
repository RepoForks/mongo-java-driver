/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.protocol.message;

import org.bson.io.OutputBuffer;
import org.mongodb.Document;
import org.mongodb.Encoder;
import org.mongodb.operation.Find;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class QueryMessage extends BaseQueryMessage {
    private final Find find;
    private final Encoder<Document> encoder;

    public QueryMessage(final String collectionName, final Find find, final Encoder<Document> encoder,
                        final MessageSettings settings) {
        super(collectionName, settings);
        this.find = find;
        this.encoder = encoder;
    }

    @Override
    protected RequestMessage encodeMessageBody(final OutputBuffer buffer, final int messageStartPosition) {
        writeQueryPrologue(find, buffer);
        addDocument(getQueryDocument(), encoder, buffer);
        if (find.getFields() != null) {
            addDocument(find.getFields(), encoder, buffer);
        }
        return null;
    }

    private Document getQueryDocument() {
        Document document = new Document();
        document.put("$query", find.getFilter());
        if (find.getOrder() != null && !find.getOrder().isEmpty()) {
            document.put("$orderby", find.getOrder());
        }
        if (find.isSnapshotMode()) {
            document.put("$snapshot", true);
        }
        if (find.isExplain()) {
            document.put("$explain", true);
        }
        // TODO: only to mongos according to spec
        if (find.getReadPreference() != null) {
            document.put("$readPreference", find.getReadPreference().toDocument());
        }

        if (find.getHint() != null) {
            document.put("$hint", find.getHint().getValue());
        }

        if (find.getOptions().getComment() != null) {
            document.put("$comment", find.getOptions().getComment());
        }

        if (find.getOptions().getMax() != null) {
            document.put("$max", find.getOptions().getMax());
        }

        if (find.getOptions().getMin() != null) {
            document.put("$min", find.getOptions().getMin());
        }

        if (find.getOptions().isReturnKey()) {
            document.put("$returnKey", true);
        }

        if (find.getOptions().isShowDiskLoc()) {
            document.put("$showDiskLoc", true);
        }

        if (find.getOptions().isSnapshot()) {
            document.put("$snapshot", true);
        }

        long maxTime = find.getOptions().getMaxTime(MILLISECONDS);
        if (maxTime != 0) {
            document.put("$maxTimeMS", maxTime);
        }

        int maxScan = find.getOptions().getMaxScan();
        if (maxScan > 0) {
            document.put("$maxScan", maxScan);
        }

        // TODO: special
        return document;
    }

}
