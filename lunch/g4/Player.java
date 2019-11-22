package lunch.g4;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import lunch.sim.Point;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Animal;
import lunch.sim.AnimalType;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.PlayerState;

/**
 * @author adityasridhar
 * 
 * Represents a COMS 4444 player that is having lunch
 *
 */
public class Player implements lunch.sim.Player
{
	private Random random;
	private Integer id;
	private Integer turn;
	private String avatars;
	private List<Animal> monkeys = new ArrayList<>();
	private List<Animal> geese = new ArrayList<>();
	private List<T> monkeyT = new ArrayList<>();
	private List<T> geeseT = new ArrayList<>();
	private List<Point> targetCorners = Arrays.asList(new Point[]{
			new Point(-50, -50), // Top-left corner
			new Point(-50, 50),  // Bottom-left corner
			new Point(50, -50)	 // Top-right corner
			});
	private Map<Integer, Point> targetCornersChosen = new HashMap<>();
	private FoodType foodCurrentlySearchingFor = null;
	private static final double MONKEY_DISTANCE_THRESHOLD = 6.0 + 10e-6;
	private static final double GOOSE_DISTANCE_THRESHOLD = 5.0 + 10e-6;
	
	/**
	 * Player constructor
	 */
	public Player() {
		turn = 0;
	}

	/**
	 * Initializes the player
	 * 
	 * @param members: all family members
	 * @param id: player ID
	 * @param f: number of family members
	 * @param animals: all animals
	 * @param m: number of monkeys
	 * @param g: number of geese
	 * @param t: simulation time
	 * @param s: seed
	 * 
	 * @return the avatars the player will use
	 * 
	 */
	public String init(ArrayList<Family> members, Integer id, int f, ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s) {
		this.id = id;
		avatars = "flintstone";
		random = new Random();
		monkeyT = new ArrayList<>();
		geeseT = new ArrayList<>();

		for(Animal animal : animals) {
			if(animal.which_animal() == AnimalType.MONKEY) {
				monkeyT.add(new T(animal.get_location(), true));
			}
			else if(animal.which_animal() == AnimalType.GOOSE)
				geeseT.add(new T(animal.get_location(), false));
		}

		return avatars;
	}
	
	/**
	 *
	 * Determines the command the player submits based on
	 * information about family members, the available
	 * food items, and the animals
	 *
	 * @param members: all family members
	 * @param animals: all animals
	 * @param ps: this player's state
	 * 
	 * @return the command/action submitted by the player
	 *
	 */
	public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
		int mt = 0;
		int gt = 0;
		for(Animal animal : animals) {
			if(animal.which_animal() == AnimalType.MONKEY) {
				monkeyT.get(mt).update(animal.get_location());
				mt++;
			}
			else if(animal.which_animal() == AnimalType.GOOSE) {
				geeseT.get(gt).update(animal.get_location());
				gt++;
			}
		}

		System.out.println("Monkey!");
		for(T a : monkeyT) {
			System.out.println(a.location);
			System.out.println("Next: " + a.getNext());
			//if(a.slope == Double.MAX_VALUE) System.out.println("undefined");
			//else System.out.println(a.slope);
		}
		System.out.println("Geese!");
		for(T a: geeseT) {
			System.out.println(a.location);
			System.out.println("Next: " + a.getNext());
			//if(a.slope == Double.MAX_VALUE) System.out.println("undefined");
			//else System.out.println(a.slope);
		}

		if(turn < 100) {
			boolean foundValidMove = false;
			Point nextMove = new Point(-1, -1);
			while(!foundValidMove) {
				Double bearing = random.nextDouble() * 2 * Math.PI;
				nextMove = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
				foundValidMove = Point.within_bounds(nextMove);
			}
			turn++;
			return Command.createMoveCommand(nextMove);
		}

		// Determine animals sorted by closest distance to the player
		ArrayList<Animal> clonedAnimals = new ArrayList<>(animals);
		Collections.sort(clonedAnimals, new Comparator<Animal>() {
		    public int compare(Animal animal1, Animal animal2) {
				double distanceToAnimal1 = Point.dist(ps.get_location(), animal1.get_location());
				double distanceToAnimal2 = Point.dist(ps.get_location(), animal2.get_location());
		        return (int) Math.signum(distanceToAnimal1 - distanceToAnimal2);
		    }
		});
						
		// Assign monkeys and geese, each sorted by closest distance to the player
		monkeys = new ArrayList<>();
		geese = new ArrayList<>();
		
