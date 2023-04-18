package jump.geneticAlgorithm;

import jump.actors.BotActor;
import jump.neuralNetwork.NeuralNetwork;

import java.util.ArrayList;
import java.util.List;

public class Genotype {

	private BotActor bot;

	private float fitness;

	public BotActor getBot() {
		return bot;
	}

	public float getFitness() {
		return fitness;
	}
	public void setFitness(float fitness) {
		this.fitness = fitness;
	}


	public Genotype(BotActor bot) {
		this.fitness = 0;
		this.bot = bot;
	}

	public Genotype(NeuralNetwork.FlattenNetwork net, int botNumber) {
		this.bot = new BotActor(net, botNumber);
		this.fitness = 0;
	}

	public Genotype(Genotype genome, int botNumber) {
		this.bot = new BotActor(genome.getBot().getNeuralNetwork().flatten(), botNumber);
		this.fitness = 0;
	}

	public static List<Genotype> crossOver(Genotype geneA, Genotype geneB, int childCount, float mutationRate, float mutationStdDev) {
		List<Genotype> children = new ArrayList<Genotype>();
		for (int ch = 0; ch < childCount; ch++) {
			NeuralNetwork.FlattenNetwork childNet = geneA.bot.getNeuralNetwork().flatten();
			NeuralNetwork.FlattenNetwork parentNet = geneB.bot.getNeuralNetwork().flatten();
			for (int i = 0; i < childNet.weights.size(); i++) { // 50/50 chance für parent genome mit mutation danach
				if (Math.random() <= 0.5) {
					childNet.weights.set(i, parentNet.weights.get(i));
				}
			}
			for (int i = 0; i < childNet.weights.size(); i++) {
				if (Math.random() <= mutationRate) {
					childNet.weights.set(i, (float) Math.random()*2*mutationStdDev - mutationStdDev); //TODO schauen, ob der Wertebereich sinvoll ist
				}
			}
			children.add(new Genotype(childNet, 0)); //default bot number 0, is set after method call
		}
		return children;
	}

	public void setNumber(int botNumber) {
		this.bot.setNumber(botNumber);
	}
}
