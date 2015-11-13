/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.apache.kylin.engine.mr.steps;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.common.topn.Counter;
import org.apache.kylin.common.topn.TopNCounter;
import org.apache.kylin.common.util.ByteArray;
import org.apache.kylin.common.util.BytesUtil;
import org.apache.kylin.common.util.SplittedBytes;
import org.apache.kylin.cube.CubeInstance;
import org.apache.kylin.cube.CubeManager;
import org.apache.kylin.cube.CubeSegment;
import org.apache.kylin.cube.common.RowKeySplitter;
import org.apache.kylin.cube.cuboid.Cuboid;
import org.apache.kylin.cube.kv.RowConstants;
import org.apache.kylin.cube.kv.RowKeyEncoder;
import org.apache.kylin.cube.kv.RowKeyEncoderProvider;
import org.apache.kylin.cube.model.CubeDesc;
import org.apache.kylin.dict.Dictionary;
import org.apache.kylin.dict.DictionaryManager;
import org.apache.kylin.engine.mr.KylinMapper;
import org.apache.kylin.engine.mr.common.AbstractHadoopJob;
import org.apache.kylin.engine.mr.common.BatchConstants;
import org.apache.kylin.metadata.measure.MeasureCodec;
import org.apache.kylin.metadata.model.ColumnDesc;
import org.apache.kylin.metadata.model.MeasureDesc;
import org.apache.kylin.metadata.model.SegmentStatusEnum;
import org.apache.kylin.metadata.model.TblColRef;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ysong1, honma
 */
public class MergeCuboidMapper extends KylinMapper<Text, Text, Text, Text> {

    private KylinConfig config;
    private String cubeName;
    private String segmentName;
    private CubeManager cubeManager;
    private CubeInstance cube;
    private CubeDesc cubeDesc;
    private CubeSegment mergedCubeSegment;
    private CubeSegment sourceCubeSegment; // Must be unique during a mapper's life cycle

    private Text outputKey = new Text();

    private byte[] newKeyBodyBuf;
    private ByteArray newKeyBuf;
    private RowKeySplitter rowKeySplitter;
    private RowKeyEncoderProvider rowKeyEncoderProvider;

    private HashMap<TblColRef, Boolean> dictsNeedMerging = new HashMap<TblColRef, Boolean>();
    private List<MeasureDesc> measuresDescs;
    private MeasureCodec codec;
    private Object[] measureObjs;
    private Integer[] measureIdxUsingDict;
    private ByteBuffer valueBuf;
    private Text outputValue;

    private Boolean checkNeedMerging(TblColRef col) throws IOException {
        Boolean ret = dictsNeedMerging.get(col);
        if (ret != null)
            return ret;
        else {
            ret = cubeDesc.getRowkey().isUseDictionary(col);
            if (ret) {
                String dictTable = DictionaryManager.getInstance(config).decideSourceData(cubeDesc.getModel(), cubeDesc.getRowkey().getDictionary(col), col).getTable();
                ret = cubeDesc.getFactTable().equalsIgnoreCase(dictTable);
            }
            dictsNeedMerging.put(col, ret);
            return ret;
        }
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.bindCurrentConfiguration(context.getConfiguration());

        cubeName = context.getConfiguration().get(BatchConstants.CFG_CUBE_NAME).toUpperCase();
        segmentName = context.getConfiguration().get(BatchConstants.CFG_CUBE_SEGMENT_NAME).toUpperCase();

        config = AbstractHadoopJob.loadKylinPropsAndMetadata();

        cubeManager = CubeManager.getInstance(config);
        cube = cubeManager.getCube(cubeName);
        cubeDesc = cube.getDescriptor();
        mergedCubeSegment = cube.getSegment(segmentName, SegmentStatusEnum.NEW);

        // int colCount = cubeDesc.getRowkey().getRowKeyColumns().length;
        newKeyBodyBuf = new byte[RowConstants.ROWKEY_BUFFER_SIZE];// size will auto-grow
        newKeyBuf = ByteArray.allocate(RowConstants.ROWKEY_BUFFER_SIZE);

        // decide which source segment
        FileSplit fileSplit = (FileSplit) context.getInputSplit();
        sourceCubeSegment = findSourceSegment(fileSplit, cube);

        rowKeySplitter = new RowKeySplitter(sourceCubeSegment, 65, 255);
        rowKeyEncoderProvider = new RowKeyEncoderProvider(mergedCubeSegment);

        if (cubeDesc.hasMeasureUsingDictionary()) {
            measuresDescs = cubeDesc.getMeasures();
            codec = new MeasureCodec(measuresDescs);
            measureObjs = new Object[measuresDescs.size()];
            List<Integer> measuresUsingDict = Lists.newArrayList();
            for (int i = 0; i < measuresDescs.size(); i++) {
                if (measuresDescs.get(i).getFunction().isTopN()) {
                    // so far only TopN uses dic
                    measuresUsingDict.add(i);
                }
            }
            measureIdxUsingDict = measuresUsingDict.toArray(new Integer[measuresUsingDict.size()]);
            valueBuf = ByteBuffer.allocate(RowConstants.ROWVALUE_BUFFER_SIZE);
            outputValue = new Text();
        }
    }

