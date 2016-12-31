
# Andrew West - Makefile for the P2P Simulator (JAVA)

# Compilers
JC = javac

# Optimization flag
OPT = 

# Cleanup macro
UNMAKE = -rm *.class */*.class

# Macros for whole libraries
CORE_FILES = core_lib/*.java
GEN_FILES = generator_lib/*.java
SIM_FILES = simulator_lib/*.java 
TSYS_FILES = trust_system_lib/*.java

# ----------------------------------------------

# Give the commands to compile the executables.

all: TraceGenerator TraceSimulator

TraceGenerator: TraceGenerator.java $(CORE_FILES) $(GEN_FILES)
	@echo [Compiling trace generator]
	$(JC) $(OPT) TraceGenerator.java
	@echo [Trace generator compilation successful!]

TraceSimulator: TraceSimulator.java $(CORE_FILES) $(SIM_FILES) $(TSYS_FILES)
	@echo [Compiling trace simulator]
	$(JC) $(OPT) TraceSimulator.java
	@echo [Trace simulator compilation successful!]

delete:
	$(UNMAKE)

