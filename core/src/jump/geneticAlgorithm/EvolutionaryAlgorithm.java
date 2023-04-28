package jump.geneticAlgorithm;

import jump.WorldMisc;
import jump.actors.BotActor;
import jump.actors.GoalActor;
import jump.actors.PlatformActor;
import jump.data.EAParametersDAO;
import jump.data.NNParametersDAO;
import jump.neuralNetwork.NeuralNetwork;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EvolutionaryAlgorithm {

	public Population population;
	public int alive;
	public int generation;
	public final int EA_TYPE = 2;
	public final int NN_TYPE = 1;
	public int populationSize; //= 100;
	public float elitism;// = 0.2f;
	public float mutationRate;// = 0.1f; //TODO load parameters from database, until then:
	public float mutationStdDev;// = 0f; //Just add a new entry in the db and change ea_id
	public float randomness;// = 0.2f;
	public int childCount;// = 1;

	private NeuralNetwork bestGenome;


	public EvolutionaryAlgorithm(List<BotActor> bots) {
		this.population = new Population(bots);
		//this.bestGenome = this.population.genomes.get(0).bird.net;
		this.alive = bots.size();
		this.generation = 1;
	}

	public EvolutionaryAlgorithm() {

		//this.bestGenome = this.population.genomes.get(0).bird.net;
		this.generation = 1;
		EAParametersDAO eaParametersDAO = new EAParametersDAO(EA_TYPE);
		NNParametersDAO nnParametersDAO = new NNParametersDAO(NN_TYPE);
		//TODO use nnParametersDAO and pass topology to BotActor() constructor
		this.populationSize = eaParametersDAO.getPopulationSize();
		this.elitism = eaParametersDAO.getElitismRate();
		this.mutationRate = eaParametersDAO.getMutationRate();
		this.mutationStdDev = 0f;
		this.randomness = eaParametersDAO.getRandomnessRate();
		this.childCount = eaParametersDAO.getChildCount();

		List<BotActor> bots = new ArrayList<>();
		for (int i = 0; i < populationSize; i++){
			bots.add(new BotActor(i));
		}
		this.population = new Population(bots);
		this.alive = bots.size();
	}

	public void updatePopulation(List<PlatformActor> platforms, GoalActor goal, int levelTimer) {
		for (Genotype genome: this.population.genomes) {
			BotActor bot = genome.getBot();
			if (bot.isAlive()) {
				PlatformActor closestPlatform = getClosestPlatform(platforms, bot);

				List<PlatformActor> platformsByDist = getXClosestPlatforms(3, platforms, bot); //TODO feed2, x init
//				List<Float> sortedPlatformDistances = new ArrayList<>();
//				for (PlatformActor platform : platforms) {
//					sortedPlatformDistances.add(bot.distanceTo(platform.getPosition()));
//					bot.angleTo(platform.getPosition());
//				}
//				Collections.sort(sortedPlatformDistances);
//				if (bot.getUserData().getBotNumber() == 1){
//					System.out.println("bot 1 sorted distances" + sortedPlatformDistances);
//				}

//				bot.feed(closestPlatform, bot.distanceTo(goal.getPosition()));
				bot.feed2(platformsByDist, bot.distanceTo(goal.getPosition()));

				bot.update(goal, levelTimer);
				if (bot.isOutOfBounds(WorldMisc.minWorldWidth, WorldMisc.minWorldHeight)) {
					bot.dead();
					this.alive--;
				}
			}
		}
	}


	public void evolvePopulation() {
		this.alive = this.populationSize;
		this.population.evolve(this.elitism, this.randomness, this.mutationRate, this.mutationStdDev, this.childCount, this.generation);
		this.bestGenome = this.population.genomes.get(0).getBot().getNeuralNetwork();
		this.generation++;
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

	private PlatformActor getClosestPlatform(List<PlatformActor> platforms, BotActor bot) {
		PlatformActor closestPlatform = null;
		float distance = Float.MAX_VALUE;
		float test;
		for (PlatformActor platform: platforms) {
			test = bot.distanceTo(platform.getPosition());
			if (test < distance) {
				distance = test;
				closestPlatform = platform;
			}
		}
		return closestPlatform;
	}

	private List<PlatformActor> getXClosestPlatforms(int x, List<PlatformActor> platforms, BotActor bot) {
		if (x >= platforms.size()){
			x = platforms.size();
			System.err.println("Only " + platforms.size() + " platforms in level");
		}
		List<PlatformActor> tempPlats = new LinkedList<>(platforms);
		List<PlatformActor> platformsByDist = new ArrayList<>();
		PlatformActor tempPlat;
		for (int i = 0; i < x; i++){
			tempPlat = getClosestPlatform(tempPlats, bot);
			platformsByDist.add(tempPlat);
			tempPlats.remove(tempPlat);
		}
		tempPlats = null;
		tempPlat = null;
		return platformsByDist;
	}

}
