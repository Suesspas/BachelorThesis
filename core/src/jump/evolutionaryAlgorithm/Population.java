package jump.evolutionaryAlgorithm;

import jump.actors.BotActor;
import jump.neuralNetwork.NeuralNetwork;

import java.util.ArrayList;
import java.util.List;

public class Population {

	public List<Genotype> genomes;

	public Population(List<BotActor> bots){
		this.genomes = new ArrayList<Genotype>();
		for (int i = 0; i < bots.size(); i++) {
			this.genomes.add(new Genotype(bots.get(i)));
		}
	}

	float[] test_weights = {-0.36999577f, 0.5912553f, -0.38558292f, -0.2923496f, -0.86852086f, 0.22749999f, -0.59749264f,
			-0.5411294f, -0.1251007f, -0.4497448f, -0.24147299f, 0.51523507f, -0.64274216f, 1.0f, -0.45589453f, 0.6580526f,
			-0.20450091f, -0.31741446f, 0.59916925f, -0.26905316f, 0.48049366f, 0.22023061f, 0.23711222f, 0.4307496f, -0.35066372f,
			-0.371162f, -0.42370203f, -0.006182039f, 0.3305713f, -0.20873228f, 0.3424438f, 0.27037615f, -0.07614395f, 0.090668716f,
			-0.21384272f, -0.14446539f, 0.6175434f, 0.6197422f, -0.1170483f, 0.26054934f, -0.48601618f, -0.022899806f, -0.3236448f,
			0.3172856f, 0.0014907718f, 0.15254347f, 0.56482905f, -0.1381852f, -0.020256035f, -0.50991035f};
	int[] test_topology = {7,5,3};
	public void test_evo (String scoreEvaluation){
		List<Genotype> nextGeneration = new ArrayList<Genotype>();
		for (int i = 0; i < genomes.size(); i++) {
			NeuralNetwork nn  = new NeuralNetwork(test_topology);
			NeuralNetwork.FlattenNetwork net  = nn.flatten();//TODO check
			if (i > 0 && i < 50){
				for (int j = 0; j < net.weights.size(); j++) {
					net.weights.set(j, test_weights[j]);
				}
			} else {
				for (int j = 1; j < net.weights.size(); j++) {
					net.weights.set(j, (float) (Math.random()*2 - 1));
				}
			}
			nextGeneration.add(new Genotype(net, nextGeneration.size(), scoreEvaluation));
		}
		this.genomes = nextGeneration;
	}

	public void evolve(float elitism, float randomness, float mutationRate, float mutationRange, int childCount,
					   float mutationStep, boolean isUniform, String parentSelection, String crossoverType, String scoreEvaluation) {
		/*System.out.println("top fitness: " + genomes.get(0).getFitness()
				+ ", " + genomes.get(1).getFitness()
				+ ", " + genomes.get(2).getFitness());
		System.out.println("top scores: " + genomes.get(0).getBot().getScore()
				+ ", " + genomes.get(1).getBot().getScore()
				+ ", " + genomes.get(2).getBot().getScore());*/
		List<Genotype> nextGeneration = new ArrayList<Genotype>();
		int eliteCount = Math.round(elitism*this.genomes.size());
		for (int i = 0; i < eliteCount; i++) {
			nextGeneration.add(new Genotype(this.genomes.get(i),nextGeneration.size(), scoreEvaluation));
		}
		int randomCount = Math.round(randomness*this.genomes.size());
		for (int i = 0; i < randomCount; i++) {
			NeuralNetwork.FlattenNetwork net  = this.genomes.get(0).getBot().getNeuralNetwork().flatten();
			for (int j = 1; j < net.weights.size(); j++) {
				net.weights.set(j, (float) (Math.random()*2 - 1));
			}
			nextGeneration.add(new Genotype(net, nextGeneration.size(), scoreEvaluation));
		}
		// Pool selection
		//TODO change selection based on parentSelection parameter
		if (parentSelection.equals("roulette")){
			this.genomes = randomSelectNextGen(mutationRate, mutationRange, childCount, mutationStep, nextGeneration,
					isUniform, scoreEvaluation, crossoverType);
		} else if (parentSelection.equals("rank")){
			this.genomes = rankingSelectNextGen(mutationRate, mutationRange, childCount, mutationStep, nextGeneration,
					isUniform, scoreEvaluation, crossoverType);
		} else {
			throw new RuntimeException("no valid parent selection parameter");
		}
	}