    private static final Pattern JOB_NAME_PATTERN = Pattern.compile("kylin-([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})");

    public static CubeSegment findSourceSegment(FileSplit fileSplit, CubeInstance cube) {
        String filePath = fileSplit.getPath().toString();
        String jobID = extractJobIDFromPath(filePath);
        return findSegmentWithUuid(jobID, cube);
    }

    private static String extractJobIDFromPath(String path) {
        Matcher matcher = JOB_NAME_PATTERN.matcher(path);
        // check the first occurrence
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalStateException("Can not extract job ID from file path : " + path);
        }
    }

    private static CubeSegment findSegmentWithUuid(String jobID, CubeInstance cubeInstance) {
        for (CubeSegment segment : cubeInstance.getSegments()) {
            String lastBuildJobID = segment.getLastBuildJobID();
            if (lastBuildJobID != null && lastBuildJobID.equalsIgnoreCase(jobID)) {
                return segment;
            }
        }
        throw new IllegalStateException("No merging segment's last build job ID equals " + jobID);
    }

    @Override
    public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        long cuboidID = rowKeySplitter.split(key.getBytes());
        Cuboid cuboid = Cuboid.findById(cubeDesc, cuboidID);
        RowKeyEncoder rowkeyEncoder = rowKeyEncoderProvider.getRowkeyEncoder(cuboid);

        SplittedBytes[] splittedByteses = rowKeySplitter.getSplitBuffers();
        int bufOffset = 0;
        int bodySplitOffset = rowKeySplitter.getBodySplitOffset();

        for (int i = 0; i < cuboid.getColumns().size(); ++i) {
            int useSplit = i + bodySplitOffset;
            TblColRef col = cuboid.getColumns().get(i);

            if (this.checkNeedMerging(col)) {
                // if dictionary on fact table column, needs rewrite
                DictionaryManager dictMgr = DictionaryManager.getInstance(config);
                Dictionary<?> sourceDict = dictMgr.getDictionary(sourceCubeSegment.getDictResPath(col));
                Dictionary<?> mergedDict = dictMgr.getDictionary(mergedCubeSegment.getDictResPath(col));

                while (sourceDict.getSizeOfValue() > newKeyBodyBuf.length - bufOffset || //
                        mergedDict.getSizeOfValue() > newKeyBodyBuf.length - bufOffset || //
                        mergedDict.getSizeOfId() > newKeyBodyBuf.length - bufOffset) {
                    byte[] oldBuf = newKeyBodyBuf;
                    newKeyBodyBuf = new byte[2 * newKeyBodyBuf.length];
                    System.arraycopy(oldBuf, 0, newKeyBodyBuf, 0, oldBuf.length);
                }

                int idInSourceDict = BytesUtil.readUnsigned(splittedByteses[useSplit].value, 0, splittedByteses[useSplit].length);
                int idInMergedDict;

                int size = sourceDict.getValueBytesFromId(idInSourceDict, newKeyBodyBuf, bufOffset);
                if (size < 0) {
                    idInMergedDict = mergedDict.nullId();
                } else {
                    idInMergedDict = mergedDict.getIdFromValueBytes(newKeyBodyBuf, bufOffset, size);
                }

                BytesUtil.writeUnsigned(idInMergedDict, newKeyBodyBuf, bufOffset, mergedDict.getSizeOfId());
                bufOffset += mergedDict.getSizeOfId();
            } else {
                // keep as it is
                while (splittedByteses[useSplit].length > newKeyBodyBuf.length - bufOffset) {
                    byte[] oldBuf = newKeyBodyBuf;
                    newKeyBodyBuf = new byte[2 * newKeyBodyBuf.length];
                    System.arraycopy(oldBuf, 0, newKeyBodyBuf, 0, oldBuf.length);
                }

                System.arraycopy(splittedByteses[useSplit].value, 0, newKeyBodyBuf, bufOffset, splittedByteses[useSplit].length);
                bufOffset += splittedByteses[useSplit].length;
            }
        }

        int fullKeySize = rowkeyEncoder.getBytesLength();
        while (newKeyBuf.array().length < fullKeySize) {
            newKeyBuf.set(new byte[newKeyBuf.length() * 2]);
        }
        newKeyBuf.set(0, fullKeySize);

        rowkeyEncoder.encode(new ByteArray(newKeyBodyBuf, 0, bufOffset), newKeyBuf);
        outputKey.set(newKeyBuf.array(), 0, fullKeySize);

        // encode measure if it uses dictionary 
        if (cubeDesc.hasMeasureUsingDictionary()) {
            codec.decode(ByteBuffer.wrap(value.getBytes(), 0, value.getLength()), measureObjs);
            reEncodeMeasure(measureObjs);
            valueBuf.clear();
            codec.encode(measureObjs, valueBuf);
            outputValue.set(valueBuf.array(), 0, valueBuf.position());
            value = outputValue;
        } 
            
        context.write(outputKey, value);
    }

    private void reEncodeMeasure(Object[] measureObjs) throws IOException, InterruptedException {
        int bufOffset = 0;
        for (int idx : measureIdxUsingDict) {
            // only TopN measure uses dic
            TopNCounter<ByteArray> topNCounters = (TopNCounter<ByteArray>) measureObjs[idx];

            MeasureDesc measureDesc = measuresDescs.get(idx);
            String displayCol = measureDesc.getFunction().getParameter().getDisplayColumn().toUpperCase();
            if (StringUtils.isNotEmpty(displayCol)) {
                ColumnDesc sourceColumn = cubeDesc.getFactTableDesc().findColumnByName(displayCol);
                TblColRef colRef = new TblColRef(sourceColumn);
                DictionaryManager dictMgr = DictionaryManager.getInstance(config);
                Dictionary<?> sourceDict = dictMgr.getDictionary(sourceCubeSegment.getDictResPath(colRef));
                Dictionary<?> mergedDict = dictMgr.getDictionary(mergedCubeSegment.getDictResPath(colRef));

                int topNSize = topNCounters.size();
                while (sourceDict.getSizeOfValue() * topNSize > newKeyBodyBuf.length - bufOffset || //
                        mergedDict.getSizeOfValue() * topNSize > newKeyBodyBuf.length - bufOffset || //
                        mergedDict.getSizeOfId() * topNSize > newKeyBodyBuf.length - bufOffset) {
                    byte[] oldBuf = newKeyBodyBuf;
                    newKeyBodyBuf = new byte[2 * newKeyBodyBuf.length];
                    System.arraycopy(oldBuf, 0, newKeyBodyBuf, 0, oldBuf.length);
                }

                for (Counter<ByteArray> c : topNCounters) {
                    int idInSourceDict = BytesUtil.readUnsigned(c.getItem().array(), c.getItem().offset(), c.getItem().length());
                    int idInMergedDict;
                    int size = sourceDict.getValueBytesFromId(idInSourceDict, newKeyBodyBuf, bufOffset);
                    if (size < 0) {
                        idInMergedDict = mergedDict.nullId();
                    } else {
                        idInMergedDict = mergedDict.getIdFromValueBytes(newKeyBodyBuf, bufOffset, size);
                    }

                    BytesUtil.writeUnsigned(idInMergedDict, newKeyBodyBuf, bufOffset, mergedDict.getSizeOfId());
                    c.getItem().set(newKeyBodyBuf, bufOffset, mergedDict.getSizeOfId());
                    bufOffset += mergedDict.getSizeOfId();
                }
            }
        }

    }
    
}