		for(Animal animal : clonedAnimals) {
			if(animal.which_animal() == AnimalType.MONKEY && !animal.busy_eating()) {
				monkeys.add(animal);
			}
			else if(animal.which_animal() == AnimalType.GOOSE && !animal.busy_eating())
				geese.add(animal);
		}
		
		boolean monkeysTooClose, gooseTooClose;
		
		// Determine if at least 3 monkeys are too close
		//System.out.println();
		if(monkeys.size() < 3)
			monkeysTooClose = false;
		else {
			double distanceToFirstMonkey = Point.dist(ps.get_location(), monkeys.get(0).get_location());			
			double distanceToSecondMonkey = Point.dist(ps.get_location(), monkeys.get(1).get_location());			
			double distanceToThirdMonkey = Point.dist(ps.get_location(), monkeys.get(2).get_location());			
			if(distanceToFirstMonkey <= MONKEY_DISTANCE_THRESHOLD && distanceToSecondMonkey <= MONKEY_DISTANCE_THRESHOLD && distanceToThirdMonkey <= MONKEY_DISTANCE_THRESHOLD) {
				monkeysTooClose = true;
				//System.out.println("Monkey distances: [" + distanceToFirstMonkey + ", " + distanceToSecondMonkey + ", " + distanceToThirdMonkey + "]");
			}
			else
				monkeysTooClose = false;
		}
		
		// Determine if any goose is too close
		if(geese.size() == 0 || Point.dist(ps.get_location(), geese.get(0).get_location()) > GOOSE_DISTANCE_THRESHOLD)
			gooseTooClose = false;
		else {
			//System.out.println("Goose distance: " + Point.dist(ps.get_location(), geese.get(0).get_location()));
			gooseTooClose = true;
		}
		
		// Print the state of the player
		//System.out.println("Player is still holding item: " + (ps.get_held_item_type() != null));
		//System.out.println("Player is still searching: " + (ps.is_player_searching()));
		printAvailability(ps);

		// Abort taking out the food item if the animal is too close
		if(ps.is_player_searching() && ps.get_held_item_type() == null &&
				(monkeysTooClose || (gooseTooClose && foodCurrentlySearchingFor == FoodType.SANDWICH))) {
			//System.out.println("Player " + id + " is aborting search.");
			foodCurrentlySearchingFor = null;
			return new Command(CommandType.ABORT);
		}

		// Keep the food item back if the animal is too close
		if(!ps.is_player_searching() && ps.get_held_item_type() != null && (((monkeysTooClose || gooseTooClose) && (ps.get_held_item_type() == FoodType.SANDWICH)) ||
				(monkeysTooClose && (ps.get_held_item_type() != FoodType.SANDWICH)))) {
			foodCurrentlySearchingFor = null;
			//System.out.println("Player " + id + " is keeping back " + ps.get_held_item_type().name() + ".");
			return new Command(CommandType.KEEP_BACK);
		}
		
		// Take out a food item (or move player) if no animal is too close
		if (!ps.is_player_searching() && !monkeysTooClose && ps.get_held_item_type() == null) {
			foodCurrentlySearchingFor = null;
			FoodType foodType = ps.check_availability_item(FoodType.COOKIE) ? FoodType.COOKIE : 
								ps.check_availability_item(FoodType.FRUIT1) ? FoodType.FRUIT1 :
								ps.check_availability_item(FoodType.FRUIT2) ? FoodType.FRUIT2 : 
								ps.check_availability_item(FoodType.EGG) ? FoodType.EGG :
								ps.check_availability_item(FoodType.SANDWICH1) ? FoodType.SANDWICH1 :
								ps.check_availability_item(FoodType.SANDWICH2) ? FoodType.SANDWICH2	: 
								null;
			
			if(foodType != null) {
				
				// Take out the food item if it is not a sandwich and monkeys are not too close
				if(foodType != FoodType.SANDWICH1 && foodType != FoodType.SANDWICH2) {
					//System.out.println("Player " + id + " is taking out " + foodType.name() + ".");
					foodCurrentlySearchingFor = foodType;
					return new Command(CommandType.TAKE_OUT, foodType);
				}
				
				// Assign a designated corner for the player to eat sandwiches
				Point currPoint = ps.get_location();
				Point targetCorner = new Point(-1, -1);
				if(!targetCornersChosen.containsKey(id)) {
					targetCornersChosen.put(id, targetCorners.get(id % targetCorners.size()));
					//System.out.println("Player " + id + " will go to corner " + targetCornersChosen.get(id));
				}
				targetCorner = targetCornersChosen.get(id);

				// Eat sandwiches only if the player is in the corner and geese are not too close
				if(currPoint.x == targetCorner.x && currPoint.y == targetCorner.y) {
					if(!gooseTooClose) {
						//System.out.println("Player " + id + " is taking out a sandwich.");
						foodCurrentlySearchingFor = FoodType.SANDWICH;
						return new Command(CommandType.TAKE_OUT, foodType);
					}
					else {
						//System.out.println("Player " + id + " is not going to take out a sandwich because the goose is too close.");
						return new Command();
					}
				}
				
				// The player reaches the corner if the distance is within (or exactly) 1 m
				double distanceFromCorner = Math.sqrt(Math.pow(targetCorner.y - currPoint.y, 2) + Math.pow(targetCorner.x - currPoint.x, 2));
				if(distanceFromCorner <= 1.0) {
					//System.out.println("Player " + id + " is making its final move to the corner.");
					return Command.createMoveCommand(targetCorner);
				}

				// Move the player toward the corner
				double slope = ((double) (targetCorner.y - currPoint.y)) / ((double) (targetCorner.x - currPoint.x));
				double deltaX = (targetCorner.x > 0 ? 1.0 : -1.0) / Math.sqrt(Math.pow(slope, 2) + 1);
				double deltaY = Math.abs(slope) * (targetCorner.y > 0 ? 1.0 : -1.0) / Math.sqrt(Math.pow(slope, 2) + 1);
				//System.out.println("Player " + id + " is moving to the corner.");
				return Command.createMoveCommand(new Point(currPoint.x + deltaX, currPoint.y + deltaY));
			}
		}
		
