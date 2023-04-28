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

    public int[] getTopologyArray(){
        String[] strArray = topology.split(",");
        int[] topology = new int[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            topology[i] = Integer.parseInt(strArray[i]);
        }
        return topology;
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
