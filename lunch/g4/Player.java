package lunch.g4;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import javafx.util.Pair; 
import java.util.ArrayList;

import lunch.sim.Point;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Animal;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.PlayerState;

public class Player implements lunch.sim.Player
{
	private int seed;
	private Random random;
	private Integer id;
	private Integer turn;
	private boolean inCorner;
	private double cornerMove;
	private Point corner;

	public Player()
	{
		turn = 0;
	}

	public void init(ArrayList<Family> members, Integer id, int f,ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s)
	{
		this.id = id;
		random = new Random(s);
		inCorner = false;
		cornerMove = Math.sqrt(2.) / 2.;
		corner = new Point(50., 50.);
	}

	public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps)
	{
		ArrayList<Animal> monkey = new ArrayList<>();
		ArrayList<Animal> geese = new ArrayList<>();
		ArrayList<Double> mDist = new ArrayList<>();
		ArrayList<Double> gDist = new ArrayList<>();

		updateList(animals, monkey, geese);

		Double min_dist = Double.MAX_VALUE;

		for(Integer i=0;i<animals.size();i++)
		{
			//dist.add(Point.dist(ps.get_location(), animals.get(i).get_location()));
			min_dist = Math.min(min_dist,Point.dist(ps.get_location(),animals.get(i).get_location()));
		}

		for(Integer i=0;i<monkey.size();i++)
		{
			mDist.add(Point.dist(ps.get_location(), animals.get(i).get_location()));
		}

		for(Integer i=0;i<geese.size();i++)
		{
			gDist.add(Point.dist(ps.get_location(), animals.get(i).get_location()));
		}

		//sortTwoList(monkey, mDist);
		//sortTwoList(geese, gDist);
		//System.out.println("Monkey");
		//System.out.println(mDist);
		//System.out.println("Geese");
		//System.out.println(gDist);

		while(!inCorner) {
			Point next_move = new Point(-1, -1);
			if(Point.dist(ps.get_location(), corner) <= 1) {
				inCorner = true;
				System.out.println(corner);
				return Command.createMoveCommand(corner);
			}
			next_move = new Point(ps.get_location().x + cornerMove, ps.get_location().y + cornerMove);
			turn++;
			//System.out.println(next_move);
			return Command.createMoveCommand(next_move);
		}
		turn++;
		System.out.println(turn);

		// abort taking out if animal is too close
		boolean danger = inDanger(monkey, geese, ps);
		if(danger && ps.is_player_searching() && ps.get_held_item_type()==null)
		{
			 System.out.println("abort command issued");
			 System.out.println(numMonkeyInArea(monkey, ps));
			// System.out.println(min_dist.toString());
			return new Command(CommandType.ABORT);
		}
		// keep food item back if animal is too close
		else if(!ps.is_player_searching() && ps.get_held_item_type()!=null && danger)
		{
			System.out.println("putting away food");
			System.out.println(numMonkeyInArea(monkey, ps));
			return new Command(CommandType.KEEP_BACK);
		}

		// if no animal is near then take out food
		else if (!ps.is_player_searching() &&  !danger && ps.get_held_item_type()==null )
		{
			System.out.println("taking out food");
			for(FoodType food_type: FoodType.values())
			{
				if(ps.check_availability_item(food_type))
				{
					Command c = new Command(CommandType.TAKE_OUT, food_type);
					return c;
				}
			}
		}
		// if no animal in vicinity then take a bite
		else if(!danger && !ps.is_player_searching() && ps.get_held_item_type()!=null)
		{
			System.out.println("Eating");
			System.out.println(numMonkeyInArea(monkey, ps));
			return new Command(CommandType.EAT);
		}

		System.out.println("player is waiting");
		return new Command(CommandType.WAIT);

	}

	public void sortTwoList(List<Animal> a, List<Double> b) {
		//System.out.println(b.size());
		for(int i = 0; i < b.size(); i++) {
			for(int j = i+1; j < b.size(); j++) {
				//System.out.println(i + ", " + j);
				if(b.get(i) > b.get(j)) {
					double dT = b.get(i);
					b.set(i, b.get(j));
					b.set(j, dT);

					Animal aT = a.get(j);
					a.set(i, a.get(j));
					a.set(i, aT);
				}
			}
		}
	}

	public void updateList(List<Animal> animals, List<Animal> monkey, List<Animal> geese) {
		for(Animal a : animals) {
			switch(a.which_animal()) {
				case MONKEY: 
					//System.out.println("MONKEY!");
					monkey.add(a);
					break;
				case GOOSE:
					//System.out.println("GEESE");
					geese.add(a);
					break;
			}
		}
	}

	public boolean inDanger(List<Animal> monkey, List<Animal> geese, PlayerState ps) {
		if(numMonkeyInArea(monkey, ps) >= 3) return true;
		if(geeseInArea(geese, ps)) return true;
		return false;
	}

	public int numMonkeyInArea(List<Animal> monkey, PlayerState ps) {
		int count = 0;
		for(Animal a: monkey) {
			if(Point.dist(a.get_location(), ps.get_location()) <= 6.+10e-7) {
				count++;
			}
		}
		return count;
	}

	public boolean geeseInArea(List<Animal> geese, PlayerState ps) {
		for(Animal a: geese) {
			if(Point.dist(a.get_location(), ps.get_location()) <= 5.+10e-7) return true;
		}
		return false;
	}

}
