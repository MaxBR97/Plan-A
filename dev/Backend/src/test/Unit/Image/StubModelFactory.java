package Unit.Image;

import java.util.Set;

import DataAccess.ModelRepository;
import Model.ModelFactory;
import Model.ModelInterface;
import Model.ModelParameter;
import Model.ModelSet;
import org.springframework.core.env.Environment;

public class StubModelFactory extends ModelFactory {
    private final StubModel stubModel;

    public StubModelFactory(ModelRepository repo) {
        super(repo, 4000, null);
        this.stubModel = new StubModel();
    }

    @Override
    public ModelInterface getModel(String id) throws Exception {
        return stubModel;
    }

    @Override
    public ModelInterface getModel(String id, Set<ModelSet> sets, Set<ModelParameter> params) throws Exception {
        // Add the sets and params to the stub model
        for (ModelSet set : sets) {
            stubModel.addSet(set);
        }
        for (ModelParameter param : params) {
            stubModel.addParameter(param);
        }
        return stubModel;
    }

    @Override
    public ModelInterface getModel(String id, String role, Set<ModelSet> sets, Set<ModelParameter> params) throws Exception {
        return getModel(id, sets, params);
    }

    @Override
    public ModelInterface getModel(String id, String role) throws Exception {
        return getModel(id);
    }

    // Helper method to get the stub model for test setup
    public StubModel getStubModel() {
        return stubModel;
    }
} 