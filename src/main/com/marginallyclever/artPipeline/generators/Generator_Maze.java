package com.marginallyclever.artPipeline.generators;

import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Makes a "well formed" maze.
 * See also https://en.wikipedia.org/wiki/Maze_generation_algorithm#Recursive_backtracker
 * @author Dan Royer
 */
public class Generator_Maze extends ImageGenerator {
	// MazeCells are the rooms separted by MazeWalls
	protected class MazeCell {
		int x, y;
		boolean visited;
		boolean onStack;
	}

	// MazeWalls separate MazeCells
	protected class MazeWall {
		int cellA, cellB;
		boolean removed;
	}

	protected static int rows = 5, columns = 5;
	protected float xMax, xMin, yMax, yMin;
	protected MazeCell[] cells;
	protected MazeWall[] walls;
	
	@Override
	public String getName() {
		return Translator.get("MazeName");
	}

	public int getRows() {
		return rows;
	}
	public void setRows(int arg0) {
		if(arg0<1) arg0=1;
		rows=arg0;
	}
	public int getCols() {
		return columns;
	}
	public void setCols(int arg0) {
		if(arg0<1) arg0=1;
		columns=arg0;
	}
	
	@Override
	public ImageGeneratorPanel getPanel() {
		return new Generator_Maze_Panel(this);
	}

	/**
	 * build a list of walls in the maze, cells in the maze, and how they connect to each other.
	 */
	@Override
	public boolean generate() {
		// build the cells
		cells = new MazeCell[rows * columns];

		int x, y, i = 0;
		for (y = 0; y < rows; ++y) {
			for (x = 0; x < columns; ++x) {
				cells[i] = new MazeCell();
				cells[i].visited = false;
				cells[i].onStack = false;
				cells[i].x = x;
				cells[i].y = y;
				++i;
			}
		}

		// build the graph
		walls = new MazeWall[((rows - 1) * columns) + ((columns - 1) * rows)];
		i = 0;
		for (y = 0; y < rows; ++y) {
			for (x = 0; x < columns; ++x) {
				if (x < columns - 1) {
					// vertical wall between horizontal cells
					walls[i] = new MazeWall();
					walls[i].removed = false;
					walls[i].cellA = y * columns + x;
					walls[i].cellB = y * columns + x + 1;
					++i;
				}
				if (y < rows - 1) {
					// horizontal wall between vertical cells
					walls[i] = new MazeWall();
					walls[i].removed = false;
					walls[i].cellA = y * columns + x;
					walls[i].cellB = y * columns + x + columns;
					++i;
				}
			}
		}

		int unvisitedCells = cells.length; // -1 for initial cell.
		int cellsOnStack = 0;

		// Make the initial cell the current cell and mark it as visited
		int currentCell = 0;
		cells[currentCell].visited = true;
		--unvisitedCells;

		// While there are unvisited cells
		while (unvisitedCells > 0) {
			// If the current cell has any neighbours which have not been visited
			// Choose randomly one of the unvisited neighbours
			int nextCell = chooseUnvisitedNeighbor(currentCell);
			if (nextCell != -1) {
				// Push the current cell to the stack
				cells[currentCell].onStack = true;
				++cellsOnStack;
				// Remove the wall between the current cell and the chosen cell
				int wallIndex = findWallBetween(currentCell, nextCell);
				assert (wallIndex != -1);
				walls[wallIndex].removed = true;
				// Make the chosen cell the current cell and mark it as visited
				currentCell = nextCell;
				cells[currentCell].visited = true;
				--unvisitedCells;
			} else if (cellsOnStack > 0) {
				// else if stack is not empty pop a cell from the stack
				for (i = 0; i < cells.length; ++i) {
					if (cells[i].onStack) {
						// Make it the current cell
						currentCell = i;
						cells[i].onStack = false;
						--cellsOnStack;
						break;
					}
				}
			}
		}

		// draw the maze
		drawMaze();
	    
	    return true;
	}

	private void drawMaze() {
		yMin = (float)settings.getMarginBottom();
		yMax = (float)settings.getMarginTop();
		xMin = (float)settings.getMarginLeft();
		xMax = (float)settings.getMarginRight();
		
		float w = (xMax - xMin) / columns;
		float h = (yMax - yMin) / rows;

		turtle = new Turtle();
		
		// Draw outside edge
		turtle.jumpTo(xMin, yMax);
		turtle.moveTo(xMax, yMax);
		turtle.moveTo(xMax, yMin + h);
		// bottom right gap for exit is here
		turtle.jumpTo(xMax, yMin);
		turtle.moveTo(xMin, yMin);
		// top-left gap for entrance is left here
		turtle.moveTo(xMin, yMax - h);

		int i;
		for (i = 0; i < walls.length; ++i) {
			if (walls[i].removed)
				continue;
			int a = walls[i].cellA;
			int b = walls[i].cellB;
			int ax = cells[a].x;
			int ay = cells[a].y;
			int bx = cells[b].x;
			int by = cells[b].y;
			if (ay == by) {
				// vertical wall
				float x = xMin + (ax + 1) * w;
				float y0 = yMin + (ay + 0) * h;
				float y1 = yMin + (ay + 1) * h;
				turtle.jumpTo(x, y0);
				turtle.moveTo(x, y1);
			} else if (ax == bx) {
				// horizontal wall
				float x0 = xMin + (ax + 0) * w;
				float x1 = xMin + (ax + 1) * w;
				float y = yMin + (ay + 1) * h;
				turtle.jumpTo(x0, y);
				turtle.moveTo(x1, y);
			}
		}
	}

	private int chooseUnvisitedNeighbor(int currentCell) {
		int x = cells[currentCell].x;
		int y = cells[currentCell].y;

		int[] candidates = new int[4];
		int found = 0;

		// left
		if (x > 0 && cells[currentCell - 1].visited == false) {
			candidates[found++] = currentCell - 1;
		}
		// right
		if (x < columns - 1 && !cells[currentCell + 1].visited) {
			candidates[found++] = currentCell + 1;
		}
		// up
		if (y > 0 && !cells[currentCell - columns].visited) {
			candidates[found++] = currentCell - columns;
		}
		// down
		if (y < rows - 1 && !cells[currentCell + columns].visited) {
			candidates[found++] = currentCell + columns;
		}

		if (found == 0)
			return -1;

		// choose a random candidate
		int choice = (int) (Math.random() * found);
		assert (choice >= 0 && choice < found);

		return candidates[choice];
	}

	private int findWallBetween(int currentCell, int nextCell) {
		int i;
		for (i = 0; i < walls.length; ++i) {
			if (walls[i].cellA == currentCell || walls[i].cellA == nextCell) {
				if (walls[i].cellB == currentCell || walls[i].cellB == nextCell)
					return i;
			}
		}
		return -1;
	}
}