	private List<Genotype> rankingSelectNextGen(float mutationRate, float mutationRange, int childCount, float mutationStep,
												List<Genotype> nextGeneration, boolean isUniform, String scoreEvaluation,
												String crossoverType) {
		int maxParentIndex = 1;

		outerloop:
		while (true) {
			for (int i = 0; i < maxParentIndex; i++) { //strenge eltern auswahl, keine Wahrscheinlichkeiten für crossover sondern reihenfolge der fitness
				List<Genotype> parents = new ArrayList<>();
				parents.add(this.genomes.get(i));
				parents.add(this.genomes.get(maxParentIndex));
				List<Genotype> children = Genotype.crossOver(parents, childCount, mutationRate, mutationRange,
						mutationStep, isUniform, scoreEvaluation, crossoverType);
				for (Genotype child: children) {
					child.assignBodyNumber(nextGeneration.size());
					nextGeneration.add(child);
					if (nextGeneration.size() >= this.genomes.size()) {
						break outerloop;
					}
				}
			}
			maxParentIndex++;
			maxParentIndex = maxParentIndex >= this.genomes.size() ? 0 : maxParentIndex;
		}
		return nextGeneration;
	}

	private List<Genotype> randomSelectNextGen(float mutationRate, float mutationRange, int childCount, float mutationStep,
											   List<Genotype> nextGeneration, boolean isUniform, String scoreEvaluation,
											   String crossoverType) {
		outerloop:
		while (true){
			List<Genotype> parents = randomParentSelection();
			List<Genotype> children = Genotype.crossOver(parents, childCount, mutationRate,
					mutationRange, mutationStep, isUniform, scoreEvaluation, crossoverType);
			for (Genotype child: children) {
				child.assignBodyNumber(nextGeneration.size());
				nextGeneration.add(child);
				if (nextGeneration.size() >= this.genomes.size()) {
					break outerloop;
				}
			}
		}
		return nextGeneration;
	}

	private List<Genotype> randomParentSelection() {//roulette wheel selection as Eiben.2015 ch 5.2.3 explains --> fitness proportional
		double rand1 = Math.random();
		double rand2 = Math.random();
		Genotype parent1 = null;
		Genotype parent2 = null;
		boolean parent1Set = false;
		boolean parent2Set = false;

		for (Genotype genome : this.genomes) {
			rand1 -= genome.getFitness();
			rand2 -= genome.getFitness();

			if (!parent1Set && rand1 < 0) {
				parent1 = genome;
				parent1Set = true;
			}
			if (!parent2Set && rand2 < 0) {
				parent2 = genome;
				parent2Set = true;
			}

			if (parent1Set && parent2Set) {
				break;
			}
		}

		List<Genotype> selectedParents = new ArrayList<>();
		selectedParents.add(parent1);
		selectedParents.add(parent2);
		return selectedParents;
	}



	public void fitnessEvaluation(String fitnessCalculation) {
		if (fitnessCalculation.equals("weightedSum")){
			this.normalizeFitnessDistribution();
		} else if (fitnessCalculation.equals("other")){
			//optional: setting fitness to Math.pow(fitness,2);
		} else {
			throw new RuntimeException("no valid fitness calc argument");
		}
		this.sortByFitness();
	}

	private void normalizeFitnessDistribution() {
		float sum = 0f;
		for (Genotype genome: this.genomes) {
			sum += genome.getBot().getScore();
		}
		for (Genotype genome: this.genomes) {
			genome.setFitness(genome.getBot().getScore() / sum);
		}
	}
	
	// TODO: Implement quick sort or something else 
	private void sortByFitness() {
		for (int i = 0; i < this.genomes.size()-1; i++) {
			int bestIndex = i;
			for (int j = i+1; j < this.genomes.size(); j++) {
				if (this.genomes.get(j).getFitness() > this.genomes.get(bestIndex).getFitness()) {
					bestIndex = j;
				}
			}
			if (bestIndex != i) {
				Genotype temp = this.genomes.get(bestIndex);
				this.genomes.set(bestIndex, this.genomes.get(i));
				this.genomes.set(i, temp);
			}
		}
	}
}
