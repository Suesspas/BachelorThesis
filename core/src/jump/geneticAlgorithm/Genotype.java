package jump.geneticAlgorithm;

import jump.WorldMisc;
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

	public Genotype(NeuralNetwork.FlattenNetwork net) { //TODO
//		this.bot = net.;
		this.fitness = 0;
	}

	public Genotype(Genotype genome) { //TODO
//		this.bird = BirdFactory.getBird(genome.bird.net.flatten());
		this.fitness = 0;
	}

	//TODO methodennamen und genotype male female namen ändern
	public static List<Genotype> breed(Genotype male, Genotype female, int childCount, float mutationRate, float mutationStdDev) {
		List<Genotype> children = new ArrayList<Genotype>();
//		for (int ch = 0; ch < childCount; ch++) {
//			NeuralNetwork.FlattenNetwork childNet = male.bird.net.flatten();
//			NeuralNetwork.FlattenNetwork parentNet = female.bird.net.flatten();
//			for (int i = 0; i < childNet.weights.size(); i++) { // 50/50 chance für parent genome mit mutation danach
//				if (Math.random() <= 0.5) {
//					childNet.weights.set(i, parentNet.weights.get(i));
//				}
//			}
//			for (int i = 0; i < childNet.weights.size(); i++) {
//				if (Math.random() <= mutationRate) {
//					childNet.weights.set(i, (float) Math.random()*2*mutationStdDev - mutationStdDev); //TODO verstehen bzw ist das richtig? sollte es nicht die existierenden werte verändern?
//				}
//			}
//			children.add(new Genotype(childNet));
//		}
		return children;
	}
}
