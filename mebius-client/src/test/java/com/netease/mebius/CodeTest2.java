/**
 * @(#)CodeTest2.java, 2023/7/21.
 * <p/>
 * Copyright 2023 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.mebius;

import com.netease.mebius.client.action.CodeAnalyze;
import com.netease.mebius.client.enums.ExecType;
import com.netease.mebius.client.enums.GitAccessType;
import com.netease.mebius.client.model.ChangeClassInfo;
import com.netease.mebius.client.model.ProjectAnalyzeResult;
import com.netease.mebius.client.model.project.ProjectParam;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenyiqing@corp.netease.com on 2023/7/21.
 */
public class CodeTest2 {

    public static Long converterTodayTimestamp() {
        LocalDate currentDate = Instant.ofEpochMilli(System.currentTimeMillis())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return currentDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }


    public static void main(String[] args) {

        List<ProjectParam> projectParams = new ArrayList<>();
        ProjectParam projectParam = new ProjectParam();
        projectParam.setProjectRootPath("/Users/chenyiqing/temp/yanxuan-qc-api");
        projectParam.setExecType(ExecType.BRANCH_DIFF);
        projectParam.setCurrentBranch("release-20230725");
        projectParam.setCompareBranch("master");
        projectParam.getGitParam().setGitAccessType(GitAccessType.ACCESS_TOKEN);
        projectParam.getGitParam().setGitAccessToken("ryrJy12SfyqNwFi1RVxs");

        projectParams.add(projectParam);
        try {
/*            List<ClassInfo> classInfos = CodeDiff.codeDiff(projectParam);
            for (ClassInfo classInfo : classInfos) {
                if (classInfo.getClassName().contains("ApplyStatusNotifySkuTO")) {
                    System.out.println(classInfo);
                }
            }*/

            ProjectAnalyzeResult result = CodeAnalyze.analyzeProjectWithDiff(projectParams, null);
            System.out.println(result);
            for (ChangeClassInfo classInfo : result.getChangeClasses()) {
                if (classInfo.getClassName().contains("ChannelApplyRefundTO")) {
                    System.out.println(classInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}