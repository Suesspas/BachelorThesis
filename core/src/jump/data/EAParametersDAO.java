package jump.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EAParametersDAO {
    private int id;
    private float mutationRate;
    private boolean isUniform;
    private float mutationStepSize;
    private float elitismRate;
    private float randomnessRate;
    private int populationSize;
    private int childCount;
    private String parentSelection;
    private String crossoverType;
    private String scoreEvaluation;
    private String fitnessCalculation;
    public EAParametersDAO(int eaType) {
        this.id = eaType;
        try {
            ResultSet result = DatabaseConnector.loadEAParametersByID(eaType);
            assert result != null;
            if (!result.next()) return;
            this.mutationRate = result.getFloat("mutation_rate");
            this.isUniform = result.getBoolean("is_uniform");
            this.mutationStepSize = result.getFloat("mutation_step_size");
            this.elitismRate = result.getFloat("elitism_rate");
            this.randomnessRate = result.getFloat("randomness_rate");
            this.populationSize = result.getInt("population_size");
            this.childCount = result.getInt("child_count");
            this.parentSelection = result.getString("parent_selection");
            this.crossoverType = result.getString("crossover_type");
            this.scoreEvaluation = result.getString("score_evaluation");
            this.fitnessCalculation = result.getString("fitness_calculation");
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public int getId() {
        return id;
    }

    public float getMutationRate() {
        return mutationRate;
    }

    public boolean isUniform() {
        return isUniform;
    }

    public float getMutationStepSize() {
        return mutationStepSize;
    }

    public float getElitismRate() {
        return elitismRate;
    }

    public float getRandomnessRate() {
        return randomnessRate;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getChildCount() {
        return childCount;
    }

    public String getParentSelection() {
        return parentSelection;
    }

    public String getCrossoverType() {
        return crossoverType;
    }

    public String getScoreEvaluation() {
        return scoreEvaluation;
    }

    public String getFitnessCalculation() {
        return fitnessCalculation;
    }
}
