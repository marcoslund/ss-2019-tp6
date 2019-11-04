import os

RESULTS_FOLDER = 'analysis/results'
DEFAULT_OUTPUT = 'ovito_output.xyz'
EXIT_OUTPUT = 'exit.txt'
EXIT_FOLDER = 'analysis/exits'
REPEAT = 10
ITERATIONS = 1
SIMULATION = 'time java -jar target/tpes-1.0-SNAPSHOT.jar < params.txt &'
REMOVE = f'rm -fr {RESULTS_FOLDER}'

# create results folder if it does not exist
if os.path.exists(RESULTS_FOLDER):
  os.system(REMOVE)
os.makedirs(RESULTS_FOLDER)

# Generate multiple simulations
for iteration in range(ITERATIONS):
  for simNum in range(REPEAT):
    os.system(f'echo "{50 * (simNum + 1)}\n50\n{50 * (simNum + 10)}" > params.txt')
    os.system(SIMULATION) # run simulation
    os.system("sleep 2")
  # os.system("sleep 720")
  # os.system("rm *.txt")
  # os.system(f'mkdir runs_6--1.5/run{iteration + 1}')
  # for simNum in range(REPEAT + 1):
    # os.system(f'mv {(simNum + 1) * 5}.xyz runs_6--1.5/run{iteration + 1}/{(simNum + 1) * 5}.xyz')
