package com.vernon.pgdatadiff.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author Vernon Chen
 * @time 2024年7月27日 下午6:39:47
 */
@Data
@Builder
public class EchoObject {
    private String filePath;
    private String content;
}
