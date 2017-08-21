package loaders;

import mini.utils.MyFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigsLoader {
    public Configs loadConfigs(MyFile configsFile) {
        Configs configs = new Configs();
        try {
            List<String> lines = configsFile.getLines();
            createConfigs(lines, configs);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Couldn't load configs file: " + configsFile);
        }
        return configs;
    }

    private boolean takeBooleanValue(String line) {
        String bool = line.split(LoaderSettings.SEPARATOR)[1];
        return bool.equals(LoaderSettings.TRUE);
    }

    private void createConfigs(List<String> lines, Configs configs) {
        configs.setExtraMap(takeBooleanValue(lines.get(0)));
        configs.setTransparency(takeBooleanValue(lines.get(1)));
        configs.setReflection(takeBooleanValue(lines.get(2)));
        configs.setRefraction(takeBooleanValue(lines.get(3)));
        configs.setCastsShadow(takeBooleanValue(lines.get(4)));
        configs.setImportant(takeBooleanValue(lines.get(5)));
        if (lines.size() == 7) {
            // TODO: Create a parser for this
            configs.setDiffuseMaps(takeListValue(lines.get(6)));
        }
    }

    private List<String> takeListValue(String line) {
        String[] split = line.split("\\[")[1].split("]")[0].split(",");
        return Arrays.stream(split).map(String::trim).collect(Collectors.toList());
    }

    private String takeStringValue(String line) {
        return line.split(LoaderSettings.SEPARATOR)[1];
    }
}
