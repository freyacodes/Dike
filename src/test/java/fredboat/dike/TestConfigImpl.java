package fredboat.dike;

import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class TestConfigImpl implements TestConfig {

    private final String testGuild;
    private final String testBotToken;
    private final String testUserToken;

    public TestConfigImpl() {
        Yaml yaml = new Yaml();
        Map<String, Object> config;
        try {
            FileInputStream fis = new FileInputStream(new File("test-credentials.yml"));
            //noinspection unchecked
            config = (Map<String, Object>) yaml.load(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        this.testGuild = (String) config.get("testGuild");
        this.testBotToken = (String) config.get("testBotToken");
        this.testUserToken = (String) config.get("testUserToken");
    }

    @Override
    public String host() {
        return "localhost";
    }

    @Override
    public int port() {
        return 2222;
    }

    @Override
    public List<String> whitelist() {
        return Collections.emptyList();
    }

    @Override
    public int timeoutIdle() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int timeoutIdleFormerGen() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String testGuild() {
        return testGuild;
    }

    @Override
    public String testBotToken() {
        return testBotToken;
    }

    @Override
    public String testUserToken() {
        return testUserToken;
    }

}
