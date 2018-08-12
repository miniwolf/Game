package mini.editor.control.transform;

import mini.editor.annotation.EditorThread;
import mini.renderer.Camera;

public interface EditorTransformSupport {
    @EditorThread
    Camera getCamera();

    enum TransformType {
        MOVE_TOOL,
        ROTATE_TOOL,
        SCALE_TOOL,
        NONE;

        private static final TransformType[] VALUES = values();

        public static TransformType valueOf(int index) {
            return VALUES[index];
        }
    }

    enum TransformationMode {
        LOCAL {
        },

        GLOBAL {

        },
        VIEW {

        };

        private static final TransformationMode[] VALUES = values();

        public static TransformationMode valueOf(final int index) {
            return VALUES[index];
        }
    }
}
