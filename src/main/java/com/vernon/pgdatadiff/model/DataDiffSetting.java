package com.vernon.pgdatadiff.model;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.Data;

/**
 * @author Vernon Chen
 * @time 2024年7月30日 下午1:10:46
 */
@Data
public class DataDiffSetting {
    private DataDiffMiscSetting misc;
    private List<DataDiffConfigItem> compares = Lists.newArrayList();
}
