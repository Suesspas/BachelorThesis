package jump.geneticAlgorithm;

import jump.actors.BotActor;
import jump.actors.PlatformActor;
import jump.neuralNetwork.NeuralNetwork;

import java.util.List;

public class GeneticAlgorithm {
	
	public class PlatformInfo {
		public float distance;
		public PlatformActor closestPlatform;
		
		public PlatformInfo(float distance, PlatformActor closestPlatform) {
			this.distance = distance;
			this.closestPlatform = closestPlatform;
		}
	}

	public Population population;
	public int alive;
	public int generation;
	
	public int populationSize = 100;
	public float elitism = 0.2f;
	public float mutationRate = 0.1f;
	public float mutationStdDev = 0.5f;
	public float randomness = 0.2f;
	public int childCount = 1;
	
	private NeuralNetwork bestGenome;
	private PlatformInfo closestPlatformInfo;

	//TODO was ist mit bias? wo wird er eingebaut und ist er nötig?

	public GeneticAlgorithm(BotActor[] bots) {
		this.population = new Population(bots);
		//this.bestGenome = this.population.genomes.get(0).bird.net;
		this.alive = bots.length;
		this.generation = 1;
	}
	
	public void updatePopulation(List<PlatformActor> platforms) {
		for (Genotype genome: this.population.genomes) {
			BotActor bot = genome.getBot();
			getClosestPlatform(platforms, bot);
			if (bot.isAlive()) {
				bot.feed(this.closestPlatformInfo.closestPlatform, this.closestPlatformInfo.distance);
				bot.update();
//				if (WHENBOTISDEAD) { //TODO
//					bot.dead();
//					this.alive--;
//				}
			}
		}
	}
	
	public void evolvePopulation() {
		this.alive = this.populationSize;
		this.generation++;
		this.population.evolve(this.elitism, this.randomness, this.mutationRate, this.mutationStdDev, this.childCount);
		this.bestGenome = this.population.genomes.get(0).getBot().getNeuralNetwork();
	}
	
	public NeuralNetwork getBestGenome() {
		return this.bestGenome;
	}
	
	public PlatformInfo getClosestPlatformInfo() {
		return this.closestPlatformInfo;
	}
	
	public int getBestScore() {
		int best = 0;
		for (Genotype genome: this.population.genomes) {
			int score = genome.getBot().getScore();
			if (score > best) {
				best = score;
			}
		}
		return best;
	}
	
	public boolean populationDead() {
		for (Genotype genome: this.population.genomes) {
			if (genome.getBot().isAlive()) {
				return false;
			}
		}
		return true;
	}
	
	private void getClosestPlatform(List<PlatformActor> platforms, BotActor bot) {
		PlatformActor closestPlatform = null;
		float distance = Float.MAX_VALUE;
		for (PlatformActor platform: platforms) {
			float test = platform.getX() + platform.getWidth()/2 - platform.getX(); //TODO y coords
			if (Math.abs(test) < Math.abs(distance)) {
				distance = test;
				closestPlatform = platform;
			}
		}
		this.closestPlatformInfo = new PlatformInfo(distance, closestPlatform);
	}
}
