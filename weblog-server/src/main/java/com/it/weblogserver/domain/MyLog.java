package com.it.weblogclient.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @TableName log
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "log")
public class MyLog implements Serializable {
    /**
     * 日志id
     */
    @Id
    @ToolParam(required = false)
    private Long id;
    /**
     * 日志关联的app的Id
     */
    @ToolParam(required = false)
    private Integer appId;
    /**
     * 记录内容
     */
    @ToolParam(required = false)
    private String content;
    /**
     * 请求方法
     */
    @ToolParam(required = false)
    private String requestMethod;

    /**
     * 请求参数
     */
    @ToolParam(required = false)
    private String requestParams;
    /**
     * 请求url
     */
    @ToolParam(required = false)
    private String requestUrl;
    /**
     * 创建时间
     */
    @ToolParam(required = false,description = "起始时间")
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;

    @ToolParam(required = false,description = "日志类型，例如NullPointException，zero/0, OutOfHeapMemory")
    private String type;
}