package ru.timeconqueror.timecore.api.common.config;

import net.minecraftforge.common.config.Configuration;
import ru.timeconqueror.lootgames.common.config.LGConfigs;

import javax.annotation.Nullable;

public abstract class ConfigSection {
    private final String key;
    @Nullable
    private final String comment;
    @Nullable
    private String parentKey = null;

    public ConfigSection(String key, @Nullable String comment) {
        this.key = key;
        this.comment = comment;
    }

    public ConfigSection(@Nullable ConfigSection parent, String key, @Nullable String comment) {
        this(key, comment);
        this.parentKey = parent.getKey();
    }

    public ConfigSection(@Nullable String parentKey, String key, @Nullable String comment) {
        this(key, comment);
        this.parentKey = parentKey;
    }

    public String getKey() {
        return key;
    }

    public String getCategoryName() {
        return LGConfigs.mergeCategories(parentKey, getKey());
    }

    protected abstract void init(Configuration config);

    public String getComment() {
        return comment;
    }
}
