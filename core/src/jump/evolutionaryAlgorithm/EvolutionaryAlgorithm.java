package jump.evolutionaryAlgorithm;

import jump.ConfigManager;
import jump.WorldMisc;
import jump.actors.BotActor;
import jump.actors.GoalActor;
import jump.actors.PlatformActor;
import jump.data.DatabaseConnector;
import jump.data.EAParametersDAO;
import jump.data.NNParametersDAO;
import jump.neuralNetwork.NeuralNetwork;

import java.util.*;

public class EvolutionaryAlgorithm {

	public Population population;
	public int alive;
	public int generation;
	private int eaType;
	private int nnType;
	public int populationSize; //= 100;
	public float elitism;// = 0.2f;
	public float mutationRate;// = 0.1f; //old default values
	public float mutationStep;
	public float mutationRange;// = 0f; // if mutation uniform --> value in [-mRange, mRange]
	public float randomness;// = 0.2f;
	public int childCount;// = 1;

	private NeuralNetwork bestGenome;
	private int runID;
	private boolean isUniform;
	private String parentSelection;
	private String crossoverType;
	private String scoreEvaluation;
	private String fitnessCalculation;
	private String NN_input;


	public EvolutionaryAlgorithm(List<BotActor> bots) {
		this.population = new Population(bots);
		//this.bestGenome = this.population.genomes.get(0).bird.net;
		this.alive = bots.size();
		this.generation = 1;
	}

	public EvolutionaryAlgorithm(boolean firstSetup) {

		//this.bestGenome = this.population.genomes.get(0).bird.net;

		eaType = ConfigManager.getInstance().getCurrentEAconf();
		nnType =  ConfigManager.getInstance().getCurrentNNconf();
		this.generation = 1;
		EAParametersDAO eaParametersDAO = new EAParametersDAO(eaType);
		NNParametersDAO nnParametersDAO = new NNParametersDAO(nnType);
		int[] nnTopology = nnParametersDAO.getTopologyArray();
		runID = DatabaseConnector.saveRun(WorldMisc.level, nnType, eaType);
		this.populationSize = eaParametersDAO.getPopulationSize();
		this.elitism = eaParametersDAO.getElitismRate();
		this.mutationRate = eaParametersDAO.getMutationRate();
		this.mutationStep = eaParametersDAO.getMutationStepSize();
		this.mutationRange = 1f; //uniform mutation makes NN weights in [-1,1]
		this.randomness = eaParametersDAO.getRandomnessRate();
		this.childCount = eaParametersDAO.getChildCount();
		this.isUniform = eaParametersDAO.isUniform();
		this.parentSelection = eaParametersDAO.getParentSelection();
		this.crossoverType = eaParametersDAO.getCrossoverType();
		this.fitnessCalculation = eaParametersDAO.getFitnessCalculation();
		this.NN_input = nnParametersDAO.getInputType();

		this.scoreEvaluation = eaParametersDAO.getScoreEvaluation();
		List<BotActor> bots = new ArrayList<>();
		System.out.println("nnTopology is " + Arrays.toString(nnTopology));
		for (int i = 0; i < populationSize; i++){
			if (firstSetup){
				bots.add(new BotActor(i, nnTopology, this.scoreEvaluation));
			} else {
				bots.add(new BotActor(i, nnTopology, this.scoreEvaluation, false));
			}
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
				if (NN_input.equals("distances")){
					bot.feedDistances(platformsByDist, bot.distanceTo(goal.getPosition()));
				} else if (NN_input.equals("positions")) {
					bot.feedPositions(platformsByDist.get(0), bot.distanceTo(goal.getPosition()));
				} else {
					throw new RuntimeException("invalid NN input parameter");
				}

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
		this.population.fitnessEvaluation(this.fitnessCalculation);
		DatabaseConnector.saveGeneration(this.population, this.generation, this.runID); //here because fitness is calculated before
		this.population.evolve(this.elitism, this.randomness, this.mutationRate, this.mutationRange, this.childCount,
				this.mutationStep, this.isUniform, this.parentSelection, this.crossoverType, this.scoreEvaluation);
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

	private List<PlatformActor> _getXClosestPlatforms(int x, List<PlatformActor> platforms, BotActor bot) {
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

	private PriorityQueue<PlatformActor> platformQueue;
	private List<PlatformActor> platformsByDist;

	private List<PlatformActor> getXClosestPlatforms(int x, List<PlatformActor> platforms, BotActor bot) {
		if (platformQueue == null) {
			platformQueue = new PriorityQueue<>(x, new Comparator<PlatformActor>() {
				@Override
				public int compare(PlatformActor p1, PlatformActor p2) {
					return Double.compare(bot.distanceTo(p1.getPosition()), bot.distanceTo(p2.getPosition()));
				}
			});
		} else {
			platformQueue.clear();
		}

		for (PlatformActor platform : platforms) {
			double distance = bot.distanceTo(platform.getPosition());
			if (platformQueue.size() < x || distance < bot.distanceTo(platformQueue.peek().getPosition())) {
				platformQueue.offer(platform);
			}
			if (platformQueue.size() > x) {
				platformQueue.poll();
			}
		}

		if (platformsByDist == null) {
			platformsByDist = new ArrayList<>(x);
		} else {
			platformsByDist.clear();
		}
		platformsByDist.addAll(platformQueue);
		Collections.reverse(platformsByDist);

		return platformsByDist;
	}
}
