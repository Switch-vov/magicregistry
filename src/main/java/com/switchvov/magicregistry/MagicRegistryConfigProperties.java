package com.switchvov.magicregistry;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * registry config properties.
 *
 * @author switch
 * @since 2024/4/16
 */
@Data
@ConfigurationProperties(prefix = "magicregistry")
public class MagicRegistryConfigProperties {
    private List<String> serverList;
}
