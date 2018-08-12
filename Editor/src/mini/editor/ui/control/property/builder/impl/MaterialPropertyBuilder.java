package mini.editor.ui.control.property.builder.impl;

import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import mini.editor.extension.property.EditableProperty;
import mini.editor.extension.property.EditablePropertyType;
import mini.editor.extension.property.SimpleProperty;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.ui.control.property.builder.PropertyBuilder;
import mini.material.MatParam;
import mini.material.Material;
import mini.shaders.VarType;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MaterialPropertyBuilder extends EditableObjectPropertyBuilder {
    private static final MaterialPropertyBuilder INSTANCE = new MaterialPropertyBuilder();

    private static final ObjectDictionary<VarType, Integer> SIZE_MAP = DictionaryFactory
            .newObjectDictionary();
    private static final Comparator<MatParam> MAT_PARAM_COMPARATOR = ((first, second) -> {
        var firstType = first.getVarType();
        var secondType = second.getVarType();
        return SIZE_MAP.get(secondType, () -> 0) - SIZE_MAP.get(firstType, () -> 0);
    });

    static {
        SIZE_MAP.put(VarType.Int, 2);
        SIZE_MAP.put(VarType.Float, 2);
        SIZE_MAP.put(VarType.Boolean, 3);
    }

    protected MaterialPropertyBuilder() {
        super(ChangeConsumer.class);
    }

    public static MaterialPropertyBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    protected List<EditableProperty<?, ?>> getProperties(Object object) {
        if (!(object instanceof Material)) {
            return null;
        }

        var material = (Material) object;
        var defintion = material.getMaterialDef();

        return defintion.getMaterialParams().stream()
                        .sorted(MAT_PARAM_COMPARATOR)
                        .map(param -> convert(param, material))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
    }

    private EditableProperty<?, Material> convert(
            MatParam param,
            Material material) {
        var propertyType = convert(param.getVarType());
        if (propertyType == null) {
            return null;
        }

        return new SimpleProperty<>(
                propertyType,
                param.getName(),
                0.1F,
                material,
                object -> getParamValue(param, object),
                (object, newValue) -> applyParam(param, object, newValue));
    }

    private void applyParam(
            MatParam param,
            Material material,
            Object newValue) {
        if (newValue == null) {
            material.clearParam(param.getName());
        } else {
            material.setParam(param.getName(), param.getVarType(), newValue);
        }
    }

    private Object getParamValue(MatParam param, Material material) {
        var currentParam = material.getParam(param.getName());
        return currentParam == null ? null : currentParam.getValue();
    }

    private EditablePropertyType convert(VarType varType) {
        switch (varType) {
            case Int:
                return EditablePropertyType.INTEGER;
            case Float:
                return EditablePropertyType.FLOAT;
            case Boolean:
                return EditablePropertyType.BOOLEAN;
        }

        return null;
    }

    @Override
    public int compareTo(PropertyBuilder o) {
        return 0;
    }
}
