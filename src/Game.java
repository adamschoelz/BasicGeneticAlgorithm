import java.util.ArrayList;
import java.util.Random;

class Game
{

	static double[] evolveWeights() throws Exception
	{
		// Create a random initial population
		Random r = new Random();
		Matrix population = new Matrix(100, 291);
		int winCount = 0;
		int winAgent = 0;
		for(int i = 0; i < 100; i++)
		{
			double[] chromosome = population.row(i);
			for(int j = 0; j < chromosome.length; j++)
				chromosome[j] = 0.03 * r.nextGaussian();
		}

		// Evolve the population
		for(int m = 0; m < 10; m++){
			//mutation
			for(int i = 0; i < 1000; i++){
				for(int j = 0; j < population.rows(); j++){
					if(r.nextDouble() < .25){
						int k = r.nextInt(291);
						population.row(j)[k] += r.nextGaussian() * 7.0;
					}
				}
			}

			//Tournament
			for(int i = 0; i < 50; i++){
				int chal = i*2;
				int chal2 = i*2+1;
				double[] chrom = population.row(chal);
				double[] chrom2 = population.row(chal2);
				int result = Controller.doBattleNoGui(new NeuralAgent(chrom), new NeuralAgent(chrom2));
				//Save percentage of losers
				/*if(r.nextDouble() < .33){
					result = 0 - result;
				}*/
				if(result < 0){
					//win for chrom2
					double[] mate = population.row(findMostLike(chal2, population));
					for(int k = 0; k < 291; k++){
						double per = r.nextDouble();
						chrom[k] = (chrom2[k]*per + mate[k]*(1-per));
					}
				}
				else{
					//lose for chrom2
					double[] mate = population.row(findMostLike(chal2, population));
					for(int k = 0; k < 291; k++){
						double per = r.nextDouble();
						chrom2[k] = (chrom[k]*per + mate[k]*(1-per));
					}
				}
			}
		}
		
		//Choose an agent that can beat reflex
		long shortest = -1;
		for(int i = 0; i < 100; i++){
			int chal = r.nextInt(100);
			double[] chrom = population.row(chal);
			long starttime = System.nanoTime();
			int reflexResult = Controller.doBattleNoGui(new ReflexAgent(), new NeuralAgent(chrom));
			long finishtime = System.nanoTime();
			long duration = finishtime - starttime;
			if(reflexResult < 0 && shortest == -1){
				winCount++;
				winAgent = chal;
				shortest = duration;
				System.out.println(shortest);
			}
			else if(reflexResult < 0 && duration < shortest){
				winCount++;
				winAgent = chal;
				shortest = duration;
				System.out.println(shortest);
			}
		}
		
		System.out.println(winCount);

		// Return an arbitrary member from the population
		return population.row(winAgent);
	}

	public static int findMostLike(int index, Matrix pop){
		int like = 0;
		double leastDiff = 0;
		for(int i = 0; i < pop.rows(); i++){
			double diff = 0;
			for(int j = 0; j < 291; j++){
				diff += pop.row(index)[j] - pop.row(i)[j];
			}
			if(diff < leastDiff || i == 0){
				leastDiff = diff;
				like = i;
			}
		}
		
		return like;
	}
	
	public static void main(String[] args) throws Exception
	{
		long starttime = System.nanoTime();
		double[] w = evolveWeights();
		long finishtime = System.nanoTime();
		long duration = finishtime - starttime;
		System.out.println("Weight evolution: " + duration);
		Controller.doBattle(new ReflexAgent(), new NeuralAgent(w));
	}

}