		// Eat if no animal is too close
		if(!ps.is_player_searching() && ps.get_held_item_type() != null) {
			foodCurrentlySearchingFor = null;
			if((!monkeysTooClose && (ps.get_held_item_type() != FoodType.SANDWICH)) ||
				(!monkeysTooClose && !gooseTooClose && (ps.get_held_item_type() == FoodType.SANDWICH))) {
				//System.out.println("Player " + id + " is going to eat " + ps.get_held_item_type().name() + ".");
				return new Command(CommandType.EAT);
			}
		}
		
		// The player is waiting, as it did not submit any other actions
		//System.out.println("Player " + id + " is going to wait.");

		return new Command(CommandType.WAIT);
		
	}
	
	/**
	 * Prints all of the available items that
	 * the player currently has left
	 * 
	 * @param ps: this player's state
	 */
	private void printAvailability(PlayerState ps) {
		//System.out.println("Cookie is available: " + ps.check_availability_item(FoodType.COOKIE)); 
		//System.out.println("Fruit 1 is available: " + ps.check_availability_item(FoodType.FRUIT1)); 
		//System.out.println("Fruit 2 is available: " + ps.check_availability_item(FoodType.FRUIT2)); 
		//System.out.println("Egg is available: " + ps.check_availability_item(FoodType.EGG)); 
		//System.out.println("Sandwich 1 is available: " + ps.check_availability_item(FoodType.SANDWICH1)); 
		//System.out.println("Sandwich 2 is available: " + ps.check_availability_item(FoodType.SANDWICH2)); 
	}

	public class T {
		private Point location;
		private Point next;
		private Point v;
		private boolean isMonkey;

		public T(Point l, boolean isMonkey) {
			this.location = l;
			this.next = l;
			this.v = new Point(0.0, 0.0);
			this.isMonkey = isMonkey;
		}

		public void update(Point l) {
			Point temp = this.location;
			this.location = l;
			this.v.x = l.x - temp.x;
			this.v.y = l.y - temp.y;
		}

		public Point getNext() {
			Point output = new Point(0,0);
			output.x = this.location.x + this.v.x;
			output.y = this.location.y + this.v.y;
			return output;
		}

		public Point getNext10() {
			Point temp = new Point(0,0);
			temp.x = location.x + v.x * 10.;
			temp.y = location.y + v.y * 10.;

			if(temp.x > 50.) {
				temp.x = 50. - (temp.x - 50.);
			}
			else if(temp.x < -50.) {
				temp.x = -50. - (temp.x + 50.);
			}

			if(temp.y > 50.) {
				temp.y = 50. - (temp.y - 50.);
			}
			else if(temp.y < -50.) {
				temp.y = 50. - (temp.y - 50.);
			}

			return temp;
		}

		//assumes that you are in the corner for animals bouncing off the corner
		public boolean isFar(Point l) {
			Point temp = this.getNext10();

			double distance = Point.dist(l, temp);
			if(this.isMonkey) {
				if(distance > 40.) return true;
				else return false;
			}
			else {
				if(distance > 20.) return true;
				else return false;
			}
		}
	}
}