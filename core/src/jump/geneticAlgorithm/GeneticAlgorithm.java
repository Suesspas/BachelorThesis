package jump.geneticAlgorithm;

import jump.GameStage;
import jump.actors.BotActor;
import jump.actors.GoalActor;
import jump.actors.PlatformActor;
import jump.neuralNetwork.NeuralNetwork;

import java.util.List;

public class GeneticAlgorithm {

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

	//TODO was ist mit bias? wo wird er eingebaut und ist er nötig?

	public GeneticAlgorithm(List<BotActor> bots) {
		this.population = new Population(bots);
		//this.bestGenome = this.population.genomes.get(0).bird.net;
		this.alive = bots.size();
		this.generation = 1;
	}
	
	public void updatePopulation(PlatformActor[] platforms, GoalActor goal) {
		for (Genotype genome: this.population.genomes) {
			BotActor bot = genome.getBot();
			if (bot.isAlive()) {
				PlatformActor closestPlatform = getClosestPlatform(platforms, bot);
				bot.feed(closestPlatform, bot.distanceTo(closestPlatform.getPosition()));
				bot.update(goal);
				if (bot.isOutOfBounds(GameStage.minWorldWidth, GameStage.minWorldHeight)) { //TODO
					bot.dead();
					this.alive--;
				}
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
	
	public float getBestScore() {
		float best = 0;
		for (Genotype genome: this.population.genomes) {
			float score = genome.getBot().getScore();
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

	private PlatformActor getClosestPlatform(PlatformActor[] platforms, BotActor bot) {
		PlatformActor closestPlatform = null;
		float distance = Float.MAX_VALUE;
		for (PlatformActor platform: platforms) {
			float test = bot.distanceTo(platform.getPosition());
			if (test < distance) {
				distance = test;
				closestPlatform = platform;
			}
		}
		return closestPlatform;
	}
}
