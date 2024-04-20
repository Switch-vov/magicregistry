package com.switchvov.magicregistry.cluster;

import com.switchvov.magicregistry.model.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

/**
 * @author switch
 * @since 2024/4/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Snapshot {
    private LinkedMultiValueMap<String, InstanceMeta> registry;
    private Map<String, Long> versions;
    private Map<String, Long> timestamps;
    private long version;
}
