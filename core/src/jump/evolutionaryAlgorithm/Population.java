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
		int randomCount = Math.round(randomness*this.genomes.size()); //TODO adjust complete randomness
		for (int i = 0; i < randomCount; i++) {
			NeuralNetwork.FlattenNetwork net  = this.genomes.get(0).getBot().getNeuralNetwork().flatten();
			for (int j = 1; j < net.weights.size(); j++) {
				net.weights.set(j, (float) (Math.random()*2 - 1));
			}
			nextGeneration.add(new Genotype(net, nextGeneration.size(), scoreEvaluation));
		}
		// Pool selection
		//TODO change selection based on parentSelection parameter
		if (parentSelection.equals("std")){
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
		int max = 1;
		outerloop:
		while (true) {
			for (int i = 0; i < max; i++) { //strenge eltern auswahl, keine Wahrscheinlichkeiten für crossover sondern reihenfolge der fitness
				List<Genotype> children = Genotype.crossOver(this.genomes.get(i), this.genomes.get(max), childCount,
						mutationRate, mutationRange, mutationStep, isUniform, scoreEvaluation, crossoverType);
				for (Genotype child: children) {
					child.assignBodyNumber(nextGeneration.size());
					nextGeneration.add(child);
					if (nextGeneration.size() >= this.genomes.size()) {
						break outerloop;
					}
				}
			}
			max++;
			max = max >= this.genomes.size() ? 0 : max;
		}
		return nextGeneration;
	}

	private List<Genotype> randomSelectNextGen(float mutationRate, float mutationRange, int childCount, float mutationStep,
											   List<Genotype> nextGeneration, boolean isUniform, String scoreEvaluation,
											   String crossoverType) {
		outerloop:
		while (true){
			List<Genotype> parents = randomParentSelection();
			List<Genotype> children = Genotype.crossOver(parents.get(0), parents.get(1), childCount, mutationRate,
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
		if (fitnessCalculation.equals("std")){
			this.weightedSumFitnessDistribution();
		} else if (fitnessCalculation.equals("other")){
			//other fitness calc
		} else {
			throw new RuntimeException("no valid fitness calc argument");
		}
		this.sortByFitness();
	}

	private void weightedSumFitnessDistribution() {
		float sum = 0f;
		for (Genotype genome: this.genomes) {
			sum += genome.getBot().getScore();
		}
		for (Genotype genome: this.genomes) {
			genome.setFitness(genome.getBot().getScore() / sum); //optional: setting fitness to Math.pow(fitness,2);
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
