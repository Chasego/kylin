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

package org.apache.kylin.job.upgrade.v2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.common.persistence.ResourceStore;
import org.apache.kylin.cube.CubeDescManager;
import org.apache.kylin.cube.CubeManager;
import org.apache.kylin.cube.model.CubeDesc;
import org.apache.kylin.metadata.MetadataManager;
import org.apache.kylin.metadata.model.DataModelDesc;
import org.apache.kylin.metadata.model.DimensionDesc;
import org.apache.kylin.metadata.model.MeasureDesc;
import org.apache.kylin.metadata.model.TblColRef;
import org.apache.kylin.metadata.project.ProjectManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Created by dongli on 11/30/15.
 *
 * In v1.x, DataModelDesc doesn't include Dimensions and Measures, this tool is to fill them from CubeDesc.
 */
public class DataModelDescUpgradeV2 {
    private static final Log logger = LogFactory.getLog(DataModelDescUpgradeV2.class);
    private KylinConfig config = null;
    private ResourceStore store;
    private String[] models;
    private List<String> updatedResources = Lists.newArrayList();
    private List<String> errorMsgs = Lists.newArrayList();

    public DataModelDescUpgradeV2(String[] models) {
        this.config = KylinConfig.getInstanceFromEnv();
        this.store = ResourceStore.getStore(config);
        this.models = models;
    }

    public static void main(String args[]) throws Exception {
        if (args != null && args.length > 1) {
            System.out.println("Usage: java DataModelDescUpradeV2 [Models]; e.g, model1,model2 ");
            return;
        }

        DataModelDescUpgradeV2 metadataUpgrade = new DataModelDescUpgradeV2(args);
        metadataUpgrade.upgrade();

        logger.info("=================================================================");
        logger.info("Run DataModelDescUpradeV2 completed;");

        if (!metadataUpgrade.updatedResources.isEmpty()) {
            logger.info("Following resources are updated successfully:");
            for (String s : metadataUpgrade.updatedResources) {
                logger.info(s);
            }
        } else {
            logger.warn("No resource updated.");
        }

        if (!metadataUpgrade.errorMsgs.isEmpty()) {
            logger.info("Here are the error/warning messages, you may need to check:");
            for (String s : metadataUpgrade.errorMsgs) {
                logger.warn(s);
            }
        } else {
            logger.info("No error or warning messages; The update succeeds.");
        }

        logger.info("=================================================================");
    }

    public void upgrade() {
        logger.info("Reloading Cube Metadata from store: " + store.getReadableResourcePath(ResourceStore.CUBE_DESC_RESOURCE_ROOT));
        CubeDescManager cubeDescManager = CubeDescManager.getInstance(config);
        List<CubeDesc> cubeDescs = cubeDescManager.listAllDesc();
        for (CubeDesc cubeDesc : cubeDescs) {
            if (ArrayUtils.isEmpty(models) || ArrayUtils.contains(models, cubeDesc.getModelName())) {
                upgradeDataModelDesc(cubeDesc);
            }
        }

        verify();
    }

    private void verify() {
        MetadataManager.getInstance(config).reload();
        CubeDescManager.clearCache();
        CubeDescManager.getInstance(config);
        CubeManager.getInstance(config);
        ProjectManager.getInstance(config);
    }

    private void upgradeDataModelDesc(CubeDesc cubeDesc) {
        boolean upgrade = false;
        DataModelDesc modelDesc = cubeDesc.getModel();
        try {
            if (modelDesc != null && modelDesc.getDimensions() == null && modelDesc.getMetrics() == null) {
                List<org.apache.kylin.cube.model.DimensionDesc> cubeDimDescList = cubeDesc.getDimensions();
                if (!CollectionUtils.isEmpty(cubeDimDescList)) {
                    Map<String, HashSet<String>> modelDimMap = Maps.newHashMap();
                    for (org.apache.kylin.cube.model.DimensionDesc cubeDimDesc : cubeDimDescList) {
                        if (!modelDimMap.containsKey(cubeDimDesc.getTable())) {
                            modelDimMap.put(cubeDimDesc.getTable(), new HashSet<String>());
                        }
                        modelDimMap.get(cubeDimDesc.getTable()).addAll(Lists.newArrayList(cubeDimDesc.getDerived() != null ? cubeDimDesc.getDerived() : cubeDimDesc.getColumn()));
                    }

                    List<DimensionDesc> modelDimDescList = Lists.newArrayListWithCapacity(modelDimMap.size());
                    for (Map.Entry<String, HashSet<String>> modelDimEntry : modelDimMap.entrySet()) {
                        DimensionDesc dimDesc = new DimensionDesc();
                        dimDesc.setTable(modelDimEntry.getKey());
                        String[] columns = new String[modelDimEntry.getValue().size()];
                        columns = modelDimEntry.getValue().toArray(columns);
                        dimDesc.setColumns(columns);
                        modelDimDescList.add(dimDesc);
                    }
                    DimensionDesc.capicalizeStrings(modelDimDescList);
                    modelDesc.setDimensions(modelDimDescList);
                    upgrade = true;
                }

                List<MeasureDesc> cubeMeasDescList = cubeDesc.getMeasures();
                if (!CollectionUtils.isEmpty(cubeDimDescList)) {
                    ArrayList<String> metrics = Lists.newArrayListWithExpectedSize(cubeMeasDescList.size());
                    for (MeasureDesc cubeMeasDesc : cubeMeasDescList) {
                        for (TblColRef tblColRef : cubeMeasDesc.getFunction().getParameter().getColRefs()) {
                            metrics.add(tblColRef.getName());
                        }
                    }
                    String[] metricsArray = new String[metrics.size()];
                    modelDesc.setMetrics(metrics.toArray(metricsArray));
                    upgrade = true;
                }
            }

            if (upgrade) {
                store.putResource(modelDesc.getResourcePath(), modelDesc, MetadataManager.MODELDESC_SERIALIZER);
                updatedResources.add(modelDesc.getResourcePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorMsgs.add("Update DataModelDesc[" + modelDesc.getName() + "] failed: " + e.getLocalizedMessage());
        }
    }
}
