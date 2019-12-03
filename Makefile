run:
	java lunch.sim.Simulator -t 3600 --players g4 -m 1 -g 0 -f 1 -s 42 -l log.txt

gui:
	java lunch.sim.Simulator -t 3600 --players g4 g4 g4 g4 -m 10 -g 6 -f 4 -s 42 --fps 100 --gui -l log.txt

compile:
	javac lunch/*/*.java

clean:
	rm lunch/*/*.class
