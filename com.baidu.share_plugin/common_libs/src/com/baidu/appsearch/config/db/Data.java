// CHECKSTYLE:OFF
package com.baidu.appsearch.config.db;

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END

/**
 *
 * 服务端配置的数据结构
 *
 * Entity mapped to table server_config_table.
 *
 * @author zhangyuchao, liuqingbiao
 */
public class Data {

    /** INDEX 主键 */
    private Long id;
    /** 配置的KEY，如xxx_enable */
    private String name;
    /** 配置的类型，如：switch_info */
    private Integer type;
    /** 对应配置KEY的值，如：true、action=xxx */
    private String value;

    // KEEP FIELDS - put your custom fields here
    /** 表示客户端请求URL类型 */
    public static final int URL_TYPE = 0;
    /** 表示服务器的配置类型 */
    public static final int SETTING_TYPE = 1;
    /** 表示客户端：活跃、激活、更新激活、有效安装的类型 */
    public static final int EVENT_TYPE = 2;
    /** 表示服务器OEM的配置类型 */
    public static final int OEM_SETTING_TYPE = 3;
    // KEEP FIELDS END

    public Data() {
    }

    public Data(Long id) {
        this.id = id;
    }

    public Data(Long id, String name, Integer type, String value) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    // KEEP METHODS - put your custom methods here
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[name=").append(name).append(",value=").append(value).append(",type=")
                .append(type).append("]");
        return sb.toString();
    }
    // KEEP METHODS END

}
