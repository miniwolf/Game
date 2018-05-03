package mini.utils;

import mini.app.Application;
import mini.app.state.AbstractApplicationState;
import mini.app.state.ApplicationStateManager;
import mini.asset.AssetInfo;
import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.asset.plugins.UrlAssetInfo;
import mini.input.InputManager;
import mini.input.controls.ActionListener;
import mini.input.controls.Trigger;
import mini.material.MatParam;
import mini.material.Material;
import mini.post.Filter;
import mini.renderer.RenderManager;
import mini.renderer.RendererException;
import mini.scene.Geometry;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.scene.shape.Box;
import mini.shaders.Shader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This appState is for debug purpose only, and was made to provide an easy way to test shaders, with a live update
 * capability.
 * <p>
 * This class provides and easy way to reload a material and catches compilation errors when needed and displays the
 * error in the console.
 * <p>
 * If no error occur on compilation, the material is reloaded in the scene.
 * <p>
 * You can either trigger the reload when pressing a key (or whatever input is supported by Triggers you can attach to
 * the input manager), or trigger it when a specific file (the shader source) has been changed on the hard drive.
 * <p>
 * Usage :
 * <code>
 * MaterialDebugAppState matDebug = new MaterialDebugAppState();
 * stateManager.attach(matDebug);
 * matDebug.registerBinding(new KeyTrigger(Keyboard.KEY_R), whateverGeometry);
 * </code>
 * <p>
 * this will reload the material of whateverGeometry when pressing the R key.
 * <p>
 * <code>matDebug.registerBinding("Shaders/distort.frag", whateverGeometry);</code>
 * <p>
 * this will reload the material of whateverGeometry when the given file is changed on the hard drive.
 * <p>
 * you can also register bindings to the appState with a post process Filter
 */
public class MaterialDebugApplicationState extends AbstractApplicationState {
    private RenderManager renderManager;
    private AssetManager assetManager;
    private InputManager inputManager;
    private List<Binding> bindings = new ArrayList<>();
    private Map<Trigger, List<Binding>> fileTriggers = new HashMap<>();


    @Override
    public void initialize(ApplicationStateManager stateManager, Application app) {
        renderManager = app.getRenderManager();
        assetManager = app.getAssetManager();
        inputManager = app.getInputManager();
        for (Binding binding : bindings) {
            bind(binding);
        }
        super.initialize(stateManager, app);
    }

    /**
     * Will reload the spatial's materials whenever the trigger is fired
     *
     * @param trigger the trigger
     * @param spat    the spatial to reload
     */
    public void registerBinding(Trigger trigger, final Spatial spat) {
        if (spat instanceof Geometry) {
            GeometryBinding binding = new GeometryBinding(trigger, (Geometry) spat);
            bindings.add(binding);
            if (isInitialized()) {
                bind(binding);
            }
        } else if (spat instanceof Node) {
            for (Spatial child : ((Node) spat).getChildren()) {
                registerBinding(trigger, child);
            }
        }
    }


    /**
     * Will reload the filter's materials whenever the trigger is fired.
     *
     * @param trigger the trigger
     * @param filter  the filter to reload
     */
    public void registerBinding(Trigger trigger, final Filter filter) {
        FilterBinding binding = new FilterBinding(trigger, filter);
        bindings.add(binding);
        if (isInitialized()) {
            bind(binding);
        }
    }


    /**
     * Will reload the filter's materials whenever the shader file is changed
     * on the hard drive
     *
     * @param shaderName the shader name (relative path to the asset folder or to a registered asset path)
     * @param filter     the filter to reload
     */
    public void registerBinding(String shaderName, final Filter filter) {
        registerBinding(new FileChangedTrigger(shaderName), filter);
    }

    /**
     * Will reload the spatials's materials whenever the shader file is changed
     * on the hard drive
     *
     * @param shaderName the shader name (relative path to the asset folder or to a registered asset path)
     * @param spat       the spatial to reload
     */
    public void registerBinding(String shaderName, final Spatial spat) {
        registerBinding(new FileChangedTrigger(shaderName), spat);
    }

    private void bind(final Binding binding) {
        if (binding.getTrigger() instanceof FileChangedTrigger) {
            FileChangedTrigger t = (FileChangedTrigger) binding.getTrigger();
            List<Binding> b = fileTriggers.get(t);
            if (b == null) {
                t.init();
                b = new ArrayList<>();
                fileTriggers.put(t, b);
            }
            b.add(binding);
        } else {
            final String actionName = binding.getActionName();
            inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
                if (actionName.equals(name) && isPressed) {
                    //reloading the material
                    binding.reload();
                }
            }, actionName);

