package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.util.Map;

public class ConfigHelper implements Helper<String> {
    private static final String CONFIG_KEY = ConfigHelper.class.getName() + "#config";
    public static final ConfigHelper INSTANCE = new ConfigHelper();

    @Override
    public CharSequence apply(String configKey, Options options) throws IOException {
        Map<String, String> configMap = options.data(CONFIG_KEY);
        String config = null;
        if (configMap != null) {
            config = configMap.get(configKey);
        }
        if (config != null) {
            return config;
        } else {
            return options.fn();
        }
    }
}
