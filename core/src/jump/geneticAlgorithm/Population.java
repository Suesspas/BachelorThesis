package jump.geneticAlgorithm;

import jump.actors.BotActor;
import jump.neuralNetwork.NeuralNetwork;

import java.util.ArrayList;
import java.util.List;

public class Population {

	public List<Genotype> genomes;

	public Population(BotActor[] bots){
		this.genomes = new ArrayList<Genotype>();
		for (int i = 0; i < bots.length; i++) {
			this.genomes.add(new Genotype(bots[i]));
		}
	}

	public void evolve(float elitism, float randomness, float mutationRate, float mutationStdDev, int childCount) {
		this.normalFitnessDistribution();
		this.sortByFitness();
		List<Genotype> nextGeneration = new ArrayList<Genotype>();
		int eliteCount = Math.round(elitism*this.genomes.size());
		for (int i = 0; i < eliteCount; i++) {
			nextGeneration.add(new Genotype(this.genomes.get(i)));
		}
		int randomCount = Math.round(randomness*this.genomes.size());
		for (int i = 0; i < randomCount; i++) {
			NeuralNetwork.FlattenNetwork net  = this.genomes.get(0).getBot().getNeuralNetwork().flatten();
			for (int j = 1; j < net.weights.size(); j++) {
				net.weights.set(j, (float) (Math.random()*2 - 1));
			}
			nextGeneration.add(new Genotype(net));
		}
		// Pool selection
		int max = 0;
		while (true) {
			for (int i = 0; i < max; i++) { //strenge eltern auswahl, keine Wahrscheinlichkeiten für crossover sondern reihenfolge der fitness
				List<Genotype> children = Genotype.breed(this.genomes.get(i), this.genomes.get(max), childCount, mutationRate, mutationStdDev);
				for (Genotype child: children) {
					nextGeneration.add(child);
					if (nextGeneration.size() >= this.genomes.size()) {
						this.genomes = nextGeneration;
						return;
					}
				}
			}
			max++;
			max = max >= this.genomes.size()-1 ? 0 : max;
		}
	}

	private void normalFitnessDistribution() {
		float sum = 0f;
		for (Genotype genome: this.genomes) {
			sum += genome.getBot().getScore();
		}
		for (Genotype genome: this.genomes) {
			genome.setFitness(genome.getBot().getScore()/ sum);
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
