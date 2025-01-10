package Image;

import Image.Modules.*;

import java.util.HashSet;
import java.util.Set;

public class Image {
    private Set<ConstraintModule> constraintsModules;
    private Set<PreferenceModule> preferenceModules;

    public Image() {
        constraintsModules = new HashSet<>();
        preferenceModules = new HashSet<>();
    }
}