            inputManager.addMapping(actionName, binding.getTrigger());
        }
    }

    public Material reloadMaterial(Material mat) {
        //clear the entire cache, there might be more clever things to do, like clearing only the matdef, and the associated shaders.
        assetManager.clearCache();

        //creating a dummy mat with the mat def of the mat to reload
        Material dummy = new Material(mat.getMaterialDef());

        for (MatParam matParam : mat.getParams()) {
            dummy.setParam(matParam.getName(), matParam.getVarType(), matParam.getValue());
        }

        dummy.getAdditionalRenderState().set(mat.getAdditionalRenderState());

        //creating a dummy geom and assigning the dummy material to it
        Geometry dummyGeom = new Geometry("dummyGeom", new Box(1f, 1f, 1f));
        dummyGeom.setMaterial(dummy);

        try {
            //preloading the dummyGeom, this call will compile the shader again
            renderManager.preloadScene(dummyGeom);
        } catch (RendererException e) {
            //compilation error, the shader code will be output to the console
            //the following code will output the error
            //System.err.println(e.getMessage());
            System.err.println(e.getMessage());
            return null;
        }

        //Logger.getLogger(MaterialDebugApplicationState.class.getName()).log(Level.INFO, "Material succesfully reloaded");
        //System.out.println("Material succesfully reloaded");
        return dummy;
    }

    @Override
    public void update(float tpf) {
        super.update(tpf); //To change body of generated methods, choose Tools | Templates.
        for (Trigger trigger : fileTriggers.keySet()) {
            if (trigger instanceof FileChangedTrigger) {
                FileChangedTrigger t = (FileChangedTrigger) trigger;
                if (t.shouldFire()) {
                    List<Binding> b = fileTriggers.get(t);
                    for (Binding binding : b) {
                        binding.reload();
                    }
                }
            }
        }
    }

    private interface Binding {
        String getActionName();

        void reload();

        Trigger getTrigger();
    }

    private class GeometryBinding implements Binding {

        Trigger trigger;
        Geometry geom;

        public GeometryBinding(Trigger trigger, Geometry geom) {
            this.trigger = trigger;
            this.geom = geom;

        }

        public void reload() {
            Material reloadedMat = reloadMaterial(geom.getMaterial());
            //if the reload is successful, we re setup the material with its params and reassign it to the box
            if (reloadedMat != null) {
                // setupMaterial(reloadedMat);
                geom.setMaterial(reloadedMat);
            }
        }

        public String getActionName() {
            return geom.getName() + "Reload";

        }

        public Trigger getTrigger() {
            return trigger;
        }
    }

    private class FilterBinding implements Binding {

        Trigger trigger;
        Filter filter;

        public FilterBinding(Trigger trigger, Filter filter) {
            this.trigger = trigger;
            this.filter = filter;
        }

        public void reload() {
            Field[] fields1 = filter.getClass().getDeclaredFields();
            Field[] fields2 = filter.getClass().getSuperclass().getDeclaredFields();

            List<Field> fields = new ArrayList<>();
            fields.addAll(Arrays.asList(fields1));
            fields.addAll(Arrays.asList(fields2));
            Material m = new Material();
            Filter.Pass p = filter.new Pass();
            try {
                for (Field field : fields) {
                    if (field.getType().isInstance(m)) {
                        field.setAccessible(true);
                        Material mat = reloadMaterial((Material) field.get(filter));
                        if (mat == null) {
                            return;
                        } else {
                            field.set(filter, mat);
                        }

                    }
                    if (field.getType().isInstance(p)) {
                        field.setAccessible(true);
                        p = (Filter.Pass) field.get(filter);
                        if (p != null && p.getPassMaterial() != null) {
                            Material mat = reloadMaterial(p.getPassMaterial());
                            if (mat == null) {
                                return;
                            } else {
                                p.setPassMaterial(mat);
                            }
                        }
                    }
                    if (field.getName().equals("postRenderPasses")) {
                        field.setAccessible(true);
                        List<Filter.Pass> passes;
                        passes = (List<Filter.Pass>) field.get(filter);
                        if (passes != null) {
                            for (Filter.Pass pass : passes) {
                                Material mat = reloadMaterial(pass.getPassMaterial());
                                if (mat == null) {
                                    return;
                                } else {
                                    pass.setPassMaterial(mat);
                                }
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                ex.printStackTrace();
                System.err.println(ex.getMessage());
            }

        }

        public String getActionName() {
            return filter.getName() + "Reload";
        }

        public Trigger getTrigger() {
            return trigger;
        }
    }

    private class FileChangedTrigger implements Trigger {
        String fileName;
        File file;
        Long fileLastM;

        public FileChangedTrigger(String fileName) {
            this.fileName = fileName;
        }

        public void init() {
            AssetInfo info = assetManager.locateAsset(new AssetKey<Shader>(fileName));
            if (info != null && info instanceof UrlAssetInfo) {
                try {
                    Field f = info.getClass().getDeclaredField("url");
                    f.setAccessible(true);
                    URL url = (URL) f.get(info);
                    file = new File(url.getFile());
                    fileLastM = file.lastModified();

                } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | SecurityException ex) {
                    System.err.println(ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }

        public boolean shouldFire() {
            try {
                if (Files.getLastModifiedTime(file.toPath()).toMillis() != fileLastM) {
                    fileLastM = file.lastModified();
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        public String getName() {
            return fileName;
        }

        public int triggerHashCode() {
            return 0;
        }
    }
}