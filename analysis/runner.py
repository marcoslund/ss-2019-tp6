import os

RESULTS_FOLDER = 'analysis/results'
DEFAULT_OUTPUT = 'ovito_output.xyz'
EXIT_OUTPUT = 'exit.txt'
EXIT_FOLDER = 'analysis/exits'
REPEAT = 10
SIMULATION = 'java -jar target/tpes-1.0-SNAPSHOT.jar < params.txt &'
REMOVE = f'rm -fr {RESULTS_FOLDER}'

# create results folder if it does not exist
if os.path.exists(RESULTS_FOLDER):
  os.system(REMOVE)
os.makedirs(RESULTS_FOLDER)

# Generate multiple simulations
for simNum in range(REPEAT):
  os.system(f'echo "5\n{simNum}" > params.txt')
  MOVE = f'mv {DEFAULT_OUTPUT} {RESULTS_FOLDER}/{simNum}.xyz'
  MOVE_EXIT = f'mv {EXIT_OUTPUT} {EXIT_FOLDER}/{simNum}.txt'
  os.system(SIMULATION) # run simulation
  os.system("sleep 5")
  # os.system(MOVE) # store results
