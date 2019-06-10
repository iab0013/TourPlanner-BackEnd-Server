package colony;

import model.Path;

public class Ant {
	
	private double timeLimit;
	private int pheromoneSize;
	private double pheromones[];
	private Path antPath;
	
	
	public Ant(int numberOfVertex, double timeLimit){
		this.timeLimit=timeLimit;
		this.pheromoneSize=numberOfVertex;
	}
	
	public void clear(){
		antPath = new Path(timeLimit);
	}
	
	public void setAntPath(Path antPath) {
		this.antPath = antPath;
	}
	
	public Path getAntPath() {
		return antPath;
	}
	
}
