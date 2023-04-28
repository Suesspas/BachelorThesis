package jump.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NNParametersDAO {
    private int id;
    private String topology;
    private String inputType;

    public NNParametersDAO(int id) {
        this.id = id;
        try {
            ResultSet result = DatabaseConnector.loadParametersByID(id, DatabaseConnector.ParameterType.NN);
            assert result != null;
            if (!result.next()) return;
            this.topology = result.getString("topology");
            this.inputType = result.getString("input_type");
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public int getId() {
        return id;
    }

    public String getTopology() {
        return topology;
    }

    public String getInputType() {
        return inputType;
    }
}
